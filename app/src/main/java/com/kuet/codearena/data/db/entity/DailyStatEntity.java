package com.kuet.codearena.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity storing aggregated daily solve count and practice points.
 */
@Entity(tableName = "daily_stats")
public class DailyStatEntity {
    @PrimaryKey
    @NonNull
    private String dateStr; // Format: yyyyMMdd
    private int solvedCount;
    private int points; // Cumulative rating points for the day

    public DailyStatEntity(@NonNull String dateStr, int solvedCount, int points) {
        this.dateStr = dateStr;
        this.solvedCount = solvedCount;
        this.points = points;
    }

    @NonNull
    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(@NonNull String dateStr) {
        this.dateStr = dateStr;
    }

    public int getSolvedCount() {
        return solvedCount;
    }

    public void setSolvedCount(int solvedCount) {
        this.solvedCount = solvedCount;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
