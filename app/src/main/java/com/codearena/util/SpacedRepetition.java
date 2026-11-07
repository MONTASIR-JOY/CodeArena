package com.codearena.util;

import java.util.concurrent.TimeUnit;

/**
 * SpacedRepetition: Calculates next review intervals based on standard or debug fast schedule.
 */
public class SpacedRepetition {

    public static final int[] STANDARD_INTERVALS_DAYS = {1, 3, 7, 14, 30};
    public static final int[] DEBUG_INTERVALS_SECONDS = {10, 30, 60, 120, 300};

    public static long calculateNextReviewEpochMillis(int currentIndex, boolean isFastDebug, long fromEpochMillis) {
        if (isFastDebug) {
            int safeIndex = Math.max(0, Math.min(currentIndex, DEBUG_INTERVALS_SECONDS.length - 1));
            int delaySecs = DEBUG_INTERVALS_SECONDS[safeIndex];
            return fromEpochMillis + TimeUnit.SECONDS.toMillis(delaySecs);
        } else {
            int safeIndex = Math.max(0, Math.min(currentIndex, STANDARD_INTERVALS_DAYS.length - 1));
            int delayDays = STANDARD_INTERVALS_DAYS[safeIndex];
            return fromEpochMillis + TimeUnit.DAYS.toMillis(delayDays);
        }
    }

    public static long calculateNextReviewEpochMillis(int currentIndex, boolean isFastDebug) {
        return calculateNextReviewEpochMillis(currentIndex, isFastDebug, System.currentTimeMillis());
    }
}

