package com.codearena.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    public static String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, secs);
        }
    }

    public static String formatDurationHours(long seconds) {
        double hours = seconds / 3600.0;
        if (hours == (long) hours) {
            return (long) hours + " hrs";
        }
        return String.format(Locale.US, "%.1f hrs", hours);
    }

    public static String formatEpochSeconds(long epochSeconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault());
        return sdf.format(new Date(epochSeconds * 1000L));
    }

    public static String formatEpochMillis(long epochMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        return sdf.format(new Date(epochMillis));
    }

    public static String formatRelativeCountdown(long targetEpochSeconds) {
        long now = System.currentTimeMillis() / 1000L;
        long diff = targetEpochSeconds - now;
        if (diff <= 0) return "Started";
        long days = diff / 86400;
        long hours = (diff % 86400) / 3600;
        long mins = (diff % 3600) / 60;
        if (days > 0) {
            return days + "d " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + mins + "m";
        } else {
            return mins + "m";
        }
    }
}

