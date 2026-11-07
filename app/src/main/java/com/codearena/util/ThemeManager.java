package com.codearena.util;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    public static void apply(String theme) {
        if ("light".equalsIgnoreCase(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("system".equalsIgnoreCase(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            // Default to dark theme
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }
}


