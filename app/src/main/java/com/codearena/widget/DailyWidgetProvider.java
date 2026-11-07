package com.codearena.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.util.Log;
import com.codearena.R;

/**
 * Simple placeholder widget that shows a static label.
 * In a full implementation you could show today’s contest or stats.
 */
public class DailyWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "DailyWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_daily);
            // Example: set a static text
            views.setTextViewText(R.id.widget_text, "CodeArena Daily");
            // Optionally set click to launch the app
            Intent intent = new Intent(context, com.codearena.MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        Log.i(TAG, "Widget updated " + appWidgetIds.length + " instances");
    }
}

