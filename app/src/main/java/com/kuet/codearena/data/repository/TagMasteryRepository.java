package com.kuet.codearena.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kuet.codearena.data.db.AppDatabase;
import com.kuet.codearena.data.db.dao.RunDao;
import com.kuet.codearena.data.db.dao.SolveDao;
import com.kuet.codearena.data.db.dao.UpsolveDao;
import com.kuet.codearena.data.db.entity.RunEntity;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.data.db.entity.UpsolveItemEntity;
import com.kuet.codearena.ui.model.TagMasteryModel;
import com.kuet.codearena.util.AppExecutors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository calculating tag proficiency, accuracy rates, and skill tier distributions from solve history.
 */
public class TagMasteryRepository {
    private static volatile TagMasteryRepository INSTANCE;

    private final SolveDao solveDao;
    private final UpsolveDao upsolveDao;
    private final RunDao runDao;
    private final AppExecutors executors;

    private TagMasteryRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.solveDao = db.solveDao();
        this.upsolveDao = db.upsolveDao();
        this.runDao = db.runDao();
        this.executors = AppExecutors.getInstance();
    }

    public static TagMasteryRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TagMasteryRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TagMasteryRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public interface TagMasteryCallback {
        void onLoaded(List<TagMasteryModel> tagStats, List<RunEntity> recentRuns, String weakestTag);
    }

    public void loadMasteryData(TagMasteryCallback callback) {
        executors.diskIO().execute(() -> {
            List<SolveEntity> allSolves = solveDao.getAllSolvesSync();
            List<UpsolveItemEntity> pendingUpsolves = upsolveDao.getPendingSync();
            List<RunEntity> recentRuns = runDao.getLastFinishedRuns(10);

            // Group solves by tag
            Map<String, Integer> tagSolvedCounts = new HashMap<>();
            Map<String, Long> tagRatingSums = new HashMap<>();
            Map<String, Integer> tagUnsolvedCounts = new HashMap<>();

            // Count pending upsolves per tag
            if (pendingUpsolves != null) {
                for (UpsolveItemEntity u : pendingUpsolves) {
                    String[] tags = extractTags(u.getTags());
                    for (String t : tags) {
                        t = t.trim().toLowerCase();
                        if (t.isEmpty()) continue;
                        tagUnsolvedCounts.put(t, tagUnsolvedCounts.getOrDefault(t, 0) + 1);
                    }
                }
            }

            // Count solves & sum ratings per tag
            if (allSolves != null) {
                for (SolveEntity s : allSolves) {
                    String[] tags = extractTags(s.getTags());
                    int rating = s.getRating() != null && s.getRating() > 0 ? s.getRating() : 800;
                    for (String t : tags) {
                        t = t.trim().toLowerCase();
                        if (t.isEmpty()) continue;
                        tagSolvedCounts.put(t, tagSolvedCounts.getOrDefault(t, 0) + 1);
                        tagRatingSums.put(t, tagRatingSums.getOrDefault(t, 0L) + rating);
                    }
                }
            }

            // Ensure common canonical tags exist if empty so user has a rich radar chart on first launch
            String[] canonicalTags = {"dp", "greedy", "math", "graphs", "data structures", "strings", "binary search", "trees"};
            for (String ct : canonicalTags) {
                if (!tagSolvedCounts.containsKey(ct)) {
                    tagSolvedCounts.put(ct, 0);
                    tagRatingSums.put(ct, 0L);
                }
            }

            List<TagMasteryModel> result = new ArrayList<>();
            String weakestTag = "dp";
            double lowestScore = Double.MAX_VALUE;

            for (Map.Entry<String, Integer> entry : tagSolvedCounts.entrySet()) {
                String tag = entry.getKey();
                int solved = entry.getValue();
                long sumRating = tagRatingSums.getOrDefault(tag, 0L);
                int avgRating = solved > 0 ? (int) (sumRating / solved) : 800;
                int unsolved = tagUnsolvedCounts.getOrDefault(tag, 0);

                // Accuracy calculation: AC / (AC + Unsolved)
                int totalAttempts = solved + unsolved;
                double accuracy = totalAttempts > 0 ? ((double) solved / totalAttempts) * 100.0 : 100.0;

                // Level determination:
                // Beginner under 1000 average, Intermediate 1000-1499, Advanced 1500 and above, with at least 5 solves
                String level;
                if (avgRating >= 1500 && solved >= 5) {
                    level = "Advanced";
                } else if (avgRating >= 1000) {
                    level = "Intermediate";
                } else {
                    level = "Beginner";
                }

                result.add(new TagMasteryModel(tag, solved, avgRating, accuracy, level, unsolved));

                // Weakness rank heuristic: lower accuracy + lower avg rating + fewer solves
                double weaknessScore = (accuracy * 0.4) + (avgRating * 0.4) + (solved * 10.0);
                if (weaknessScore < lowestScore) {
                    lowestScore = weaknessScore;
                    weakestTag = tag;
                }
            }

            // Sort by solvedCount descending
            Collections.sort(result, (a, b) -> Integer.compare(b.getSolvedCount(), a.getSolvedCount()));

            final String finalWeakest = weakestTag;
            executors.mainThread().execute(() -> {
                if (callback != null) {
                    callback.onLoaded(result, recentRuns != null ? recentRuns : new ArrayList<>(), finalWeakest);
                }
            });
        });
    }

    private String[] extractTags(String tagsStr) {
        if (tagsStr == null || tagsStr.trim().isEmpty()) {
            return new String[0];
        }
        return tagsStr.split("[,;]");
    }
}

