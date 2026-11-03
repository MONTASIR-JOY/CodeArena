package com.kuet.codearena.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.kuet.codearena.data.db.AppDatabase;
import com.kuet.codearena.data.db.dao.DailyStatDao;
import com.kuet.codearena.data.db.dao.SolveDao;
import com.kuet.codearena.data.db.entity.DailyStatEntity;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.data.remote.ApiClient;
import com.kuet.codearena.data.remote.AtCoderPlatformSource;
import com.kuet.codearena.data.remote.LeetCodePlatformSource;
import com.kuet.codearena.data.remote.model.CfApiResponse;
import com.kuet.codearena.data.remote.model.CfProblem;
import com.kuet.codearena.data.remote.model.CfSubmission;
import com.kuet.codearena.data.remote.model.Platform;
import com.kuet.codearena.util.AppExecutors;
import com.kuet.codearena.util.PreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Response;

/**
 * Repository synchronizing accepted problem solves across platforms and maintaining daily stats and practice ratings.
 */
public class SyncRepository {
    private static final String TAG = "SyncRepository";
    private static volatile SyncRepository INSTANCE;

    private final Context context;
    private final SolveDao solveDao;
    private final DailyStatDao dailyStatDao;
    private final AppExecutors executors;
    private final PreferenceHelper preferenceHelper;

    public interface SyncCallback {
        void onSuccess(int newSolves);
        void onError(String error);
    }

