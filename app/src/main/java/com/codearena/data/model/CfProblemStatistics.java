package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CfProblemStatistics implements Serializable {
    @SerializedName("contestId")
    private Long contestId;

    @SerializedName("index")
    private String index;

    @SerializedName("solvedCount")
    private int solvedCount;

    public CfProblemStatistics() {}

    public Long getContestId() { return contestId; }
    public void setContestId(Long contestId) { this.contestId = contestId; }

    public String getIndex() { return index; }
    public void setIndex(String index) { this.index = index; }

    public int getSolvedCount() { return solvedCount; }
    public void setSolvedCount(int solvedCount) { this.solvedCount = solvedCount; }
}

