package com.codearena.data.repository;

import android.content.Context;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * FirestoreSyncRepository: Synchronizes user handles, ratings, and read bookmarks
 * directly with Cloud Firestore under collection 'users/{uid}'.
 */
public class FirestoreSyncRepository {

    private static final String TAG = "FirestoreSync";
    private static final String COLLECTION_USERS = "users";

    public interface SyncCallback {
        void onComplete(boolean success);
    }

    /**
     * Uploads the local handles and read-progress to Cloud Firestore under users/{uid}.
     */
    public static void uploadToFirestore(Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getUid() == null) {
            Log.d(TAG, "No logged-in user, skipping Firestore upload");
            return;
        }

        String uid = currentUser.getUid();
        UserPreferencesRepository prefs = new UserPreferencesRepository(context);

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", currentUser.getEmail() != null ? currentUser.getEmail() : "");
        userData.put("codeforcesHandle", prefs.getHandle());
        userData.put("leetcodeHandle", prefs.getLeetCodeHandle());
        userData.put("codechefHandle", prefs.getCodeChefHandle());
        userData.put("atcoderHandle", prefs.getAtCoderHandle());
        userData.put("hackerrankHandle", prefs.getHackerRankHandle());

        // Structured platform handles map for clear Firestore Console display
        Map<String, String> handlesMap = new HashMap<>();
        handlesMap.put("codeforces", prefs.getHandle());
        handlesMap.put("leetcode", prefs.getLeetCodeHandle());
        handlesMap.put("codechef", prefs.getCodeChefHandle());
        handlesMap.put("atcoder", prefs.getAtCoderHandle());
        handlesMap.put("hackerrank", prefs.getHackerRankHandle());
        userData.put("handles", handlesMap);

        // Platform solve counts
        Map<String, Integer> solvedCountsMap = new HashMap<>();
        solvedCountsMap.put("codeforces", prefs.getCfSolvedCount());
        solvedCountsMap.put("leetcode", prefs.getLeetCodeSolvedCount());
        solvedCountsMap.put("codechef", prefs.getCodeChefSolvedCount());
        solvedCountsMap.put("atcoder", prefs.getAtCoderSolvedCount());
        solvedCountsMap.put("hackerrank", prefs.getHackerRankSolvedCount());
        userData.put("solvedCounts", solvedCountsMap);

        userData.put("rating", prefs.getUserRating());
        userData.put("readBlogIds", new ArrayList<>(prefs.getReadBlogIds()));
        userData.put("readCatalogIds", new ArrayList<>(prefs.getReadCatalogIds()));
        userData.put("lastUpdated", System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(COLLECTION_USERS).document(uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully synced profile to Firestore: " + uid))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to sync profile to Firestore", e));
    }

    /**
     * Pulls the user's saved handles and read-progress from Firestore and populates UserPreferencesRepository.
     */
    public static void pullFromFirestore(Context context, SyncCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getUid() == null) {
            if (callback != null) callback.onComplete(false);
            return;
        }

        String uid = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDoc = db.collection(COLLECTION_USERS).document(uid);

        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot doc = task.getResult();
                UserPreferencesRepository prefs = new UserPreferencesRepository(context);

                Map<String, Object> handles = (Map<String, Object>) doc.get("handles");

                String cf = doc.getString("codeforcesHandle");
                if ((cf == null || cf.isEmpty()) && handles != null) cf = (String) handles.get("codeforces");
                if (cf != null && !cf.isEmpty()) prefs.saveHandle(cf);

                String lc = doc.getString("leetcodeHandle");
                if ((lc == null || lc.isEmpty()) && handles != null) lc = (String) handles.get("leetcode");
                if (lc != null && !lc.isEmpty()) prefs.saveLeetCodeHandle(lc);

                String cc = doc.getString("codechefHandle");
                if ((cc == null || cc.isEmpty()) && handles != null) cc = (String) handles.get("codechef");
                if (cc != null && !cc.isEmpty()) prefs.saveCodeChefHandle(cc);

                String ac = doc.getString("atcoderHandle");
                if ((ac == null || ac.isEmpty()) && handles != null) ac = (String) handles.get("atcoder");
                if (ac != null && !ac.isEmpty()) prefs.saveAtCoderHandle(ac);

                String hr = doc.getString("hackerrankHandle");
                if ((hr == null || hr.isEmpty()) && handles != null) hr = (String) handles.get("hackerrank");
                if (hr != null && !hr.isEmpty()) prefs.saveHackerRankHandle(hr);

                Map<String, Object> counts = (Map<String, Object>) doc.get("solvedCounts");
                if (counts != null) {
                    if (counts.get("codeforces") instanceof Number) {
                        prefs.saveCfSolvedCount(((Number) counts.get("codeforces")).intValue());
                    }
                    if (counts.get("leetcode") instanceof Number) {
                        prefs.saveLeetCodeSolvedCount(((Number) counts.get("leetcode")).intValue());
                    }
                    if (counts.get("codechef") instanceof Number) {
                        prefs.saveCodeChefSolvedCount(((Number) counts.get("codechef")).intValue());
                    }
                    if (counts.get("atcoder") instanceof Number) {
                        prefs.saveAtCoderSolvedCount(((Number) counts.get("atcoder")).intValue());
                    }
                    if (counts.get("hackerrank") instanceof Number) {
                        prefs.saveHackerRankSolvedCount(((Number) counts.get("hackerrank")).intValue());
                    }
                }

                Long rating = doc.getLong("rating");
                if (rating != null) prefs.saveUserRating(rating.intValue());

                List<String> blogs = (List<String>) doc.get("readBlogIds");
                if (blogs != null) {
                    for (String bId : blogs) {
                        try {
                            prefs.setBlogRead(Long.parseLong(bId), true);
                        } catch (Exception ignored) {}
                    }
                }

                List<String> catalog = (List<String>) doc.get("readCatalogIds");
                if (catalog != null) {
                    for (String cId : catalog) {
                        try {
                            prefs.setCatalogRead(Long.parseLong(cId), true);
                        } catch (Exception ignored) {}
                    }
                }

                Log.d(TAG, "Successfully restored profile from Firestore for UID: " + uid);
                if (callback != null) callback.onComplete(true);
            } else {
                Log.d(TAG, "No existing Firestore document found for UID: " + uid + ", uploading current local data");
                uploadToFirestore(context);
                if (callback != null) callback.onComplete(true);
            }
        });
    }
}

