package com.kuet.codearena.util;

import android.graphics.Color;

/**
 * Utility mapping competitive programming rating numbers to standard platform tier colors.
 */
public final class RatingColorUtil {
    private RatingColorUtil() {}

    public static int getRatingColor(Integer rating) {
        if (rating == null || rating <= 0) {
            return Color.parseColor("#64748B"); // Slate gray
        }
        if (rating < 1200) {
            return Color.parseColor("#808080"); // Gray (Newbie)
        } else if (rating < 1400) {
            return Color.parseColor("#008000"); // Green (Pupil)
        } else if (rating < 1600) {
            return Color.parseColor("#03A89E"); // Cyan (Specialist)
        } else if (rating < 1900) {
            return Color.parseColor("#0000FF"); // Blue (Expert)
        } else if (rating < 2200) {
            return Color.parseColor("#AA00AA"); // Purple (Candidate Master)
        } else if (rating < 2400) {
            return Color.parseColor("#FF8C00"); // Orange (Master)
        } else {
            return Color.parseColor("#FF0000"); // Red (Grandmaster)
        }
    }
}
