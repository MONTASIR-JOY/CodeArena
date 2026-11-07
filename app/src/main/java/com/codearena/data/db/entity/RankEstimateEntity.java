package com.codearena.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rank_estimates")
public class RankEstimateEntity {
    @PrimaryKey
    private long runId;
    private long contestId;
    private int estimatedRank;
    private int totalParticipants;
    private double points;
    private int penalty;
    private long createdAtEpochMillis = System.currentTimeMillis();

    public RankEstimateEntity() {}

    public RankEstimateEntity(long runId, long contestId, int estimatedRank, int totalParticipants,
                              double points, int penalty, long createdAtEpochMillis) {
        this.runId = runId;
        this.contestId = contestId;
        this.estimatedRank = estimatedRank;
        this.totalParticipants = totalParticipants;
        this.points = points;
        this.penalty = penalty;
        this.createdAtEpochMillis = createdAtEpochMillis;
    }

    public long getRunId() { return runId; }
    public void setRunId(long runId) { this.runId = runId; }

    public long getContestId() { return contestId; }
    public void setContestId(long contestId) { this.contestId = contestId; }

    public int getEstimatedRank() { return estimatedRank; }
    public void setEstimatedRank(int estimatedRank) { this.estimatedRank = estimatedRank; }

    public int getTotalParticipants() { return totalParticipants; }
    public void setTotalParticipants(int totalParticipants) { this.totalParticipants = totalParticipants; }

    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }

    public int getPenalty() { return penalty; }
    public void setPenalty(int penalty) { this.penalty = penalty; }

    public long getCreatedAtEpochMillis() { return createdAtEpochMillis; }
    public void setCreatedAtEpochMillis(long createdAtEpochMillis) { this.createdAtEpochMillis = createdAtEpochMillis; }
}

