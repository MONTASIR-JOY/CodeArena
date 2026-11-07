package com.codearena.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.codearena.data.api.ApiClient;
import com.codearena.data.api.CodeforcesApi;
import com.codearena.data.db.AppDatabase;
import com.codearena.data.db.entity.ContestEntity;
import com.codearena.data.db.entity.ProblemEntity;
import com.codearena.data.model.CfBlogEntry;
import com.codearena.data.model.CfContest;
import com.codearena.data.model.CfProblem;
import com.codearena.data.model.CfProblemsetResult;
import com.codearena.data.model.CfRatingChange;
import com.codearena.data.model.CfRecentAction;
import com.codearena.data.model.CfResponse;
import com.codearena.data.model.CfStandingsResult;
import com.codearena.data.model.CfSubmission;
import com.codearena.data.model.CfUser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

public class CodeforcesRepository {

    private final CodeforcesApi api;
    private final AppDatabase database;

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }

    public CodeforcesRepository(Context context) {
        this.api = ApiClient.getCodeforcesApi();
        this.database = context != null ? AppDatabase.getInstance(context) : null;
    }

    public LiveData<List<CfContest>> getCachedFinishedContestsLiveData() {
        if (database == null) return null;
        return Transformations.map(database.contestDao().getFinishedContestsLiveData(), list -> {
            List<CfContest> contests = new ArrayList<>();
            if (list != null) {
                for (ContestEntity entity : list) {
                    contests.add(entity.toCfContest());
                }
            }
            return contests;
        });
    }

    public void validateAndFetchUser(String handle, Callback<CfUser> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                String cleanHandle = handle != null ? handle.trim() : "";
                if (cleanHandle.isEmpty()) {
                    callback.onError(new IllegalArgumentException("Handle cannot be empty"));
                    return;
                }
                Response<CfResponse<List<CfUser>>> response = api.getUserInfo(cleanHandle).execute();
                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    List<CfUser> list = response.body().getResult();
                    if (list != null && !list.isEmpty()) {
                        callback.onSuccess(list.get(0));
                        return;
                    }
                }
                String err = (response.body() != null && response.body().getComment() != null) ?
                        response.body().getComment() : "Handle '" + cleanHandle + "' not found on Codeforces";
                callback.onError(new Exception(err));
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getCachedFinishedContests(Callback<List<CfContest>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (database != null) {
                List<ContestEntity> cached = database.contestDao().getFinishedContests();
                if (cached != null && !cached.isEmpty()) {
                    List<CfContest> contests = new ArrayList<>();
                    for (ContestEntity entity : cached) {
                        contests.add(entity.toCfContest());
                    }
                    callback.onSuccess(contests);
                    return;
                }
            }
            callback.onError(new Exception("No cached contests"));
        });
    }

    public void fetchContests(Callback<List<CfContest>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Response<CfResponse<List<CfContest>>> response = api.getContestList(false).execute();
                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    List<CfContest> contests = response.body().getResult();
                    if (contests != null) {
                        // Immediately deliver results to UI to avoid blocking on SQLite batch insert
                        callback.onSuccess(contests);

                        if (database != null) {
                            try {
                                List<ContestEntity> entities = new ArrayList<>();
                                for (CfContest c : contests) {
                                    entities.add(ContestEntity.fromCfContest(c));
                                }
                                database.contestDao().insertContests(entities);
                            } catch (Exception ignored) {}
                        }
                        return;
                    }
                }
                fallbackToCachedContests("Failed to load contests from Codeforces", callback);
            } catch (Exception e) {
                fallbackToCachedContests(e.getMessage() != null ? e.getMessage() : "Network error", callback);
            }
        });
    }

    private void fallbackToCachedContests(String err, Callback<List<CfContest>> callback) {
        if (database != null) {
            List<ContestEntity> cached = database.contestDao().getFinishedContests();
            if (cached != null && !cached.isEmpty()) {
                List<CfContest> contests = new ArrayList<>();
                for (ContestEntity entity : cached) {
                    contests.add(entity.toCfContest());
                }
                callback.onSuccess(contests);
                return;
            }
        }
        callback.onError(new Exception(err));
    }

    public void fetchContestStandings(long contestId, Callback<CfStandingsResult> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Response<CfResponse<CfStandingsResult>> response =
                        api.getContestStandings(contestId).execute();
                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    CfStandingsResult result = response.body().getResult();
                    if (result != null) {
                        if (database != null && result.getProblems() != null) {
                            List<ProblemEntity> entities = new ArrayList<>();
                            for (CfProblem p : result.getProblems()) {
                                entities.add(ProblemEntity.fromCfProblem(contestId, p));
                            }
                            database.problemDao().insertProblems(entities);
                        }
                        callback.onSuccess(result);
                        return;
                    }
                }
                fallbackToCachedProblems(contestId, "Failed to load contest standings", callback);
            } catch (Exception e) {
                fallbackToCachedProblems(contestId, e.getMessage() != null ? e.getMessage() : "Network error", callback);
            }
        });
    }

    private void fallbackToCachedProblems(long contestId, String err, Callback<CfStandingsResult> callback) {
        if (database != null) {
            List<ProblemEntity> cached = database.problemDao().getProblemsForContest(contestId);
            if (cached != null && !cached.isEmpty()) {
                List<CfProblem> problems = new ArrayList<>();
                for (ProblemEntity entity : cached) {
                    problems.add(entity.toCfProblem());
                }
                ContestEntity contestEntity = database.contestDao().getContestById(contestId);
                CfContest contest = contestEntity != null ? contestEntity.toCfContest() :
                        new CfContest(contestId, "Contest #" + contestId, "CF", "FINISHED", 7200, 0L);
                callback.onSuccess(new CfStandingsResult(contest, problems, new ArrayList<>()));
                return;
            }
        }
        callback.onError(new Exception(err));
    }

    public LiveData<List<CfProblem>> getCachedProblemsLiveData(long contestId) {
        if (database == null) return null;
        return Transformations.map(database.problemDao().getProblemsForContestLiveData(contestId), list -> {
            List<CfProblem> problems = new ArrayList<>();
            if (list != null) {
                for (ProblemEntity entity : list) {
                    problems.add(entity.toCfProblem());
                }
            }
            return problems;
        });
    }

    public void fetchUserSubmissions(String handle, int count, Callback<List<CfSubmission>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Response<CfResponse<List<CfSubmission>>> response = api.getUserStatus(handle, 1, count).execute();
                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    callback.onSuccess(response.body().getResult() != null ? response.body().getResult() : new ArrayList<>());
                } else {
                    callback.onError(new Exception("Failed to load submissions"));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void fetchUserRatingHistory(String handle, Callback<List<CfRatingChange>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Response<CfResponse<List<CfRatingChange>>> response = api.getUserRating(handle).execute();
                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    callback.onSuccess(response.body().getResult() != null ? response.body().getResult() : new ArrayList<>());
                } else {
                    callback.onError(new Exception("Failed to load rating history"));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void fetchProblemset(Callback<CfProblemsetResult> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Response<CfResponse<CfProblemsetResult>> response = api.getProblemset().execute();
                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    callback.onSuccess(response.body().getResult());
                } else {
                    callback.onError(new Exception("Failed to load problemset"));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void fetchProblemsetByTag(String tag, Callback<CfProblemsetResult> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Response<CfResponse<CfProblemsetResult>> response = api.getProblemsetByTag(tag).execute();
                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    callback.onSuccess(response.body().getResult());
                } else {
                    callback.onError(new Exception("Failed to load problems for tag: " + tag));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void fetchUsers(List<String> handles, Callback<List<CfUser>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                if (handles == null || handles.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }
                String joined = String.join(";", handles);
                Response<CfResponse<List<CfUser>>> response = api.getUserInfo(joined).execute();
                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    callback.onSuccess(response.body().getResult() != null ? response.body().getResult() : new ArrayList<>());
                } else {
                    callback.onError(new Exception("Failed to load users"));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void fetchRecentActions(Callback<List<CfRecentAction>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Response<CfResponse<List<CfRecentAction>>> response = api.getRecentActions(100).execute();
                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    callback.onSuccess(response.body().getResult() != null ? response.body().getResult() : new ArrayList<>());
                } else {
                    callback.onError(new Exception("Failed to load recent actions"));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void fetchBlogEntry(long blogEntryId, Callback<CfBlogEntry> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Response<CfResponse<CfBlogEntry>> response = api.getBlogEntry(blogEntryId).execute();
                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    callback.onSuccess(response.body().getResult());
                } else {
                    callback.onError(new Exception("Failed to load blog entry"));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void fetchUserBlogEntries(String handle, Callback<List<CfBlogEntry>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Response<CfResponse<List<CfBlogEntry>>> response = api.getUserBlogEntries(handle).execute();
                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    callback.onSuccess(response.body().getResult() != null ? response.body().getResult() : new ArrayList<>());
                } else {
                    callback.onError(new Exception("Failed to load user blog entries"));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void fetchAllSubmissions(String handle, Callback<List<CfSubmission>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CfSubmission> all = new ArrayList<>();
            int from = 1;
            int count = 2000;
            try {
                while (true) {
                    Response<CfResponse<List<CfSubmission>>> response = api.getUserStatus(handle, from, count).execute();
                    if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                        List<CfSubmission> page = response.body().getResult();
                        if (page == null || page.isEmpty()) break;
                        all.addAll(page);
                        if (page.size() < count) break;
                        from += page.size();
                    } else {
                        break;
                    }
                }
                callback.onSuccess(all);
            } catch (Exception e) {
                if (!all.isEmpty()) {
                    callback.onSuccess(all);
                } else {
                    callback.onError(e);
                }
            }
        });
    }
}

