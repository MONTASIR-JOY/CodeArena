package com.codearena.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "virtual_runs")
public class VirtualRunEntity {
    @PrimaryKey(autoGenerate = true)
    private long runId = 0;

    private long contestId;
    private String contestName;
    private long startTimeEpochMillis;
    private long endTimeEpochMillis;
    private long durationSeconds;
    private int solvedCount = 0;
    private int totalProblemsCount = 0;
    private long penaltyMinutes = 0;
    private int estimatedPerformance = 0;
    private String mode = "Standard"; // "Standard" or "Practice"
    private boolean completed = false;

    public VirtualRunEntity() {}

    public VirtualRunEntity(long contestId, String contestName, long startTimeEpochMillis,
                            long endTimeEpochMillis, long durationSeconds, int solvedCount,
                            int totalProblemsCount, long penaltyMinutes, int estimatedPerformance,
                            String mode, boolean completed) {
        this.contestId = contestId;
        this.contestName = contestName;
        this.startTimeEpochMillis = startTimeEpochMillis;
        this.endTimeEpochMillis = endTimeEpochMillis;
        this.durationSeconds = durationSeconds;
        this.solvedCount = solvedCount;
        this.totalProblemsCount = totalProblemsCount;
        this.penaltyMinutes = penaltyMinutes;
        this.estimatedPerformance = estimatedPerformance;
        this.mode = mode != null ? mode : "Standard";
        this.completed = completed;
    }

    public long getRunId() { return runId; }
    public void setRunId(long runId) { this.runId = runId; }

    public long getContestId() { return contestId; }
    public void setContestId(long contestId) { this.contestId = contestId; }

    public String getContestName() { return contestName; }
    public void setContestName(String contestName) { this.contestName = contestName; }

    public long getStartTimeEpochMillis() { return startTimeEpochMillis; }
    public void setStartTimeEpochMillis(long startTimeEpochMillis) { this.startTimeEpochMillis = startTimeEpochMillis; }

    public long getEndTimeEpochMillis() { return endTimeEpochMillis; }
    public void setEndTimeEpochMillis(long endTimeEpochMillis) { this.endTimeEpochMillis = endTimeEpochMillis; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public int getSolvedCount() { return solvedCount; }
    public void setSolvedCount(int solvedCount) { this.solvedCount = solvedCount; }

    public int getTotalProblemsCount() { return totalProblemsCount; }
    public void setTotalProblemsCount(int totalProblemsCount) { this.totalProblemsCount = totalProblemsCount; }

    public long getPenaltyMinutes() { return penaltyMinutes; }
    public void setPenaltyMinutes(long penaltyMinutes) { this.penaltyMinutes = penaltyMinutes; }

    public int getEstimatedPerformance() { return estimatedPerformance; }
    public void setEstimatedPerformance(int estimatedPerformance) { this.estimatedPerformance = estimatedPerformance; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}

