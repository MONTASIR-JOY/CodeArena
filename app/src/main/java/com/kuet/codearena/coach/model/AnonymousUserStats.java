package com.kuet.codearena.coach.model;

import java.util.List;

/**
 * Anonymized user performance metrics passed to AI coaching models containing no PII, handles, or emails.
 */
public class AnonymousUserStats {
    private final double practiceRating;
    private final int streakDays;
    private final int pendingUpsolves;
    private final List<String> weakTags;
    private final int solvedTodayCount;
    private final int upcomingContestsCount;

    public AnonymousUserStats(double practiceRating, int streakDays, int pendingUpsolves,
                              List<String> weakTags, int solvedTodayCount, int upcomingContestsCount) {
        this.practiceRating = practiceRating;
        this.streakDays = streakDays;
        this.pendingUpsolves = pendingUpsolves;
        this.weakTags = weakTags;
        this.solvedTodayCount = solvedTodayCount;
        this.upcomingContestsCount = upcomingContestsCount;
    }

    public double getPracticeRating() { return practiceRating; }
    public int getStreakDays() { return streakDays; }
    public int getPendingUpsolves() { return pendingUpsolves; }
    public List<String> getWeakTags() { return weakTags; }
    public int getSolvedTodayCount() { return solvedTodayCount; }
    public int getUpcomingContestsCount() { return upcomingContestsCount; }
}
