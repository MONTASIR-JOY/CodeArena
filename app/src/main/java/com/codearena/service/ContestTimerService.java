package com.codearena.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.codearena.R;

/**
 * Very simple foreground service placeholder for the virtual contest timer.
 * In a full implementation you would keep a running countdown, update a notification
 * and broadcast timer actions. Here we just start a foreground notification so the
 * service can be started without the system killing it.
 */
public class ContestTimerService extends Service {
    private static final String CHANNEL_ID = "contest_timer_channel";
    private static final int NOTIF_ID = 101;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Contest Timer")
                .setContentText("Timer is running…")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
        startForeground(NOTIF_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // No real timer logic – placeholder only
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cleanup if needed
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Contest Timer",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Foreground service for virtual contest timing");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
