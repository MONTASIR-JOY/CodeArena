package com.codearena.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receiver for timer action buttons (Pause, Resume, Stop) from the notification.
 * This stub just logs the received action.
 */
public class TimerActionReceiver extends BroadcastReceiver {
    private static final String TAG = "TimerActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : "null";
        Log.i(TAG, "Received timer action: " + action);
        // In a full implementation you would forward the action to ContestTimerService.
    }
}

