package com.codearena.util;

import java.util.Arrays;
import java.util.List;

public class BadgeCalculator {

    public static class BadgeStats {
        public final int dailyDaysCompleted;
        public final int longestStreak;
        public final int contestsCompleted;
        public final int contestProblemsSolved;
        public final int upsolvesSolved;
        public final int maxTagLevel;

        public BadgeStats(int dailyDaysCompleted, int longestStreak, int contestsCompleted,
                          int contestProblemsSolved, int upsolvesSolved, int maxTagLevel) {
            this.dailyDaysCompleted = dailyDaysCompleted;
            this.longestStreak = longestStreak;
            this.contestsCompleted = contestsCompleted;
            this.contestProblemsSolved = contestProblemsSolved;
            this.upsolvesSolved = upsolvesSolved;
            this.maxTagLevel = maxTagLevel;
        }
    }

    public static class Badge {
        public final String id;
        public final String title;
        public final String description;
        public final boolean earned;
        public final String progress;

        public Badge(String id, String title, String description, boolean earned, String progress) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.earned = earned;
            this.progress = progress;
        }
    }

    private static Badge badge(String id, String title, String description, int value, int goal) {
        return new Badge(id, title, description, value >= goal, Math.min(value, goal) + " / " + goal);
    }

    public static List<Badge> compute(BadgeStats s) {
        return Arrays.asList(
            badge("first_daily", "First Step", "Finish a daily challenge", s.dailyDaysCompleted, 1),
            badge("streak_3", "On a Roll", "3-day streak", s.longestStreak, 3),
            badge("streak_7", "Week Warrior", "7-day streak", s.longestStreak, 7),
            badge("streak_30", "Unstoppable", "30-day streak", s.longestStreak, 30),
            badge("daily_10", "Consistent", "Finish 10 daily challenges", s.dailyDaysCompleted, 10),
            badge("contest_1", "Contestant", "Finish a virtual contest", s.contestsCompleted, 1),
            badge("contest_10", "Arena Regular", "Finish 10 virtual contests", s.contestsCompleted, 10),
            badge("ac_50", "Problem Crusher", "Solve 50 problems inside contests", s.contestProblemsSolved, 50),
            badge("upsolve_10", "Comeback Kid", "Solve 10 upsolve problems", s.upsolvesSolved, 10),
            badge("tag_5", "Topic Master", "Reach level 5 in any tag", s.maxTagLevel, 5)
        );
    }
}