    private SyncRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        this.solveDao = db.solveDao();
        this.dailyStatDao = db.dailyStatDao();
        this.executors = AppExecutors.getInstance();
        this.preferenceHelper = new PreferenceHelper(context);
    }

    public static SyncRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SyncRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SyncRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<SolveEntity>> getAllSolvesLive() {
        return solveDao.getAllSolvesLive();
    }

    public LiveData<List<SolveEntity>> getTodaySolvesLive() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        long endOfDay = startOfDay + (24L * 60L * 60L * 1000L);
        return solveDao.getSolvesBetweenLive(startOfDay, endOfDay);
    }

    public LiveData<List<DailyStatEntity>> getAllDailyStatsLive() {
        return dailyStatDao.getAllStatsLive();
    }

    /** Synchronizes all configured platform handles. Called by background receivers using goAsync(). */
    public void syncAllPlatformsDirectly() {
        syncCodeforcesSolves(null);
        syncAtCoderSolves(preferenceHelper.getAtCoderHandle(), null);
        syncLeetCodeSolves(preferenceHelper.getLeetCodeHandle(), null);
    }

    /** Syncs all platforms and invokes callback with combined new solve count. */
    public void syncAllPlatforms(final SyncCallback callback) {
        final AtomicInteger newSolves = new AtomicInteger(0);
        final AtomicInteger pending = new AtomicInteger(3);

        SyncCallback inner = new SyncCallback() {
            @Override
            public void onSuccess(int count) {
                newSolves.addAndGet(count);
                if (pending.decrementAndGet() == 0 && callback != null) {
                    callback.onSuccess(newSolves.get());
                }
            }
            @Override
            public void onError(String error) {
                if (pending.decrementAndGet() == 0 && callback != null) {
                    callback.onSuccess(newSolves.get()); // partial success is still success
                }
            }
        };

        syncCodeforcesSolves(inner);
        syncAtCoderSolves(preferenceHelper.getAtCoderHandle(), inner);
        syncLeetCodeSolves(preferenceHelper.getLeetCodeHandle(), inner);
    }

    /** Synchronizes accepted submissions from Codeforces and updates Practice Rating + Daily Stats. */
    public void syncCodeforcesSolves(final SyncCallback callback) {
        executors.networkIO().execute(() -> {
            String cfHandle = preferenceHelper.getCfHandle();
            if (cfHandle == null || cfHandle.trim().isEmpty()) {
                if (callback != null) {
                    executors.mainThread().execute(() -> callback.onError("No Codeforces handle configured"));
                }
                return;
            }

            try {
                Response<CfApiResponse<List<CfSubmission>>> response =
                        ApiClient.getCodeforcesApi().getUserStatus(cfHandle, 1, 200).execute();

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    List<CfSubmission> submissions = response.body().getResult();
                    int newSolvesCount = 0;
                    double currentRating = preferenceHelper.getPracticeRating();

                    if (submissions != null) {
                        for (CfSubmission s : submissions) {
                            if ("OK".equalsIgnoreCase(s.getVerdict()) && s.getProblem() != null) {
                                CfProblem p = s.getProblem();
                                String problemId = "CF-" + p.getContestId() + p.getIndex();

                                if (solveDao.countMatching(Platform.CODEFORCES.name(), problemId) == 0) {
                                    Integer rating = p.getRating();
                                    int effectiveRating = rating != null && rating > 0 ? rating : 800;
                                    String tags = p.getTags() != null ? String.join(", ", p.getTags()) : "";
                                    long solvedAtMs = s.getCreationTimeSeconds() * 1000L;

                                    SolveEntity solve = new SolveEntity(
                                            Platform.CODEFORCES.name(),
                                            problemId,
                                            p.getName(),
                                            rating,
                                            tags,
                                            solvedAtMs
                                    );
                                    solveDao.insert(solve);
                                    newSolvesCount++;

                                    // Practice Rating ELO formula:
                                    // expected = 1 / (1 + 10^((p - R) / 400)); R = R + 20 * (1 - expected)
                                    double diff = (effectiveRating - currentRating) / 400.0;
                                    double expected = 1.0 / (1.0 + Math.pow(10.0, diff));
                                    currentRating = currentRating + (20.0 * (1.0 - expected));

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
                                    String dateKey = sdf.format(new Date(solvedAtMs));
                                    recordDailyStat(dateKey, effectiveRating);
                                }
                            }
                        }
                    }

                    preferenceHelper.setPracticeRating(Math.max(0, currentRating));

                    if (callback != null) {
                        final int finalCount = newSolvesCount;
                        executors.mainThread().execute(() -> callback.onSuccess(finalCount));
                    }
                } else {
                    String msg = response.body() != null ? response.body().getComment() : "Failed to sync Codeforces";
                    if (callback != null) {
                        executors.mainThread().execute(() -> callback.onError(msg));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing Codeforces", e);
                if (callback != null) {
                    executors.mainThread().execute(() -> callback.onError("Network error: " + e.getMessage()));
                }
            }
        });
    }

    /** Synchronizes accepted submissions from AtCoder via kenkoooo.com community API. */
    public void syncAtCoderSolves(final String handle, final SyncCallback callback) {
        if (handle == null || handle.trim().isEmpty()) {
            if (callback != null) {
                executors.mainThread().execute(() -> callback.onError("No AtCoder handle configured"));
            }
            return;
        }
        executors.networkIO().execute(() -> {
            try {
                AtCoderPlatformSource source = new AtCoderPlatformSource();
                List<SolveEntity> solves = source.fetchUserAcceptedSubmissions(handle);
                int newSolvesCount = 0;
                double currentRating = preferenceHelper.getPracticeRating();

                for (SolveEntity solve : solves) {
                    if (solveDao.countMatching(solve.getPlatform(), solve.getProblemId()) == 0) {
                        solveDao.insert(solve);
                        newSolvesCount++;

                        int effectiveRating = solve.getRating() != null ? solve.getRating() : 800;
                        double diff = (effectiveRating - currentRating) / 400.0;
                        double expected = 1.0 / (1.0 + Math.pow(10.0, diff));
                        currentRating = currentRating + (20.0 * (1.0 - expected));

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
                        String dateKey = sdf.format(new Date(solve.getSolvedAt()));
                        recordDailyStat(dateKey, effectiveRating);
                    }
                }

                preferenceHelper.setPracticeRating(Math.max(0, currentRating));

                if (callback != null) {
                    final int finalCount = newSolvesCount;
                    executors.mainThread().execute(() -> callback.onSuccess(finalCount));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing AtCoder", e);
                if (callback != null) {
                    executors.mainThread().execute(() -> callback.onError("AtCoder sync error: " + e.getMessage()));
                }
            }
        });
    }

    /** Synchronizes recent accepted submissions from LeetCode via public GraphQL API. */
    public void syncLeetCodeSolves(final String handle, final SyncCallback callback) {
        if (handle == null || handle.trim().isEmpty()) {
            if (callback != null) {
                executors.mainThread().execute(() -> callback.onError("No LeetCode handle configured"));
            }
            return;
        }
        executors.networkIO().execute(() -> {
            try {
                LeetCodePlatformSource source = new LeetCodePlatformSource();
                List<SolveEntity> solves = source.fetchUserAcceptedSubmissions(handle);
                int newSolvesCount = 0;
                double currentRating = preferenceHelper.getPracticeRating();

                for (SolveEntity solve : solves) {
                    if (solveDao.countMatching(solve.getPlatform(), solve.getProblemId()) == 0) {
                        solveDao.insert(solve);
                        newSolvesCount++;

                        int effectiveRating = solve.getRating() != null ? solve.getRating() : 800;
                        double diff = (effectiveRating - currentRating) / 400.0;
                        double expected = 1.0 / (1.0 + Math.pow(10.0, diff));
                        currentRating = currentRating + (20.0 * (1.0 - expected));

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
                        String dateKey = sdf.format(new Date(solve.getSolvedAt()));
                        recordDailyStat(dateKey, effectiveRating);
                    }
                }

                preferenceHelper.setPracticeRating(Math.max(0, currentRating));

                if (callback != null) {
                    final int finalCount = newSolvesCount;
                    executors.mainThread().execute(() -> callback.onSuccess(finalCount));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing LeetCode", e);
                if (callback != null) {
                    executors.mainThread().execute(() -> callback.onError("LeetCode sync error: " + e.getMessage()));
                }
            }
        });
    }

    /** Thread-safe update of a single day's cumulative stats. */
    public synchronized void recordDailyStat(String dateKey, int points) {
        DailyStatEntity stat = dailyStatDao.getStatForDate(dateKey);
        if (stat != null) {
            stat.setSolvedCount(stat.getSolvedCount() + 1);
            stat.setPoints(stat.getPoints() + points);
            dailyStatDao.insertOrUpdate(stat);
        } else {
            dailyStatDao.insertOrUpdate(new DailyStatEntity(dateKey, 1, points));
        }
    }
}
