package com.kuet.codearena.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.kuet.codearena.R;
import com.kuet.codearena.data.repository.VirtualRunRepository;
import com.kuet.codearena.ui.VirtualRunActivity;
import com.kuet.codearena.util.PreferenceHelper;

import java.util.Locale;

/**
 * Foreground Service maintaining virtual contest countdown, periodic submission polling, and status notifications.
 */
public class VirtualContestService extends Service {
    private static final String TAG = "VirtualContestService";

    public static final String ACTION_START = "com.kuet.codearena.action.START_RUN";
    public static final String ACTION_STOP = "com.kuet.codearena.action.STOP_RUN";
    public static final String ACTION_TICK_BROADCAST = "com.kuet.codearena.action.TICK";

    public static final String EXTRA_RUN_ID = "extra_run_id";
    public static final String EXTRA_CONTEST_ID = "extra_contest_id";
    public static final String EXTRA_CONTEST_NAME = "extra_contest_name";
    public static final String EXTRA_START_TIME_MS = "extra_start_time_ms";
    public static final String EXTRA_DURATION_MS = "extra_duration_ms";
    public static final String EXTRA_REMAINING_MS = "extra_remaining_ms";
    public static final String EXTRA_ELAPSED_MS = "extra_elapsed_ms";
    public static final String EXTRA_ALERT = "extra_alert";

    private static final String CHANNEL_ID = "channel_virtual_contest";
    private static final int NOTIFICATION_ID = 1001;
    private static final int ALERT_NOTIFICATION_ID = 1002;

    private NotificationManager notificationManager;
    private Handler timerHandler;
    private Runnable timerRunnable;

    private long runId;
    private int contestId;
    private String contestName;
    private long startTimeMs;
    private long durationMs;
    private String userHandle;

    private boolean alertedHalfway = false;
    private boolean alertedTenMinutes = false;
    private int tickCount = 0;

    private VirtualRunRepository runRepository;
    private PreferenceHelper preferenceHelper;

    public static Intent createStartIntent(Context context, long runId, int contestId, String contestName, long startTimeMs, long durationMs) {
        Intent intent = new Intent(context, VirtualContestService.class);
        intent.setAction(ACTION_START);
        intent.putExtra(EXTRA_RUN_ID, runId);
        intent.putExtra(EXTRA_CONTEST_ID, contestId);
        intent.putExtra(EXTRA_CONTEST_NAME, contestName);
        intent.putExtra(EXTRA_START_TIME_MS, startTimeMs);
        intent.putExtra(EXTRA_DURATION_MS, durationMs);
        return intent;
    }

    public static Intent createStopIntent(Context context) {
        Intent intent = new Intent(context, VirtualContestService.class);
        intent.setAction(ACTION_STOP);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        runRepository = VirtualRunRepository.getInstance(this);
        preferenceHelper = new PreferenceHelper(this);
        timerHandler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopRunningService();
            return START_NOT_STICKY;
        }

        if (intent != null && ACTION_START.equals(intent.getAction())) {
            runId = intent.getLongExtra(EXTRA_RUN_ID, -1);
            contestId = intent.getIntExtra(EXTRA_CONTEST_ID, -1);
            contestName = intent.getStringExtra(EXTRA_CONTEST_NAME);
            startTimeMs = intent.getLongExtra(EXTRA_START_TIME_MS, System.currentTimeMillis());
            durationMs = intent.getLongExtra(EXTRA_DURATION_MS, 7200000L);
            userHandle = preferenceHelper.getCfHandle();

            preferenceHelper.saveActiveRun(runId, contestId, contestName, startTimeMs, durationMs);
            startForeground(NOTIFICATION_ID, buildOngoingNotification(durationMs));
            startTimerLoop();
        }

