package com.codearena.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receiver that fires when the device boots.
 * It should reschedule any alarms (contests, upsolve reminders).
 * This placeholder just logs the event.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Device boot completed – would reschedule alarms here.");
        // Real implementation would enqueue WorkManager jobs or AlarmManager repeats.
    }
}

