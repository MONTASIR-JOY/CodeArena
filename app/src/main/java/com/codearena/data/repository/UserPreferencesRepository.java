package com.codearena.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * UserPreferencesRepository: Fast, persistent, thread-safe user preferences using Android SharedPreferences,
 * isolated per logged-in Firebase user account.
 */
public class UserPreferencesRepository {

    private static final String PREF_NAME_PREFIX = "codearena_preferences_";

    // Preference keys
    public static final String KEY_HANDLE = "user_handle";
    public static final String KEY_HANDLE_LEETCODE = "user_handle_leetcode";
    public static final String KEY_HANDLE_CODECHEF = "user_handle_codechef";
    public static final String KEY_HANDLE_ATCODER = "user_handle_atcoder";
    public static final String KEY_HANDLE_HACKERRANK = "user_handle_hackerrank";
    public static final String KEY_CF_SOLVED_COUNT = "cf_solved_count";
    public static final String KEY_LC_SOLVED_COUNT = "lc_solved_count";
    public static final String KEY_CC_SOLVED_COUNT = "cc_solved_count";
    public static final String KEY_ATCODER_SOLVED_COUNT = "atcoder_solved_count";
    public static final String KEY_HR_SOLVED_COUNT = "hr_solved_count";
    public static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String KEY_MILESTONE_ALERTS = "milestone_alerts_enabled";
    public static final String KEY_FAST_REVIEW_DEBUG = "fast_review_debug_mode";
    public static final String KEY_THEME = "app_theme";
    public static final String KEY_ONBOARDING_DONE = "onboarding_completed";
    public static final String KEY_MASTERY_SYNCED_AT = "mastery_synced_at";
    public static final String KEY_DAILY_SIZE = "daily_challenge_size";
    public static final String KEY_DAILY_DATA = "daily_challenge_data";
    public static final String KEY_FRIENDS = "friend_handles";
    public static final String KEY_READ_CATALOG = "read_catalog_items";
    public static final String KEY_READ_BLOGS = "read_blog_ids";
    public static final String KEY_CACHED_BLOGS = "cached_blogs_json";
    public static final String KEY_CACHED_UPCOMING_CONTESTS = "cached_upcoming_contests_json";
    public static final String KEY_DAILY_TARGET = "daily_upsolve_target";
    public static final String KEY_USER_RATING = "user_cf_rating";
    public static final String KEY_USER_SOLVED_PROBLEM_KEYS = "user_solved_problem_keys";

    private final Context context;
    private SharedPreferences prefs;
    private String currentPrefName;

    private final MutableLiveData<String> handleLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> leetcodeHandleLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> codechefHandleLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> atcoderHandleLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> hackerrankHandleLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> themeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> notificationsEnabledLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> milestoneAlertsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> fastReviewDebugLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> dailySizeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> friendsLiveData = new MutableLiveData<>();

