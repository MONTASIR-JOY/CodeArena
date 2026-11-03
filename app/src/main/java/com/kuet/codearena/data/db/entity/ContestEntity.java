package com.kuet.codearena.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a competitive programming contest from any supported platform.
 */
@Entity(tableName = "contests")
public class ContestEntity {
    @PrimaryKey
    private int id;
    private String name;
    private String type; // CF, IOI, ICPC, etc.
    private String phase; // BEFORE, CODING, FINISHED
    private long durationSeconds;
    private long startTimeSeconds;
    private String division; // Div. 1, Div. 2, Div. 3, Div. 4, Educational, etc.
    private String platform; // CODEFORCES, ATCODER, LEETCODE, CODECHEF
    private String url;

    public ContestEntity(int id, String name, String type, String phase, long durationSeconds,
                         long startTimeSeconds, String division, String platform, String url) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.phase = phase;
        this.durationSeconds = durationSeconds;
        this.startTimeSeconds = startTimeSeconds;
        this.division = division;
        this.platform = platform;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public long getStartTimeSeconds() {
        return startTimeSeconds;
    }

    public void setStartTimeSeconds(long startTimeSeconds) {
        this.startTimeSeconds = startTimeSeconds;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
