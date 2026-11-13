package com.codearena.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Synchronizes account profile data and user-facing preferences with users/{uid}.
 * Local database records and API credentials are intentionally not uploaded.
 */
public class FirestoreSyncRepository {

    private static final String TAG = "FirestoreSync";
    private static final String COLLECTION_USERS = "users";
    private static final long UPLOAD_DEBOUNCE_MS = 750L;
    private static final Handler SYNC_HANDLER = new Handler(Looper.getMainLooper());
    private static Runnable pendingUpload;

    public interface SyncCallback {
        void onComplete(boolean success);
    }

    public static void scheduleUpload(Context context) {
        Context appContext = context.getApplicationContext();
        synchronized (FirestoreSyncRepository.class) {
            if (pendingUpload != null) SYNC_HANDLER.removeCallbacks(pendingUpload);
            pendingUpload = () -> {
                synchronized (FirestoreSyncRepository.class) {
                    pendingUpload = null;
                }
                uploadToFirestore(appContext, null);
            };
            SYNC_HANDLER.postDelayed(pendingUpload, UPLOAD_DEBOUNCE_MS);
        }
    }

    public static void uploadToFirestore(Context context) {
        uploadToFirestore(context, null);
    }

    private static void uploadToFirestore(Context context, SyncCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getUid() == null) {
            Log.d(TAG, "No logged-in user, skipping Firestore upload");
            if (callback != null) callback.onComplete(false);
            return;
        }

        UserPreferencesRepository prefs = new UserPreferencesRepository(context);
        String uid = currentUser.getUid();

        Map<String, Object> handles = new HashMap<>();
        handles.put("codeforces", prefs.getHandle());
        handles.put("leetcode", prefs.getLeetCodeHandle());
        handles.put("codechef", prefs.getCodeChefHandle());
        handles.put("atcoder", prefs.getAtCoderHandle());
        handles.put("hackerrank", prefs.getHackerRankHandle());
        handles.put("cses", prefs.getCsesHandle());

        Map<String, Object> solvedCounts = new HashMap<>();
        solvedCounts.put("codeforces", prefs.getCfSolvedCount());
        solvedCounts.put("leetcode", prefs.getLeetCodeSolvedCount());
        solvedCounts.put("codechef", prefs.getCodeChefSolvedCount());
        solvedCounts.put("atcoder", prefs.getAtCoderSolvedCount());
        solvedCounts.put("hackerrank", prefs.getHackerRankSolvedCount());
        solvedCounts.put("cses", prefs.getCsesSolvedCount());

        Map<String, Object> ratings = new HashMap<>();
        ratings.put("codeforces", prefs.getUserRating());
        ratings.put("leetcode", prefs.getLeetCodeRating());
        ratings.put("codechef", prefs.getCodeChefRating());
        ratings.put("atcoder", prefs.getAtCoderRating());

