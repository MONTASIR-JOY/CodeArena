package com.kuet.codearena.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.kuet.codearena.receiver.AlarmReceiver;

/**
 * Utility managing AlarmManager exact alarms with API 31 permission checks and fallback handling.
 */
public class AlarmScheduler {
    private static final String TAG = "AlarmScheduler";

    public static void scheduleExactAlarm(Context context, long triggerTimeMs, Intent intent, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        // API 31+ exact alarm permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent);
                Log.d(TAG, "Scheduled exact alarm with canScheduleExactAlarms() at " + triggerTimeMs);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent);
                Log.d(TAG, "Fallback: Scheduled with setAndAllowWhileIdle() at " + triggerTimeMs);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent);
            Log.d(TAG, "Scheduled exact alarm on pre-API 31 at " + triggerTimeMs);
        }
    }

    public static void scheduleDailySyncAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_DAILY_SYNC);
        long triggerAtMillis = System.currentTimeMillis() + (24L * 60L * 60L * 1000L);
        scheduleExactAlarm(context, triggerAtMillis, intent, 9999);
    }

    public static void cancelAlarm(Context context, Intent intent, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        alarmManager.cancel(pendingIntent);
    }
}
