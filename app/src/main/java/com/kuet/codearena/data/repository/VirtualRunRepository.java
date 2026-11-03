package com.kuet.codearena.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.kuet.codearena.data.db.AppDatabase;
import com.kuet.codearena.data.db.dao.ProblemDao;
import com.kuet.codearena.data.db.dao.RunDao;
import com.kuet.codearena.data.db.dao.UpsolveDao;
import com.kuet.codearena.data.db.entity.ProblemEntity;
import com.kuet.codearena.data.db.entity.RunEntity;
import com.kuet.codearena.data.db.entity.RunProblemEntity;
import com.kuet.codearena.data.db.entity.UpsolveItemEntity;
import com.kuet.codearena.data.remote.ApiClient;
import com.kuet.codearena.data.remote.model.CfApiResponse;
import com.kuet.codearena.data.remote.model.CfSubmission;
import com.kuet.codearena.util.AppExecutors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

/**
 * Repository orchestrating virtual contest run lifecycles, submission polls, and post-contest metrics.
 */
public class VirtualRunRepository {
    private static final String TAG = "VirtualRunRepository";
    private static volatile VirtualRunRepository INSTANCE;

    private final RunDao runDao;
    private final ProblemDao problemDao;
    private final UpsolveDao upsolveDao;
    private final AppExecutors executors;

