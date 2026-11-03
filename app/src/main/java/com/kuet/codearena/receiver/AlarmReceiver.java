package com.kuet.codearena.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.kuet.codearena.R;
import com.kuet.codearena.data.repository.SyncRepository;
import com.kuet.codearena.ui.MainActivity;
import com.kuet.codearena.util.AppExecutors;

/**
 * BroadcastReceiver executing AlarmManager scheduled alerts and performing asynchronous background sync via goAsync.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    public static final String ACTION_CONTEST_REMINDER = "com.kuet.codearena.action.CONTEST_REMINDER";
    public static final String ACTION_UPSOLVE_REMINDER = "com.kuet.codearena.action.UPSOLVE_REMINDER";
    public static final String ACTION_DAILY_SYNC = "com.kuet.codearena.action.DAILY_SYNC";

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String EXTRA_CONTEST_ID = "extra_contest_id";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    private static final String CHANNEL_ID_REMINDERS = "channel_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();
        Log.d(TAG, "Alarm triggered: " + action);

        if (ACTION_DAILY_SYNC.equals(action)) {
            // Background work with AlarmManager using goAsync() and ExecutorService
            final PendingResult pendingResult = goAsync();
            AppExecutors.getInstance().networkIO().execute(() -> {
                try {
                    Log.d(TAG, "Executing asynchronous daily solve sync via goAsync()...");
                    SyncRepository.getInstance(context).syncAllPlatformsDirectly();
                } catch (Exception e) {
                    Log.e(TAG, "Error in daily sync receiver", e);
                } finally {
                    pendingResult.finish();
                }
            });
            return;
        }

        // Contest or Upsolve notifications
        String title = intent.getStringExtra(EXTRA_TITLE);
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, (int) System.currentTimeMillis());

        if (title == null) title = "CodeArena Alert";
        if (message == null) message = "You have a scheduled competitive programming activity.";

        showNotification(context, title, message, notificationId);
    }

    private void showNotification(Context context, String title, String message, int notificationId) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_REMINDERS,
                    "CodeArena Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminders for upcoming contests and due upsolves");
            nm.createNotificationChannel(channel);
        }

        Intent openAppIntent = new Intent(context, MainActivity.class);
        int flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, openAppIntent, flag);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        nm.notify(notificationId, builder.build());
    }
}
