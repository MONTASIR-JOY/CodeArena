package com.kuet.codearena.data.repository;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.kuet.codearena.data.db.AppDatabase;
import com.kuet.codearena.data.db.dao.UpsolveDao;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.data.db.entity.UpsolveItemEntity;
import com.kuet.codearena.data.remote.ApiClient;
import com.kuet.codearena.data.remote.model.CfApiResponse;
import com.kuet.codearena.data.remote.model.CfSubmission;
import com.kuet.codearena.data.remote.model.Platform;
import com.kuet.codearena.receiver.AlarmReceiver;
import com.kuet.codearena.util.AlarmScheduler;
import com.kuet.codearena.util.AppExecutors;
import com.kuet.codearena.util.PreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Response;

/**
 * Repository orchestrating spaced-repetition upsolve lifecycle, API verification, and alarm reminders.
 */
public class UpsolveRepository {
    private static final String TAG = "UpsolveRepository";
    private static volatile UpsolveRepository INSTANCE;

    private final Context context;
    private final UpsolveDao upsolveDao;
    private final SyncRepository syncRepository;
    private final PreferenceHelper preferenceHelper;
    private final AppExecutors executors;

    public interface VerificationCallback {
        void onVerified(boolean apiVerified, String message);
        void onError(String error);
    }

    private UpsolveRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        this.upsolveDao = db.upsolveDao();
        this.syncRepository = SyncRepository.getInstance(context);
        this.preferenceHelper = new PreferenceHelper(context);
        this.executors = AppExecutors.getInstance();
    }

    public static UpsolveRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UpsolveRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UpsolveRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<UpsolveItemEntity>> getPendingUpsolves() {
        return upsolveDao.getPendingLive();
    }

    public LiveData<List<UpsolveItemEntity>> getAllUpsolves() {
        return upsolveDao.getAllLive();
    }

    public LiveData<List<UpsolveItemEntity>> getTodaySolvedUpsolves() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        long endOfDay = startOfDay + (24L * 60L * 60L * 1000L);
        return upsolveDao.getSolvedBetweenLive(startOfDay, endOfDay);
    }

    /**
     * Tries to verify problem solve status via Codeforces API; updates database and practice rating.
     */
    public void markSolvedAndVerify(final long upsolveId, final VerificationCallback callback) {
        executors.networkIO().execute(() -> {
            UpsolveItemEntity item = upsolveDao.getItemById(upsolveId);
            if (item == null) {
                executors.mainThread().execute(() -> callback.onError("Upsolve item not found"));
                return;
            }

            String handle = preferenceHelper.getCfHandle();
            boolean apiVerified = false;

            try {
                if (handle != null && !handle.trim().isEmpty()) {
                    Response<CfApiResponse<List<CfSubmission>>> response =
                            ApiClient.getCodeforcesApi().getUserStatus(handle, 1, 100).execute();

                    if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                        List<CfSubmission> subs = response.body().getResult();
                        if (subs != null) {
                            for (CfSubmission s : subs) {
                                if (s.getContestId() == item.getContestId() &&
                                        s.getProblem() != null &&
                                        item.getProblemIndex().equalsIgnoreCase(s.getProblem().getIndex()) &&
                                        "OK".equalsIgnoreCase(s.getVerdict())) {
                                    apiVerified = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "API verification network check failed: " + e.getMessage());
            }

            final boolean verified = apiVerified;
            long now = System.currentTimeMillis();
            item.setSolved(true);
            item.setSolvedAtMs(now);
            upsolveDao.update(item);

            // Record solve entity
            int rating = item.getRating() != null && item.getRating() > 0 ? item.getRating() : 800;
            String problemId = "CF-" + item.getContestId() + item.getProblemIndex();
            AppDatabase.getInstance(context).solveDao().insert(new SolveEntity(
                    Platform.CODEFORCES.name(),
                    problemId,
                    item.getProblemName(),
                    rating,
                    item.getTags(),
                    now
            ));

            // Record daily stat
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
            syncRepository.recordDailyStat(sdf.format(new Date(now)), rating);

            // Update Practice Rating
            double currentRating = preferenceHelper.getPracticeRating();
            double diff = (rating - currentRating) / 400.0;
            double expected = 1.0 / (1.0 + Math.pow(10.0, diff));
            currentRating = currentRating + (20.0 * (1.0 - expected));
            preferenceHelper.setPracticeRating(Math.max(0, currentRating));

            // Cancel pending reminder alarm
            Intent upsolveIntent = new Intent(context, AlarmReceiver.class);
            AlarmScheduler.cancelAlarm(context, upsolveIntent, (int) (20000 + item.getId()));

            executors.mainThread().execute(() -> {
                String msg = verified ? "Verified with Codeforces AC!" : "Marked solved manually (+points added)";
                callback.onVerified(verified, msg);
            });
        });
    }

    /**
     * Advances spaced repetition intervals (1 -> 3 -> 7 -> 14 days) or applies penalty if expired.
     */
    public void processSpacedRepetitionSchedule(final UpsolveItemEntity item) {
        executors.diskIO().execute(() -> {
            long now = System.currentTimeMillis();
            int nextInterval;

            switch (item.getIntervalStep()) {
                case 1: nextInterval = 3; break;
                case 3: nextInterval = 7; break;
                case 7: nextInterval = 14; break;
                default: nextInterval = -1; break; // Expired after 14 days
            }

            if (nextInterval != -1) {
                item.setIntervalStep(nextInterval);
                item.setDueDateMs(now + (nextInterval * 24L * 60L * 60L * 1000L));
                upsolveDao.update(item);

                // Schedule next alarm reminder
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.setAction(AlarmReceiver.ACTION_UPSOLVE_REMINDER);
                intent.putExtra(AlarmReceiver.EXTRA_TITLE, "Upsolve Due: " + item.getProblemName());
                intent.putExtra(AlarmReceiver.EXTRA_MESSAGE,
                        "Day " + nextInterval + " review for [" + item.getProblemIndex() + "] " + item.getProblemName());
                intent.putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, (int) (20000 + item.getId()));

                AlarmScheduler.scheduleExactAlarm(context, item.getDueDateMs(), intent, (int) (20000 + item.getId()));
            } else {
                // Expired after 14 days: penalty R = R - 5 (minimum 0)
                item.setExpired(true);
                upsolveDao.update(item);

                double rating = preferenceHelper.getPracticeRating();
                preferenceHelper.setPracticeRating(Math.max(0, rating - 5.0));
                Log.d(TAG, "Upsolve item expired after 14 days. Practice rating penalized by 5. New rating: " + preferenceHelper.getPracticeRating());
            }
        });
    }
}