    public interface RunCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    private VirtualRunRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.runDao = db.runDao();
        this.problemDao = db.problemDao();
        this.upsolveDao = db.upsolveDao();
        this.executors = AppExecutors.getInstance();
    }

    public static VirtualRunRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (VirtualRunRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new VirtualRunRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<RunEntity> getRunLive(long runId) {
        return runDao.getRunByIdLive(runId);
    }

    public LiveData<List<RunProblemEntity>> getRunProblemsLive(long runId) {
        return runDao.getRunProblemsLive(runId);
    }

    public LiveData<List<RunEntity>> getAllRunsLive() {
        return runDao.getAllRunsLive();
    }

    /**
     * Initializes a new virtual run in Room and sets up associated problem records.
     */
    public void startRun(final int contestId, final String contestName, final long durationSeconds, final RunCallback<Long> callback) {
        executors.diskIO().execute(() -> {
            try {
                long nowMs = System.currentTimeMillis();
                long durationMs = durationSeconds * 1000L;

                RunEntity run = new RunEntity(
                        contestId,
                        contestName,
                        nowMs,
                        durationMs,
                        0,
                        "RUNNING",
                        0.0,
                        0,
                        0,
                        0
                );
                long runId = runDao.insertRun(run);

                // Fetch cached problems or dummy set if not yet populated
                List<ProblemEntity> cachedProblems = problemDao.getProblemsForContestSync(contestId);
                List<RunProblemEntity> runProblems = new ArrayList<>();

                if (cachedProblems != null && !cachedProblems.isEmpty()) {
                    for (ProblemEntity p : cachedProblems) {
                        runProblems.add(new RunProblemEntity(
                                runId,
                                p.getProblemIndex(),
                                p.getName(),
                                p.getRating(),
                                false,
                                0,
                                0
                        ));
                    }
                } else {
                    // Default fallback problem set (A-E)
                    String[] defaultIndices = {"A", "B", "C", "D", "E"};
                    for (String idx : defaultIndices) {
                        runProblems.add(new RunProblemEntity(
                                runId,
                                idx,
                                "Problem " + idx,
                                800 + (idx.charAt(0) - 'A') * 200,
                                false,
                                0,
                                0
                        ));
                    }
                }

                runDao.insertRunProblems(runProblems);
                executors.mainThread().execute(() -> callback.onSuccess(runId));
            } catch (Exception e) {
                Log.e(TAG, "Error starting run", e);
                executors.mainThread().execute(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Polls Codeforces user.status, checks verdicts, and computes penalties and running score.
     */
    public void checkSubmissions(final long runId, final int contestId, final long startTimeMs, final String handle) {
        executors.networkIO().execute(() -> {
            try {
                Response<CfApiResponse<List<CfSubmission>>> response =
                        ApiClient.getCodeforcesApi().getUserStatus(handle, 1, 100).execute();

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    List<CfSubmission> submissions = response.body().getResult();
                    long startSec = startTimeMs / 1000L;

                    // Filter contest submissions made after run start time
                    List<CfSubmission> contestSubs = new ArrayList<>();
                    if (submissions != null) {
                        for (CfSubmission s : submissions) {
                            if (s.getContestId() == contestId && s.getCreationTimeSeconds() >= startSec) {
                                contestSubs.add(s);
                            }
                        }
                    }

                    // Sort chronologically ascending
                    Collections.sort(contestSubs, Comparator.comparingLong(CfSubmission::getCreationTimeSeconds));

                    // Group by problem index
                    Map<String, List<CfSubmission>> subsByProblem = new HashMap<>();
                    for (CfSubmission s : contestSubs) {
                        if (s.getProblem() != null && s.getProblem().getIndex() != null) {
                            String idx = s.getProblem().getIndex();
                            if (!subsByProblem.containsKey(idx)) {
                                subsByProblem.put(idx, new ArrayList<>());
                            }
                            subsByProblem.get(idx).add(s);
                        }
                    }

                    // Update problem statuses in Room
                    List<RunProblemEntity> currentProblems = runDao.getRunProblemsSync(runId);
                    RunEntity run = runDao.getRunById(runId);
                    if (run == null || currentProblems == null) return;

                    int solvedCount = 0;
                    int totalWrongAttempts = 0;
                    long totalPenaltyMinutes = 0;
                    double totalScore = 0.0;
                    double contestMinutes = (double) run.getDurationMs() / (60.0 * 1000.0);

                    for (RunProblemEntity rp : currentProblems) {
                        List<CfSubmission> pSubs = subsByProblem.get(rp.getProblemIndex());
                        if (pSubs != null && !pSubs.isEmpty()) {
                            boolean solved = false;
                            int wrongCount = 0;
                            long solveTimeSec = 0;

                            for (CfSubmission sub : pSubs) {
                                if ("OK".equalsIgnoreCase(sub.getVerdict())) {
                                    solved = true;
                                    solveTimeSec = sub.getCreationTimeSeconds() - startSec;
                                    break;
                                } else {
                                    wrongCount++;
                                }
                            }

                            rp.setSolved(solved);
                            rp.setWrongAttempts(wrongCount);
                            rp.setSolveTimeSeconds(solveTimeSec);
                            runDao.updateRunProblem(rp);

                            if (solved) {
                                solvedCount++;
                                long solveMinutes = Math.max(1, solveTimeSec / 60);
                                totalPenaltyMinutes += solveMinutes + (wrongCount * 20L);

                                int rating = rp.getRating() != null && rp.getRating() > 0 ? rp.getRating() : 800;
                                double timeFactor = Math.max(0.3, 1.0 - ((double) solveMinutes / contestMinutes));
                                totalScore += (rating * timeFactor);
                            }
                            totalWrongAttempts += wrongCount;
                        }
                    }

                    totalScore = Math.max(0, totalScore - (50 * totalWrongAttempts));

                    run.setSolvedCount(solvedCount);
                    run.setTotalWrongAttempts(totalWrongAttempts);
                    run.setTotalPenaltyMinutes(totalPenaltyMinutes);
                    run.setScore(totalScore);
                    runDao.updateRun(run);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking submissions", e);
            }
        });
    }

    /**
     * Finishes a virtual contest run, enqueues unsolved problems for spaced upsolve, and recalculates score.
     */
    public void finishRun(final long runId, final RunCallback<RunEntity> callback) {
        executors.diskIO().execute(() -> {
            try {
                RunEntity run = runDao.getRunById(runId);
                if (run != null) {
                    run.setStatus("FINISHED");
                    run.setFinishTimeMs(System.currentTimeMillis());
                    runDao.updateRun(run);

                    // F5: Enqueue unsolved problems into Upsolve queue (Day 1 repetition)
                    List<RunProblemEntity> problems = runDao.getRunProblemsSync(runId);
                    long now = System.currentTimeMillis();
                    long oneDayMs = 24L * 60L * 60L * 1000L;

                    if (problems != null) {
                        for (RunProblemEntity rp : problems) {
                            if (!rp.isSolved()) {
                                UpsolveItemEntity existing = upsolveDao.getItemByContestAndIndex(
                                        run.getContestId(), rp.getProblemIndex()
                                );
                                if (existing == null) {
                                    upsolveDao.insert(new UpsolveItemEntity(
                                            run.getContestId(),
                                            rp.getProblemIndex(),
                                            rp.getProblemName(),
                                            rp.getRating(),
                                            "",
                                            now,
                                            now + oneDayMs, // First spaced repetition due in 1 day
                                            1,
                                            false,
                                            0,
                                            false
                                    ));
                                }
                            }
                        }
                    }

                    executors.mainThread().execute(() -> callback.onSuccess(run));
                } else {
                    executors.mainThread().execute(() -> callback.onError("Run not found"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error finishing run", e);
                executors.mainThread().execute(() -> callback.onError(e.getMessage()));
            }
        });
    }
}
