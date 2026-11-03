package com.kuet.codearena.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a scheduled contest reminder alarm.
 */
@Entity(tableName = "reminders")
public class ReminderEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private int contestId;
    private String contestName;
    private String platform;
    private long contestStartTimeMs;
    private long reminderTimeMs;
    private int leadTimeMinutes;
    private boolean isActive;

    public ReminderEntity(int contestId, String contestName, String platform,
                          long contestStartTimeMs, long reminderTimeMs, int leadTimeMinutes, boolean isActive) {
        this.contestId = contestId;
        this.contestName = contestName;
        this.platform = platform;
        this.contestStartTimeMs = contestStartTimeMs;
        this.reminderTimeMs = reminderTimeMs;
        this.leadTimeMinutes = leadTimeMinutes;
        this.isActive = isActive;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getContestId() {
        return contestId;
    }

    public void setContestId(int contestId) {
        this.contestId = contestId;
    }

    public String getContestName() {
        return contestName;
    }

    public void setContestName(String contestName) {
        this.contestName = contestName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public long getContestStartTimeMs() {
        return contestStartTimeMs;
    }

    public void setContestStartTimeMs(long contestStartTimeMs) {
        this.contestStartTimeMs = contestStartTimeMs;
    }

    public long getReminderTimeMs() {
        return reminderTimeMs;
    }

    public void setReminderTimeMs(long reminderTimeMs) {
        this.reminderTimeMs = reminderTimeMs;
    }

    public int getLeadTimeMinutes() {
        return leadTimeMinutes;
    }

    public void setLeadTimeMinutes(int leadTimeMinutes) {
        this.leadTimeMinutes = leadTimeMinutes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