    public static String getPrefName() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getUid() != null && !user.getUid().isEmpty()) {
                return PREF_NAME_PREFIX + user.getUid();
            }
        } catch (Exception ignored) {}
        return PREF_NAME_PREFIX + "guest";
    }

    public UserPreferencesRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = getPrefs();
    }

    private synchronized SharedPreferences getPrefs() {
        String needed = getPrefName();
        if (prefs == null || !needed.equals(currentPrefName)) {
            currentPrefName = needed;
            prefs = context.getSharedPreferences(needed, Context.MODE_PRIVATE);
            updateLiveData();
            prefs.registerOnSharedPreferenceChangeListener((sp, key) -> updateLiveData());
        }
        return prefs;
    }

    private void updateLiveData() {
        handleLiveData.postValue(getHandle());
        leetcodeHandleLiveData.postValue(getLeetCodeHandle());
        codechefHandleLiveData.postValue(getCodeChefHandle());
        atcoderHandleLiveData.postValue(getAtCoderHandle());
        hackerrankHandleLiveData.postValue(getHackerRankHandle());
        themeLiveData.postValue(getTheme());
        notificationsEnabledLiveData.postValue(isNotificationsEnabled());
        milestoneAlertsLiveData.postValue(isMilestoneAlertsEnabled());
        fastReviewDebugLiveData.postValue(isFastReviewDebugMode());
        dailySizeLiveData.postValue(getDailySize());
        friendsLiveData.postValue(getFriends());
    }

    // ----- Handles -----
    public String getHandle() {
        return getPrefs().getString(KEY_HANDLE, "");
    }

    public void saveHandle(String handle) {
        getPrefs().edit().putString(KEY_HANDLE, handle != null ? handle.trim() : "").apply();
    }

    public LiveData<String> getHandleLiveData() {
        return handleLiveData;
    }

    public String getLeetCodeHandle() {
        return getPrefs().getString(KEY_HANDLE_LEETCODE, "");
    }

    public void saveLeetCodeHandle(String handle) {
        getPrefs().edit().putString(KEY_HANDLE_LEETCODE, handle != null ? handle.trim() : "").apply();
    }

    public LiveData<String> getLeetCodeHandleLiveData() {
        return leetcodeHandleLiveData;
    }

    public String getCodeChefHandle() {
        return getPrefs().getString(KEY_HANDLE_CODECHEF, "");
    }

    public void saveCodeChefHandle(String handle) {
        getPrefs().edit().putString(KEY_HANDLE_CODECHEF, handle != null ? handle.trim() : "").apply();
    }

    public LiveData<String> getCodeChefHandleLiveData() {
        return codechefHandleLiveData;
    }

    public String getAtCoderHandle() {
        return getPrefs().getString(KEY_HANDLE_ATCODER, "");
    }

    public void saveAtCoderHandle(String handle) {
        getPrefs().edit().putString(KEY_HANDLE_ATCODER, handle != null ? handle.trim() : "").apply();
    }

    public LiveData<String> getAtCoderHandleLiveData() {
        return atcoderHandleLiveData;
    }

    public String getHackerRankHandle() {
        return getPrefs().getString(KEY_HANDLE_HACKERRANK, "");
    }

    public void saveHackerRankHandle(String handle) {
        getPrefs().edit().putString(KEY_HANDLE_HACKERRANK, handle != null ? handle.trim() : "").apply();
    }

    public LiveData<String> getHackerRankHandleLiveData() {
        return hackerrankHandleLiveData;
    }

    // ----- Solve counts -----
    public int getLeetCodeSolvedCount() {
        return getPrefs().getInt(KEY_LC_SOLVED_COUNT, 0);
    }

    public void saveLeetCodeSolvedCount(int count) {
        getPrefs().edit().putInt(KEY_LC_SOLVED_COUNT, count).apply();
    }

    public int getCodeChefSolvedCount() {
        return getPrefs().getInt(KEY_CC_SOLVED_COUNT, 0);
    }

    public void saveCodeChefSolvedCount(int count) {
        getPrefs().edit().putInt(KEY_CC_SOLVED_COUNT, count).apply();
    }

    public int getAtCoderSolvedCount() {
        return getPrefs().getInt(KEY_ATCODER_SOLVED_COUNT, 0);
    }

    public void saveAtCoderSolvedCount(int count) {
        getPrefs().edit().putInt(KEY_ATCODER_SOLVED_COUNT, count).apply();
    }

    public int getHackerRankSolvedCount() {
        return getPrefs().getInt(KEY_HR_SOLVED_COUNT, 0);
    }

    public void saveHackerRankSolvedCount(int count) {
        getPrefs().edit().putInt(KEY_HR_SOLVED_COUNT, count).apply();
    }

    public int getCfSolvedCount() {
        return getPrefs().getInt(KEY_CF_SOLVED_COUNT, 0);
    }

    public void saveCfSolvedCount(int count) {
        getPrefs().edit().putInt(KEY_CF_SOLVED_COUNT, count).apply();
    }

    // ----- Misc settings -----
    public int getDailyTarget() {
        return getPrefs().getInt(KEY_DAILY_TARGET, 3);
    }

    public void saveDailyTarget(int target) {
        getPrefs().edit().putInt(KEY_DAILY_TARGET, target).apply();
    }

    public boolean isNotificationsEnabled() {
        return getPrefs().getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public LiveData<Boolean> getNotificationsEnabledLiveData() {
        return notificationsEnabledLiveData;
    }

    public boolean isMilestoneAlertsEnabled() {
        return getPrefs().getBoolean(KEY_MILESTONE_ALERTS, true);
    }

    public void setMilestoneAlertsEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_MILESTONE_ALERTS, enabled).apply();
    }

    public LiveData<Boolean> getMilestoneAlertsLiveData() {
        return milestoneAlertsLiveData;
    }

    public boolean isFastReviewDebugMode() {
        return getPrefs().getBoolean(KEY_FAST_REVIEW_DEBUG, false);
    }

    public void setFastReviewDebugMode(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_FAST_REVIEW_DEBUG, enabled).apply();
    }

    public LiveData<Boolean> getFastReviewDebugLiveData() {
        return fastReviewDebugLiveData;
    }

    public String getTheme() {
        return getPrefs().getString(KEY_THEME, "dark");
    }

    public void setTheme(String theme) {
        getPrefs().edit().putString(KEY_THEME, theme).apply();
    }

    public void saveTheme(String theme) {
        setTheme(theme);
    }

    public LiveData<String> getThemeLiveData() {
        return themeLiveData;
    }

    public boolean isOnboardingDone() {
        return getPrefs().getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingDone(boolean done) {
        getPrefs().edit().putBoolean(KEY_ONBOARDING_DONE, done).apply();
    }

    public long getMasterySyncedAt() {
        return getPrefs().getLong(KEY_MASTERY_SYNCED_AT, 0L);
    }

    public void setMasterySyncedAt(long millis) {
        getPrefs().edit().putLong(KEY_MASTERY_SYNCED_AT, millis).apply();
    }

    public void markMasteryStale() {
        setMasterySyncedAt(0L);
    }

    public int getDailySize() {
        int s = getPrefs().getInt(KEY_DAILY_SIZE, 3);
        return Math.max(3, Math.min(10, s));
    }

    public void saveDailySize(int size) {
        getPrefs().edit().putInt(KEY_DAILY_SIZE, Math.max(3, Math.min(10, size))).apply();
    }

    public LiveData<Integer> getDailySizeLiveData() {
        return dailySizeLiveData;
    }

    public String getDailyData() {
        return getPrefs().getString(KEY_DAILY_DATA, "");
    }

    public void saveDailyData(String json) {
        getPrefs().edit().putString(KEY_DAILY_DATA, json).apply();
    }

    public int getUserRating() {
        return getPrefs().getInt(KEY_USER_RATING, 1200);
    }

    public void saveUserRating(int rating) {
        getPrefs().edit().putInt(KEY_USER_RATING, rating).apply();
    }

    public Set<String> getUserSolvedProblemKeys() {
        return getPrefs().getStringSet(KEY_USER_SOLVED_PROBLEM_KEYS, Collections.emptySet());
    }

    public void saveUserSolvedProblemKeys(Set<String> keys) {
        if (keys == null) return;
        getPrefs().edit().putStringSet(KEY_USER_SOLVED_PROBLEM_KEYS, new HashSet<>(keys)).apply();
    }

    public boolean isProblemSolvedByUser(Long contestId, String index) {
        if (contestId == null || index == null) return false;
        String idx = index.trim().toUpperCase();
        String k1 = contestId + idx;
        String k2 = contestId + "_" + idx;
        Set<String> solved = getUserSolvedProblemKeys();
        return solved.contains(k1) || solved.contains(k2);
    }

    public Set<String> getFriends() {
        return getPrefs().getStringSet(KEY_FRIENDS, Collections.emptySet());
    }

    public void saveFriends(Set<String> handles) {
        getPrefs().edit().putStringSet(KEY_FRIENDS, new HashSet<>(handles)).apply();
    }

    public void addFriend(String handle) {
        if (handle == null || handle.trim().isEmpty()) return;
        Set<String> current = new HashSet<>(getFriends());
        current.add(handle.trim());
        saveFriends(current);
    }

    public void removeFriend(String handle) {
        if (handle == null) return;
        Set<String> current = new HashSet<>(getFriends());
        current.remove(handle.trim());
        saveFriends(current);
    }

    public LiveData<Set<String>> getFriendsLiveData() {
        return friendsLiveData;
    }

    public Set<String> getReadCatalogIds() {
        return getPrefs().getStringSet(KEY_READ_CATALOG, Collections.emptySet());
    }

    public boolean isCatalogRead(long id) {
        return getReadCatalogIds().contains(String.valueOf(id));
    }

    public void setCatalogRead(long id, boolean read) {
        Set<String> current = new HashSet<>(getReadCatalogIds());
        if (read) {
            current.add(String.valueOf(id));
        } else {
            current.remove(String.valueOf(id));
        }
        getPrefs().edit().putStringSet(KEY_READ_CATALOG, current).apply();
    }

    public Set<String> getReadBlogIds() {
        return getPrefs().getStringSet(KEY_READ_BLOGS, Collections.emptySet());
    }

    public boolean isBlogRead(long id) {
        return getReadBlogIds().contains(String.valueOf(id));
    }

    public void setBlogRead(long id, boolean read) {
        Set<String> current = new HashSet<>(getReadBlogIds());
        if (read) {
            current.add(String.valueOf(id));
        } else {
            current.remove(String.valueOf(id));
        }
        getPrefs().edit().putStringSet(KEY_READ_BLOGS, current).apply();
    }

    public String getCachedBlogsJson() {
        return getPrefs().getString(KEY_CACHED_BLOGS, "");
    }

    public void setCachedBlogsJson(String json) {
        getPrefs().edit().putString(KEY_CACHED_BLOGS, json).apply();
    }

    public String getCachedUpcomingContestsJson() {
        return getPrefs().getString(KEY_CACHED_UPCOMING_CONTESTS, "");
    }

    public void setCachedUpcomingContestsJson(String json) {
        getPrefs().edit().putString(KEY_CACHED_UPCOMING_CONTESTS, json).apply();
    }

    public boolean isContestReminderSet(long contestId) {
        return getPrefs().getBoolean("contest_reminder_" + contestId, false);
    }

    public void setContestReminderSet(long contestId, boolean isSet) {
        getPrefs().edit().putBoolean("contest_reminder_" + contestId, isSet).apply();
    }
}
