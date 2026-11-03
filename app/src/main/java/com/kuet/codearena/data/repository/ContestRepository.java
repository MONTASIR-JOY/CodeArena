package com.kuet.codearena.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.kuet.codearena.data.db.AppDatabase;
import com.kuet.codearena.data.db.dao.ContestDao;
import com.kuet.codearena.data.db.dao.ProblemDao;
import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.db.entity.ProblemEntity;
import com.kuet.codearena.data.remote.ApiClient;
import com.kuet.codearena.data.remote.Endpoints;
import com.kuet.codearena.data.remote.model.CfApiResponse;
import com.kuet.codearena.data.remote.model.CfContest;
import com.kuet.codearena.data.remote.model.CfProblem;
import com.kuet.codearena.data.remote.model.CfProblemStatistics;
import com.kuet.codearena.data.remote.model.CfProblemsResponse;
import com.kuet.codearena.data.remote.model.CfStandingsResponse;
import com.kuet.codearena.data.remote.model.Platform;
import com.kuet.codearena.util.AppExecutors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

/**
 * Repository orchestrating Codeforces contest listing, caching in Room, and problemset retrieval.
 */
public class ContestRepository {
    private static final String TAG = "ContestRepository";
    private static volatile ContestRepository INSTANCE;

    private final ContestDao contestDao;
    private final ProblemDao problemDao;
    private final AppExecutors executors;

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }

    private ContestRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.contestDao = db.contestDao();
        this.problemDao = db.problemDao();
        this.executors = AppExecutors.getInstance();
    }

    public static ContestRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ContestRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ContestRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<ContestEntity>> getPastContests() {
        return contestDao.getPastCodeforcesContests();
    }

    public LiveData<List<ContestEntity>> searchPastContests(String query) {
        return contestDao.searchPastCodeforcesContests(query);
    }

    public LiveData<ContestEntity> getContestByIdLive(int contestId) {
        return contestDao.getContestByIdLive(contestId);
    }

    public LiveData<List<ProblemEntity>> getProblemsForContest(int contestId) {
        return problemDao.getProblemsForContest(contestId);
    }

    /**
     * Refreshes finished Codeforces contests from official API and updates local database cache.
     */
    public void refreshPastContests(final OperationCallback callback) {
        executors.networkIO().execute(() -> {
            try {
                Response<CfApiResponse<List<CfContest>>> response =
                        ApiClient.getCodeforcesApi().getContests(false).execute();

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    List<CfContest> cfList = response.body().getResult();
                    List<ContestEntity> entities = new ArrayList<>();

                    for (CfContest cf : cfList) {
                        if ("FINISHED".equalsIgnoreCase(cf.getPhase())) {
                            String division = parseDivision(cf.getName());
                            String url = Endpoints.CF_PROBLEM_WEB_BASE + cf.getId();
                            entities.add(new ContestEntity(
                                    cf.getId(),
                                    cf.getName(),
                                    cf.getType(),
                                    cf.getPhase(),
                                    cf.getDurationSeconds(),
                                    cf.getStartTimeSeconds(),
                                    division,
                                    Platform.CODEFORCES.name(),
                                    url
                            ));
                        }
                    }

                    contestDao.insertAll(entities);
                    executors.mainThread().execute(callback::onSuccess);
                } else {
                    String msg = response.body() != null ? response.body().getComment() : "Failed to load contests";
                    executors.mainThread().execute(() -> callback.onError(msg));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching contests", e);
                executors.mainThread().execute(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    /**
     * Fetches problems of a specific contest via contest.standings and caches them in Room.
     */
    public void refreshContestProblems(final int contestId, final OperationCallback callback) {
        executors.networkIO().execute(() -> {
            try {
                Response<CfApiResponse<CfStandingsResponse>> response =
                        ApiClient.getCodeforcesApi().getContestStandings(contestId, 1, 1, false).execute();

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    CfStandingsResponse standings = response.body().getResult();
                    List<ProblemEntity> problemEntities = new ArrayList<>();

                    if (standings.getProblems() != null) {
                        for (CfProblem problem : standings.getProblems()) {
                            String tagsStr = problem.getTags() != null ? String.join(", ", problem.getTags()) : "";
                            problemEntities.add(new ProblemEntity(
                                    contestId,
                                    problem.getIndex(),
                                    problem.getName(),
                                    problem.getType(),
                                    problem.getRating(),
                                    tagsStr,
                                    0 // solvedCount populated from problemset sync
                            ));
                        }
                    }

                    problemDao.insertAll(problemEntities);
                    executors.mainThread().execute(callback::onSuccess);
                } else {
                    String msg = response.body() != null ? response.body().getComment() : "Failed to load contest problems";
                    executors.mainThread().execute(() -> callback.onError(msg));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching contest problems", e);
                executors.mainThread().execute(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    /**
     * Caches Codeforces problemset with ratings, tags, and solvedCount.
     */
    public void refreshProblemset(final OperationCallback callback) {
        executors.networkIO().execute(() -> {
            try {
                Response<CfApiResponse<CfProblemsResponse>> response =
                        ApiClient.getCodeforcesApi().getProblemsetProblems(null).execute();

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    CfProblemsResponse result = response.body().getResult();
                    Map<String, Integer> statsMap = new HashMap<>();

                    if (result.getProblemStatistics() != null) {
                        for (CfProblemStatistics stat : result.getProblemStatistics()) {
                            statsMap.put(stat.getContestId() + "_" + stat.getIndex(), stat.getSolvedCount());
                        }
                    }

                    List<ProblemEntity> entities = new ArrayList<>();
                    if (result.getProblems() != null) {
                        for (CfProblem p : result.getProblems()) {
                            String key = p.getContestId() + "_" + p.getIndex();
                            int solvedCount = statsMap.containsKey(key) ? statsMap.get(key) : 0;
                            String tagsStr = p.getTags() != null ? String.join(", ", p.getTags()) : "";
                            entities.add(new ProblemEntity(
                                    p.getContestId(),
                                    p.getIndex(),
                                    p.getName(),
                                    p.getType(),
                                    p.getRating(),
                                    tagsStr,
                                    solvedCount
                            ));
                        }
                    }

                    problemDao.insertAll(entities);
                    executors.mainThread().execute(callback::onSuccess);
                } else {
                    String msg = response.body() != null ? response.body().getComment() : "Failed to load problemset";
                    executors.mainThread().execute(() -> callback.onError(msg));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading problemset", e);
                executors.mainThread().execute(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public static String parseDivision(String name) {
        if (name == null) return "Other";
        if (name.contains("Div. 1") && name.contains("Div. 2")) return "Div. 1 + Div. 2";
        if (name.contains("Div. 1")) return "Div. 1";
        if (name.contains("Div. 2")) return "Div. 2";
        if (name.contains("Div. 3")) return "Div. 3";
        if (name.contains("Div. 4")) return "Div. 4";
        if (name.contains("Educational")) return "Educational";
        if (name.contains("Global")) return "Global";
        return "Other";
    }
}
