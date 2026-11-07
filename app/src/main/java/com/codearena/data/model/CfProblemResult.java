package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CfProblemResult implements Serializable {
    @SerializedName("points")
    private double points = 0.0;

    @SerializedName("penalty")
    private Integer penalty;

    @SerializedName("rejectedAttemptCount")
    private int rejectedAttemptCount = 0;

    @SerializedName("type")
    private String type;

    @SerializedName("bestSubmissionTimeSeconds")
    private Long bestSubmissionTimeSeconds;

    public CfProblemResult() {}

    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }

    public Integer getPenalty() { return penalty; }
    public void setPenalty(Integer penalty) { this.penalty = penalty; }

    public int getRejectedAttemptCount() { return rejectedAttemptCount; }
    public void setRejectedAttemptCount(int rejectedAttemptCount) { this.rejectedAttemptCount = rejectedAttemptCount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getBestSubmissionTimeSeconds() { return bestSubmissionTimeSeconds; }
    public void setBestSubmissionTimeSeconds(Long bestSubmissionTimeSeconds) { this.bestSubmissionTimeSeconds = bestSubmissionTimeSeconds; }
}

