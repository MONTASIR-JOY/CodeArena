package com.kuet.codearena.ui.model;

import java.util.List;
import java.util.Map;

/**
 * Model representing a competitive programmer's profile and metrics on global leaderboards.
 */
public class LeaderboardUserModel {
    private final String uid;
    private final String displayName;
    private final String photoUrl;
    private final int cfRating;
    private final double practiceRating;
    private final Map<String, Map<String, Object>> dailyStats;

    public LeaderboardUserModel(String uid, String displayName, String photoUrl,
                                int cfRating, double practiceRating,
                                Map<String, Map<String, Object>> dailyStats) {
        this.uid = uid;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.cfRating = cfRating;
        this.practiceRating = practiceRating;
        this.dailyStats = dailyStats;
    }

    public String getUid() {
        return uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public int getCfRating() {
        return cfRating;
    }

    public double getPracticeRating() {
        return practiceRating;
    }

    public Map<String, Map<String, Object>> getDailyStats() {
        return dailyStats;
    }

    public int getSolvedInWindow(List<String> dateKeys) {
        if (dailyStats == null) return 0;
        int sum = 0;
        for (String k : dateKeys) {
            Map<String, Object> day = dailyStats.get(k);
            if (day != null && day.containsKey("solved")) {
                Object val = day.get("solved");
                if (val instanceof Number) {
                    sum += ((Number) val).intValue();
                }
            }
        }
        return sum;
    }

    public int getPointsInWindow(List<String> dateKeys) {
        if (dailyStats == null) return 0;
        int sum = 0;
        for (String k : dateKeys) {
            Map<String, Object> day = dailyStats.get(k);
            if (day != null && day.containsKey("points")) {
                Object val = day.get("points");
                if (val instanceof Number) {
                    sum += ((Number) val).intValue();
                }
            }
        }
        return sum;
    }
}
