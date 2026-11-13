package com.codearena.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.util.Log;
import com.codearena.R;
import com.codearena.data.repository.UserPreferencesRepository;

/**
 * Daily Widget Provider displaying CodeArena user status and upsolve targets on the Home Screen.
 */
public class DailyWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "DailyWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        UserPreferencesRepository repo = new UserPreferencesRepository(context);
        String handle = repo.getHandle();
        if (handle == null || handle.trim().isEmpty()) {
            handle = "Codeforces User";
        }

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_daily);
            views.setTextViewText(R.id.widget_title, "CodeArena Daily Target");
            views.setTextViewText(R.id.widget_handle, handle);
            views.setTextViewText(R.id.widget_progress_text, "🎯 0/1 solved today");

            Intent intent = new Intent(context, com.codearena.MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        Log.i(TAG, "DailyWidgetProvider updated " + appWidgetIds.length + " instances");
    }
}
