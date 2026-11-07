package com.codearena.util;

import android.graphics.Color;

public class RankColors {

    public static int getColorForRating(Integer rating) {
        if (rating == null) return Color.parseColor("#9E9E9E");
        if (rating < 1200) return Color.parseColor("#9E9E9E"); // Newbie (Gray)
        if (rating < 1400) return Color.parseColor("#00C853"); // Pupil (Green)
        if (rating < 1600) return Color.parseColor("#03A89E"); // Specialist (Cyan)
        if (rating < 1900) return Color.parseColor("#3B82F6"); // Expert (Blue)
        if (rating < 2100) return Color.parseColor("#AA00AA"); // Candidate Master (Purple)
        if (rating < 2300) return Color.parseColor("#FF8C00"); // Master (Orange)
        if (rating < 2400) return Color.parseColor("#FF8C00"); // International Master (Orange)
        if (rating < 2600) return Color.parseColor("#FF0000"); // Grandmaster (Red)
        return Color.parseColor("#FF0000"); // Legendary Grandmaster (Red)
    }

    public static String getTitleForRating(Integer rating) {
        if (rating == null) return "Unrated";
        if (rating < 1200) return "Newbie";
        if (rating < 1400) return "Pupil";
        if (rating < 1600) return "Specialist";
        if (rating < 1900) return "Expert";
        if (rating < 2100) return "Candidate Master";
        if (rating < 2300) return "Master";
        if (rating < 2400) return "International Master";
        if (rating < 2600) return "Grandmaster";
        return "Legendary Grandmaster";
    }
}


