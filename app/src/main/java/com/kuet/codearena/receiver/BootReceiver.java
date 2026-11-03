package com.kuet.codearena.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kuet.codearena.data.db.AppDatabase;
import com.kuet.codearena.data.db.entity.ReminderEntity;
import com.kuet.codearena.data.db.entity.UpsolveItemEntity;
import com.kuet.codearena.util.AlarmScheduler;
import com.kuet.codearena.util.AppExecutors;

import java.util.List;

/**
 * BroadcastReceiver restoring and rescheduling pending contest alarms and upsolve reminders after device reboot.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        Log.d(TAG, "Device reboot completed. Rescheduling all active alarms...");

        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                long now = System.currentTimeMillis();

                // 1. Reschedule active future contest reminders
                List<ReminderEntity> activeReminders = db.reminderDao().getFutureActiveReminders(now);
                if (activeReminders != null) {
                    for (ReminderEntity reminder : activeReminders) {
                        Intent reminderIntent = new Intent(context, AlarmReceiver.class);
                        reminderIntent.setAction(AlarmReceiver.ACTION_CONTEST_REMINDER);
                        reminderIntent.putExtra(AlarmReceiver.EXTRA_TITLE, "Upcoming Contest Reminder");
                        reminderIntent.putExtra(AlarmReceiver.EXTRA_MESSAGE,
                                reminder.getContestName() + " begins in " + reminder.getLeadTimeMinutes() + " minutes!");
                        reminderIntent.putExtra(AlarmReceiver.EXTRA_CONTEST_ID, reminder.getContestId());
                        reminderIntent.putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, (int) reminder.getId());

                        AlarmScheduler.scheduleExactAlarm(context, reminder.getReminderTimeMs(), reminderIntent, (int) reminder.getId());
                    }
                    Log.d(TAG, "Rescheduled " + activeReminders.size() + " contest reminders.");
                }

                // 2. Reschedule due upsolve items
                List<UpsolveItemEntity> pendingUpsolves = db.upsolveDao().getPendingSync();
                if (pendingUpsolves != null) {
                    for (UpsolveItemEntity upsolve : pendingUpsolves) {
                        if (upsolve.getDueDateMs() > now) {
                            Intent upsolveIntent = new Intent(context, AlarmReceiver.class);
                            upsolveIntent.setAction(AlarmReceiver.ACTION_UPSOLVE_REMINDER);
                            upsolveIntent.putExtra(AlarmReceiver.EXTRA_TITLE, "Upsolve Due: " + upsolve.getProblemName());
                            upsolveIntent.putExtra(AlarmReceiver.EXTRA_MESSAGE,
                                    "It's time to solve [" + upsolve.getProblemIndex() + "] " + upsolve.getProblemName() + " to maintain your CP streak!");
                            upsolveIntent.putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, (int) (20000 + upsolve.getId()));

                            AlarmScheduler.scheduleExactAlarm(context, upsolve.getDueDateMs(), upsolveIntent, (int) (20000 + upsolve.getId()));
                        }
                    }
                    Log.d(TAG, "Rescheduled " + pendingUpsolves.size() + " upsolve reminders.");
                }

                // 3. Reschedule daily sync alarm
                Intent dailySyncIntent = new Intent(context, AlarmReceiver.class);
                dailySyncIntent.setAction(AlarmReceiver.ACTION_DAILY_SYNC);
                long nextSyncTime = now + (24L * 60L * 60L * 1000L);
                AlarmScheduler.scheduleExactAlarm(context, nextSyncTime, dailySyncIntent, 99999);

            } catch (Exception e) {
                Log.e(TAG, "Failed to reschedule alarms after boot", e);
            }
        });
    }
}
