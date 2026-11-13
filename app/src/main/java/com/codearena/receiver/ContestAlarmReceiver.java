package com.codearena.receiver;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.codearena.MainActivity;
import com.codearena.R;
import com.codearena.data.repository.UserPreferencesRepository;

public class ContestAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "ContestAlarmReceiver";
    private static final String CHANNEL_ID = "contest_reminders";

    public static void scheduleContestReminder(Context context, long contestId, String contestName, long startTimeMillis) {
        if (context == null) return;
        try {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am == null) {
                new UserPreferencesRepository(context).setContestReminderSet(contestId, false);
                return;
            }

            long now = System.currentTimeMillis();
            long triggerAt = startTimeMillis - (30 * 60 * 1000L); // 30 minutes before
            if (triggerAt <= now) {
                // If contest starts in less than 30 minutes, notify in 5 seconds
                if (startTimeMillis > now) {
                    triggerAt = now + 5000L;
                } else {
                    new UserPreferencesRepository(context).setContestReminderSet(contestId, false);
                    return; // Contest already started or passed
                }
            }

            Intent intent = new Intent(context, ContestAlarmReceiver.class);
            intent.putExtra("contest_id", contestId);
            intent.putExtra("contest_name", contestName);

            PendingIntent pi = PendingIntent.getBroadcast(
                    context, (int) (contestId % Integer.MAX_VALUE),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Check exact alarm permission safely on Android 12+ (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    try {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                    } catch (SecurityException se) {
                        Log.w(TAG, "Exact alarm permission rejected, using inexact fallback", se);
                        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                    }
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                } catch (Exception e) {
                    am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                }
            } else {
                am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
            new UserPreferencesRepository(context).setContestReminderSet(contestId, true);
        } catch (Exception e) {
            new UserPreferencesRepository(context).setContestReminderSet(contestId, false);
            Log.e(TAG, "Failed to schedule contest reminder for " + contestName, e);
        }
    }

    public static void cancelContestReminder(Context context, long contestId) {
        if (context == null) return;
        try {
            new UserPreferencesRepository(context).setContestReminderSet(contestId, false);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am == null) return;

            Intent intent = new Intent(context, ContestAlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(
                    context, (int) (contestId % Integer.MAX_VALUE),
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pi != null) {
                am.cancel(pi);
                pi.cancel();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to cancel contest reminder", e);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        try {
            long contestId = intent.getLongExtra("contest_id", 0);
            String name = intent.getStringExtra("contest_name");
            if (name == null || name.trim().isEmpty()) {
                name = "Upcoming Contest";
            }

            if (!new UserPreferencesRepository(context).isNotificationsEnabled()) {
                Log.i(TAG, "Contest reminders are disabled in settings; skipping notification");
                return;
            }

            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID, "Contest Reminders", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Alerts for upcoming competitive programming contests");
                nm.createNotificationChannel(channel);
            }

            Intent openIntent = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(
                    context, 0, openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_timer)
                    .setContentTitle("Contest in 30 minutes!")
                    .setContentText(name)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            nm.notify((int) (contestId % Integer.MAX_VALUE), builder.build());
        } catch (Exception e) {
            Log.e(TAG, "Error showing contest notification", e);
        }
    }
}
