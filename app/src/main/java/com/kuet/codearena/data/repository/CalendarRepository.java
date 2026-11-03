package com.kuet.codearena.data.repository;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.kuet.codearena.data.db.AppDatabase;
import com.kuet.codearena.data.db.dao.ContestDao;
import com.kuet.codearena.data.db.dao.ReminderDao;
import com.kuet.codearena.data.db.dao.SolveDao;
import com.kuet.codearena.data.db.dao.UpsolveDao;
import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.db.entity.ReminderEntity;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.data.db.entity.UpsolveItemEntity;
import com.kuet.codearena.data.remote.AtCoderPlatformSource;
import com.kuet.codearena.data.remote.ClistPlatformSource;
import com.kuet.codearena.data.remote.CodeChefPlatformSource;
import com.kuet.codearena.data.remote.CodeforcesPlatformSource;
import com.kuet.codearena.data.remote.LeetCodePlatformSource;
import com.kuet.codearena.data.remote.PlatformSource;
import com.kuet.codearena.receiver.AlarmReceiver;
import com.kuet.codearena.util.AlarmScheduler;
import com.kuet.codearena.util.AppExecutors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository synchronizing upcoming contests across all four platforms and coordinating reminder alarms.
 */
public class CalendarRepository {
    private static final String TAG = "CalendarRepository";
    private static volatile CalendarRepository INSTANCE;

    private final Context context;
    private final ContestDao contestDao;
    private final ReminderDao reminderDao;
    private final UpsolveDao upsolveDao;
    private final SolveDao solveDao;
    private final AppExecutors executors;

    private final List<PlatformSource> sources = new ArrayList<>();
    private final ClistPlatformSource clistSource = new ClistPlatformSource();

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }

    private CalendarRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        this.contestDao = db.contestDao();
        this.reminderDao = db.reminderDao();
        this.upsolveDao = db.upsolveDao();
        this.solveDao = db.solveDao();
        this.executors = AppExecutors.getInstance();

        sources.add(new CodeforcesPlatformSource());
        sources.add(new AtCoderPlatformSource());
        sources.add(new LeetCodePlatformSource());
        sources.add(new CodeChefPlatformSource());
    }

    public static CalendarRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CalendarRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CalendarRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<ContestEntity>> getUpcomingContests() {
        long nowSec = System.currentTimeMillis() / 1000L;
        return contestDao.getUpcomingContests(nowSec);
    }

    public LiveData<List<ReminderEntity>> getRemindersForContest(int contestId) {
        return reminderDao.getRemindersForContestLive(contestId);
    }

    public LiveData<List<UpsolveItemEntity>> getAllUpsolvesLive() {
        return upsolveDao.getAllLive();
    }

    public LiveData<List<SolveEntity>> getAllSolvesLive() {
        return solveDao.getAllSolvesLive();
    }

    /**
     * Refreshes upcoming contests using clist.by API with seamless individual platform fallbacks.
     */
    public void refreshUpcomingContests(final OperationCallback callback) {
        executors.networkIO().execute(() -> {
            Map<Integer, ContestEntity> contestMap = new HashMap<>();

            // 1. Primary: clist.by (if configured)
            if (ClistPlatformSource.isConfigured()) {
                try {
                    List<ContestEntity> clistContests = clistSource.fetchUpcomingContests();
                    for (ContestEntity c : clistContests) {
                        contestMap.put(c.getId(), c);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "clist.by upcoming contests fetch failed, falling back to direct sources: " + e.getMessage());
                }
            }

            // 2. Direct platform fallbacks
            for (PlatformSource source : sources) {
                try {
                    List<ContestEntity> list = source.fetchUpcomingContests();
                    if (list != null) {
                        for (ContestEntity c : list) {
                            if (!contestMap.containsKey(c.getId())) {
                                contestMap.put(c.getId(), c);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error fetching upcoming from " + source.getPlatform() + ": " + e.getMessage());
                }
            }

            if (!contestMap.isEmpty()) {
                contestDao.insertAll(new ArrayList<>(contestMap.values()));
                executors.mainThread().execute(callback::onSuccess);
            } else {
                executors.mainThread().execute(() -> callback.onError("Could not refresh upcoming contests. Showing cached data."));
            }
        });
    }

    public void setReminder(final ContestEntity contest, final int leadTimeMinutes) {
        executors.diskIO().execute(() -> {
            long contestStartMs = contest.getStartTimeSeconds() * 1000L;
            long reminderTimeMs = contestStartMs - (leadTimeMinutes * 60L * 1000L);

            ReminderEntity existing = reminderDao.getByContestAndLead(contest.getId(), leadTimeMinutes);
            long reminderId;

            if (existing != null) {
                existing.setActive(true);
                existing.setReminderTimeMs(reminderTimeMs);
                reminderDao.update(existing);
                reminderId = existing.getId();
            } else {
                ReminderEntity entity = new ReminderEntity(
                        contest.getId(),
                        contest.getName(),
                        contest.getPlatform(),
                        contestStartMs,
                        reminderTimeMs,
                        leadTimeMinutes,
                        true
                );
                reminderId = reminderDao.insert(entity);
            }

            // Schedule alarm via AlarmManager
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.setAction(AlarmReceiver.ACTION_CONTEST_REMINDER);
            intent.putExtra(AlarmReceiver.EXTRA_TITLE, "Upcoming " + contest.getPlatform() + " Contest!");
            intent.putExtra(AlarmReceiver.EXTRA_MESSAGE, contest.getName() + " begins in " + leadTimeMinutes + " minutes!");
            intent.putExtra(AlarmReceiver.EXTRA_CONTEST_ID, contest.getId());
            intent.putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, (int) reminderId);

            AlarmScheduler.scheduleExactAlarm(context, reminderTimeMs, intent, (int) reminderId);
        });
    }

    public void removeReminder(final int contestId, final int leadTimeMinutes) {
        executors.diskIO().execute(() -> {
            ReminderEntity existing = reminderDao.getByContestAndLead(contestId, leadTimeMinutes);
            if (existing != null) {
                Intent intent = new Intent(context, AlarmReceiver.class);
                AlarmScheduler.cancelAlarm(context, intent, (int) existing.getId());
                reminderDao.deleteById(existing.getId());
            }
        });
    }
}

