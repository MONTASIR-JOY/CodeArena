package com.kuet.codearena.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences manager storing user platform handles, preferences, and active run sessions.
 */
public class PreferenceHelper {
    private static final String PREF_NAME = "codearena_preferences";

    private static final String KEY_CF_HANDLE = "key_cf_handle";
    private static final String KEY_ATCODER_HANDLE = "key_atcoder_handle";
    private static final String KEY_LEETCODE_HANDLE = "key_leetcode_handle";
    private static final String KEY_DAILY_TARGET = "key_daily_target";
    private static final String KEY_PRACTICE_RATING = "key_practice_rating";

    private static final String KEY_ACTIVE_RUN_ID = "key_active_run_id";
    private static final String KEY_ACTIVE_CONTEST_ID = "key_active_contest_id";
    private static final String KEY_ACTIVE_CONTEST_NAME = "key_active_contest_name";
    private static final String KEY_ACTIVE_START_TIME_MS = "key_active_start_time_ms";
    private static final String KEY_ACTIVE_DURATION_MS = "key_active_duration_ms";

    private final SharedPreferences prefs;

    public PreferenceHelper(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setCfHandle(String handle) {
        prefs.edit().putString(KEY_CF_HANDLE, handle).apply();
    }

    public String getCfHandle() {
        return prefs.getString(KEY_CF_HANDLE, "tourist"); // default demo handle
    }

    public void setAtCoderHandle(String handle) {
        prefs.edit().putString(KEY_ATCODER_HANDLE, handle).apply();
    }

    public String getAtCoderHandle() {
        return prefs.getString(KEY_ATCODER_HANDLE, "");
    }

    public void setLeetCodeHandle(String handle) {
        prefs.edit().putString(KEY_LEETCODE_HANDLE, handle).apply();
    }

    public String getLeetCodeHandle() {
        return prefs.getString(KEY_LEETCODE_HANDLE, "");
    }

    public void setDailyTarget(int target) {
        prefs.edit().putInt(KEY_DAILY_TARGET, target).apply();
    }

    public int getDailyTarget() {
        return prefs.getInt(KEY_DAILY_TARGET, 2); // default 2 upsolves
    }

    public void setPracticeRating(double rating) {
        prefs.edit().putFloat(KEY_PRACTICE_RATING, (float) rating).apply();
    }

    public double getPracticeRating() {
        return prefs.getFloat(KEY_PRACTICE_RATING, 800.0f); // default 800
    }

    public void saveActiveRun(long runId, int contestId, String contestName, long startTimeMs, long durationMs) {
        prefs.edit()
                .putLong(KEY_ACTIVE_RUN_ID, runId)
                .putInt(KEY_ACTIVE_CONTEST_ID, contestId)
                .putString(KEY_ACTIVE_CONTEST_NAME, contestName)
                .putLong(KEY_ACTIVE_START_TIME_MS, startTimeMs)
                .putLong(KEY_ACTIVE_DURATION_MS, durationMs)
                .apply();
    }

    public void clearActiveRun() {
        prefs.edit()
                .remove(KEY_ACTIVE_RUN_ID)
                .remove(KEY_ACTIVE_CONTEST_ID)
                .remove(KEY_ACTIVE_CONTEST_NAME)
                .remove(KEY_ACTIVE_START_TIME_MS)
                .remove(KEY_ACTIVE_DURATION_MS)
                .apply();
    }

    public boolean hasActiveRun() {
        return prefs.getLong(KEY_ACTIVE_RUN_ID, -1) != -1;
    }

    public long getActiveRunId() {
        return prefs.getLong(KEY_ACTIVE_RUN_ID, -1);
    }

    public int getActiveContestId() {
        return prefs.getInt(KEY_ACTIVE_CONTEST_ID, -1);
    }

    public String getActiveContestName() {
        return prefs.getString(KEY_ACTIVE_CONTEST_NAME, "");
    }

    public long getActiveStartTimeMs() {
        return prefs.getLong(KEY_ACTIVE_START_TIME_MS, 0);
    }

    public long getActiveDurationMs() {
        return prefs.getLong(KEY_ACTIVE_DURATION_MS, 0);
    }
}
