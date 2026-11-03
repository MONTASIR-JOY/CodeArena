package com.kuet.codearena.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing an ongoing or finished virtual contest run session.
 */
@Entity(tableName = "runs")
public class RunEntity {
    @PrimaryKey(autoGenerate = true)
    private long runId;
    private int contestId;
    private String contestName;
    private long startTimeMs;
    private long durationMs;
    private long finishTimeMs;
    private String status; // RUNNING, FINISHED, CANCELLED
    private double score;
    private int solvedCount;
    private int totalWrongAttempts;
    private long totalPenaltyMinutes;

    public RunEntity(int contestId, String contestName, long startTimeMs, long durationMs,
                     long finishTimeMs, String status, double score, int solvedCount,
                     int totalWrongAttempts, long totalPenaltyMinutes) {
        this.contestId = contestId;
        this.contestName = contestName;
        this.startTimeMs = startTimeMs;
        this.durationMs = durationMs;
        this.finishTimeMs = finishTimeMs;
        this.status = status;
        this.score = score;
        this.solvedCount = solvedCount;
        this.totalWrongAttempts = totalWrongAttempts;
        this.totalPenaltyMinutes = totalPenaltyMinutes;
    }

    public long getRunId() {
        return runId;
    }

    public void setRunId(long runId) {
        this.runId = runId;
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

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public long getFinishTimeMs() {
        return finishTimeMs;
    }

    public void setFinishTimeMs(long finishTimeMs) {
        this.finishTimeMs = finishTimeMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getSolvedCount() {
        return solvedCount;
    }

    public void setSolvedCount(int solvedCount) {
        this.solvedCount = solvedCount;
    }

    public int getTotalWrongAttempts() {
        return totalWrongAttempts;
    }

    public void setTotalWrongAttempts(int totalWrongAttempts) {
        this.totalWrongAttempts = totalWrongAttempts;
    }

    public long getTotalPenaltyMinutes() {
        return totalPenaltyMinutes;
    }

    public void setTotalPenaltyMinutes(long totalPenaltyMinutes) {
        this.totalPenaltyMinutes = totalPenaltyMinutes;
    }
}
