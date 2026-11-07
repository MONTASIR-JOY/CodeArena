package com.codearena.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * StreakCalculator: Calculates consecutive daily challenge streaks.
 */
public class StreakCalculator {

    public enum DayState {
        COMPLETE,
        PARTIAL,
        MISSED,
        PENDING,
        NONE
    }

    public static class DayProgress {
        private final int total;
        private final int solved;

        public DayProgress(int total, int solved) {
            this.total = total;
            this.solved = solved;
        }

        public int getTotal() { return total; }
        public int getSolved() { return solved; }
        public boolean isComplete() { return total > 0 && solved >= total; }
    }

    public static int currentStreak(Map<LocalDate, DayProgress> progress, LocalDate today) {
        if (progress == null) return 0;
        DayProgress todayProg = progress.get(today);
        LocalDate day = (todayProg != null && todayProg.isComplete()) ? today : today.minusDays(1);
        int streak = 0;
        while (true) {
            DayProgress dp = progress.get(day);
            if (dp != null && dp.isComplete()) {
                streak++;
                day = day.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    public static int longestStreak(Map<LocalDate, DayProgress> progress) {
        if (progress == null || progress.isEmpty()) return 0;

        List<LocalDate> completed = new ArrayList<>();
        for (Map.Entry<LocalDate, DayProgress> entry : progress.entrySet()) {
            if (entry.getValue() != null && entry.getValue().isComplete()) {
                completed.add(entry.getKey());
            }
        }
        Collections.sort(completed);

        int best = 0;
        int run = 0;
        LocalDate previous = null;

        for (LocalDate date : completed) {
            if (previous != null && previous.plusDays(1).equals(date)) {
                run++;
            } else {
                run = 1;
            }
            best = Math.max(best, run);
            previous = date;
        }
        return best;
    }

    public static int completedDays(Map<LocalDate, DayProgress> progress) {
        if (progress == null) return 0;
        int count = 0;
        for (DayProgress dp : progress.values()) {
            if (dp != null && dp.isComplete()) count++;
        }
        return count;
    }

    public static DayState stateOf(DayProgress progress, LocalDate date, LocalDate today) {
        if (progress == null || progress.getTotal() == 0) return DayState.NONE;
        if (progress.isComplete()) return DayState.COMPLETE;
        if (date.isEqual(today)) return DayState.PENDING;
        if (progress.getSolved() > 0) return DayState.PARTIAL;
        return DayState.MISSED;
    }
}