        return START_STICKY;
    }

    private void startTimerLoop() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                // Time is ALWAYS calculated from the saved startTimeMs, never from an in-memory counter
                long now = System.currentTimeMillis();
                long elapsedMs = now - startTimeMs;
                long remainingMs = Math.max(0, durationMs - elapsedMs);

                // Broadcast tick
                broadcastTick(remainingMs, elapsedMs, null);

                // Update ongoing notification every 5 seconds or when under 1 minute
                if (tickCount % 5 == 0 || remainingMs <= 60000) {
                    notificationManager.notify(NOTIFICATION_ID, buildOngoingNotification(remainingMs));
                }

                // Check alerts
                checkAlerts(elapsedMs, remainingMs);

                // Submission check every 15 seconds (15 ticks)
                if (tickCount % 15 == 0 && userHandle != null && !userHandle.isEmpty()) {
                    runRepository.checkSubmissions(runId, contestId, startTimeMs, userHandle);
                }

                tickCount++;

                if (remainingMs > 0) {
                    timerHandler.postDelayed(this, 1000);
                } else {
                    onContestFinished();
                }
            }
        };

        timerHandler.post(timerRunnable);
    }

    private void checkAlerts(long elapsedMs, long remainingMs) {
        long halfwayMs = durationMs / 2;
        long tenMinMs = 10 * 60 * 1000L;

        if (!alertedHalfway && elapsedMs >= halfwayMs && remainingMs > tenMinMs) {
            alertedHalfway = true;
            showMilestoneNotification("Halfway Point Reached!", "You are 50% through " + contestName + ". Keep up the pace!");
            broadcastTick(remainingMs, elapsedMs, "HALFWAY");
        }

        if (!alertedTenMinutes && remainingMs <= tenMinMs && remainingMs > 0) {
            alertedTenMinutes = true;
            showMilestoneNotification("10 Minutes Remaining!", "Final stretch! Wrap up your current problem submission.");
            broadcastTick(remainingMs, elapsedMs, "TEN_MINUTES");
        }
    }

    private void broadcastTick(long remainingMs, long elapsedMs, @Nullable String alert) {
        Intent tickIntent = new Intent(ACTION_TICK_BROADCAST);
        tickIntent.putExtra(EXTRA_RUN_ID, runId);
        tickIntent.putExtra(EXTRA_REMAINING_MS, remainingMs);
        tickIntent.putExtra(EXTRA_ELAPSED_MS, elapsedMs);
        tickIntent.putExtra(EXTRA_DURATION_MS, durationMs);
        if (alert != null) {
            tickIntent.putExtra(EXTRA_ALERT, alert);
        }
        sendBroadcast(tickIntent);
    }

    private void onContestFinished() {
        showMilestoneNotification("Contest Finished!", "Time is up for " + contestName + ". Reviewing post-mortem.");
        broadcastTick(0, durationMs, "FINISHED");
        runRepository.finishRun(runId, new VirtualRunRepository.RunCallback<com.kuet.codearena.data.db.entity.RunEntity>() {
            @Override
            public void onSuccess(com.kuet.codearena.data.db.entity.RunEntity result) {
                stopRunningService();
            }

            @Override
            public void onError(String error) {
                stopRunningService();
            }
        });
    }

    private void stopRunningService() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        preferenceHelper.clearActiveRun();
        stopForeground(true);
        stopSelf();
    }

    private Notification buildOngoingNotification(long remainingMs) {
        Intent openActivityIntent = new Intent(this, VirtualRunActivity.class);
        openActivityIntent.putExtra(VirtualRunActivity.EXTRA_RUN_ID, runId);
        openActivityIntent.putExtra(VirtualRunActivity.EXTRA_CONTEST_ID, contestId);
        openActivityIntent.putExtra(VirtualRunActivity.EXTRA_CONTEST_NAME, contestName);
        openActivityIntent.putExtra(VirtualRunActivity.EXTRA_DURATION_SECONDS, durationMs / 1000);

        int flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openActivityIntent, flag);

        long hours = remainingMs / 3600000;
        long minutes = (remainingMs % 3600000) / 60000;
        long seconds = (remainingMs % 60000) / 1000;
        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(contestName != null ? contestName : "Virtual Contest")
                .setContentText("Time remaining: " + formattedTime)
                .setSmallIcon(R.drawable.ic_timer)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private void showMilestoneNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(ALERT_NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Virtual Contest Running",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Ongoing timer for live virtual contest sessions");
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
