package com.codearena;

import android.app.Application;
import android.util.Log;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.util.ThemeManager;

/**
 * Main Application class for CodeArena.
 * Applies the default dark theme on startup.
 */
public class CodeArenaApp extends Application {
    private static final String TAG = "CodeArenaApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Application onCreate – CodeArenaApp initialized");

        UserPreferencesRepository prefs = new UserPreferencesRepository(this);
        String theme = prefs.getTheme();
        if (theme == null || theme.trim().isEmpty()) {
            theme = "dark";
            prefs.saveTheme("dark");
        }
        ThemeManager.apply(theme);

        // Enqueue periodic background widget synchronization via WorkManager
        androidx.work.PeriodicWorkRequest widgetSyncRequest =
                new androidx.work.PeriodicWorkRequest.Builder(com.codearena.widget.WidgetSyncWorker.class, 4, java.util.concurrent.TimeUnit.HOURS)
                        .build();
        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "CodeArenaWidgetSync",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                widgetSyncRequest);
    }
}