        Map<String, Object> maxRatings = new HashMap<>();
        maxRatings.put("leetcode", prefs.getLeetCodeMaxRating());
        maxRatings.put("codechef", prefs.getCodeChefMaxRating());
        maxRatings.put("atcoder", prefs.getAtCoderMaxRating());

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("dailyTarget", prefs.getDailyTarget());
        preferences.put("notificationsEnabled", prefs.isNotificationsEnabled());
        preferences.put("milestoneAlertsEnabled", prefs.isMilestoneAlertsEnabled());
        preferences.put("fastReviewDebugMode", prefs.isFastReviewDebugMode());
        preferences.put("theme", prefs.getTheme());
        preferences.put("readerDarkTheme", prefs.isReaderDarkTheme());

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("handles", handles);
        userData.put("solvedCounts", solvedCounts);
        userData.put("ratings", ratings);
        userData.put("maxRatings", maxRatings);
        userData.put("friends", new ArrayList<>(prefs.getFriends()));
        userData.put("readBlogIds", new ArrayList<>(prefs.getReadBlogIds()));
        userData.put("readCatalogIds", new ArrayList<>(prefs.getReadCatalogIds()));
        userData.put("preferences", preferences);
        userData.put("lastUpdated", System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection(COLLECTION_USERS)
                .document(uid)
                .set(userData)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Profile synced for UID: " + uid);
                    if (callback != null) callback.onComplete(true);
                })
                .addOnFailureListener(error -> {
                    Log.e(TAG, "Failed to sync profile", error);
                    if (callback != null) callback.onComplete(false);
                });
    }

    public static void pullFromFirestore(Context context, SyncCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getUid() == null) {
            if (callback != null) callback.onComplete(false);
            return;
        }

        String uid = currentUser.getUid();
        DocumentReference userDoc = FirebaseFirestore.getInstance()
                .collection(COLLECTION_USERS)
                .document(uid);

        userDoc.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Exception error = task.getException();
                Log.e(TAG, "Failed to read profile from Firestore", error);
                if (callback != null) callback.onComplete(false);
                return;
            }

            DocumentSnapshot doc = task.getResult();
            if (!doc.exists()) {
                // Initialize the remote document only after a successful read confirmed it is absent.
                uploadToFirestore(context, callback);
                return;
            }

            restoreProfile(context, doc);
            Log.d(TAG, "Profile restored for UID: " + uid);
            if (callback != null) callback.onComplete(true);
        });
    }

    private static void restoreProfile(Context context, DocumentSnapshot doc) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(
                UserPreferencesRepository.getPrefName(),
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = prefs.edit();

        Map<String, Object> handles = asMap(doc.get("handles"));
        restoreString(editor, doc, handles, "codeforces", "codeforcesHandle", UserPreferencesRepository.KEY_HANDLE);
        restoreString(editor, doc, handles, "leetcode", "leetcodeHandle", UserPreferencesRepository.KEY_HANDLE_LEETCODE);
        restoreString(editor, doc, handles, "codechef", "codechefHandle", UserPreferencesRepository.KEY_HANDLE_CODECHEF);
        restoreString(editor, doc, handles, "atcoder", "atcoderHandle", UserPreferencesRepository.KEY_HANDLE_ATCODER);
        restoreString(editor, doc, handles, "hackerrank", "hackerrankHandle", UserPreferencesRepository.KEY_HANDLE_HACKERRANK);
        restoreString(editor, doc, handles, "cses", "csesHandle", UserPreferencesRepository.KEY_HANDLE_CSES);

        restoreCount(editor, doc, "codeforces", UserPreferencesRepository.KEY_CF_SOLVED_COUNT);
        restoreCount(editor, doc, "leetcode", UserPreferencesRepository.KEY_LC_SOLVED_COUNT);
        restoreCount(editor, doc, "codechef", UserPreferencesRepository.KEY_CC_SOLVED_COUNT);
        restoreCount(editor, doc, "atcoder", UserPreferencesRepository.KEY_ATCODER_SOLVED_COUNT);
        restoreCount(editor, doc, "hackerrank", UserPreferencesRepository.KEY_HR_SOLVED_COUNT);
        restoreCount(editor, doc, "cses", UserPreferencesRepository.KEY_CSES_SOLVED_COUNT);

        Map<String, Object> ratings = asMap(doc.get("ratings"));
        restoreMetric(editor, doc, ratings, "codeforces", "rating", UserPreferencesRepository.KEY_USER_RATING);
        restoreMetric(editor, doc, ratings, "leetcode", null, UserPreferencesRepository.KEY_LC_RATING);
        restoreMetric(editor, doc, ratings, "codechef", null, UserPreferencesRepository.KEY_CC_RATING);
        restoreMetric(editor, doc, ratings, "atcoder", null, UserPreferencesRepository.KEY_ATCODER_RATING);

        Map<String, Object> maxRatings = asMap(doc.get("maxRatings"));
        restoreMetric(editor, doc, maxRatings, "leetcode", null, UserPreferencesRepository.KEY_LC_MAX_RATING);
        restoreMetric(editor, doc, maxRatings, "codechef", null, UserPreferencesRepository.KEY_CC_MAX_RATING);
        restoreMetric(editor, doc, maxRatings, "atcoder", null, UserPreferencesRepository.KEY_ATCODER_MAX_RATING);

        restoreSet(editor, doc.get("friends"), UserPreferencesRepository.KEY_FRIENDS);
        restoreSet(editor, doc.get("readBlogIds"), UserPreferencesRepository.KEY_READ_BLOGS);
        restoreSet(editor, doc.get("readCatalogIds"), UserPreferencesRepository.KEY_READ_CATALOG);

        Map<String, Object> settings = asMap(doc.get("preferences"));
        putIntIfPresent(editor, settings, "dailyTarget", UserPreferencesRepository.KEY_DAILY_TARGET);
        putBooleanIfPresent(editor, settings, "notificationsEnabled", UserPreferencesRepository.KEY_NOTIFICATIONS_ENABLED);
        putBooleanIfPresent(editor, settings, "milestoneAlertsEnabled", UserPreferencesRepository.KEY_MILESTONE_ALERTS);
        putBooleanIfPresent(editor, settings, "fastReviewDebugMode", UserPreferencesRepository.KEY_FAST_REVIEW_DEBUG);
        putStringIfPresent(editor, settings, "theme", UserPreferencesRepository.KEY_THEME);
        putBooleanIfPresent(editor, settings, "readerDarkTheme", UserPreferencesRepository.KEY_READER_DARK_THEME);

        editor.apply();
    }

    private static void restoreString(SharedPreferences.Editor editor, DocumentSnapshot doc,
                                      Map<String, Object> handles, String mapKey, String legacyKey,
                                      String preferenceKey) {
        String value = doc.getString(legacyKey);
        if (value == null && handles.get(mapKey) instanceof String) {
            value = (String) handles.get(mapKey);
        }
        if (value != null) editor.putString(preferenceKey, value);
    }

    private static void restoreCount(SharedPreferences.Editor editor, DocumentSnapshot doc,
                                    String key, String preferenceKey) {
        Map<String, Object> counts = asMap(doc.get("solvedCounts"));
        Object value = counts.get(key);
        if (value instanceof Number) editor.putInt(preferenceKey, ((Number) value).intValue());
    }

    private static void restoreMetric(SharedPreferences.Editor editor, DocumentSnapshot doc,
                                      Map<String, Object> values, String key, String legacyKey,
                                      String preferenceKey) {
        Object value = values.get(key);
        if (value == null && legacyKey != null) value = doc.get(legacyKey);
        if (value instanceof Number) editor.putInt(preferenceKey, ((Number) value).intValue());
    }

    private static void restoreSet(SharedPreferences.Editor editor, Object value, String preferenceKey) {
        if (!(value instanceof List)) return;
        Set<String> restored = new HashSet<>();
        for (Object item : (List<?>) value) {
            if (item instanceof String || item instanceof Number) {
                restored.add(String.valueOf(item));
            }
        }
        editor.putStringSet(preferenceKey, restored);
    }

    private static void putIntIfPresent(SharedPreferences.Editor editor, Map<String, Object> values,
                                        String field, String preferenceKey) {
        Object value = values.get(field);
        if (value instanceof Number) editor.putInt(preferenceKey, ((Number) value).intValue());
    }

    private static void putBooleanIfPresent(SharedPreferences.Editor editor, Map<String, Object> values,
                                            String field, String preferenceKey) {
        Object value = values.get(field);
        if (value instanceof Boolean) editor.putBoolean(preferenceKey, (Boolean) value);
    }

    private static void putStringIfPresent(SharedPreferences.Editor editor, Map<String, Object> values,
                                          String field, String preferenceKey) {
        Object value = values.get(field);
        if (value instanceof String) editor.putString(preferenceKey, (String) value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : new HashMap<>();
    }
}
