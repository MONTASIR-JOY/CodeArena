package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CfRanklistRow implements Serializable {
    @SerializedName("party")
    private CfParty party;

    @SerializedName("rank")
    private int rank;

    @SerializedName("points")
    private double points;

    @SerializedName("penalty")
    private int penalty;

    @SerializedName("successfulHackCount")
    private int successfulHackCount;

    @SerializedName("unsuccessfulHackCount")
    private int unsuccessfulHackCount;

    @SerializedName("problemResults")
    private List<CfProblemResult> problemResults = new ArrayList<>();

    public CfRanklistRow() {}

    public CfRanklistRow(CfParty party, int rank, double points, int penalty, List<CfProblemResult> problemResults) {
        this.party = party;
        this.rank = rank;
        this.points = points;
        this.penalty = penalty;
        this.problemResults = problemResults != null ? problemResults : new ArrayList<>();
    }

    public CfParty getParty() { return party; }
    public void setParty(CfParty party) { this.party = party; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }

    public int getPenalty() { return penalty; }
    public void setPenalty(int penalty) { this.penalty = penalty; }

    public int getSuccessfulHackCount() { return successfulHackCount; }
    public void setSuccessfulHackCount(int successfulHackCount) { this.successfulHackCount = successfulHackCount; }

    public int getUnsuccessfulHackCount() { return unsuccessfulHackCount; }
    public void setUnsuccessfulHackCount(int unsuccessfulHackCount) { this.unsuccessfulHackCount = unsuccessfulHackCount; }

    public List<CfProblemResult> getProblemResults() { return problemResults != null ? problemResults : new ArrayList<>(); }
    public void setProblemResults(List<CfProblemResult> problemResults) { this.problemResults = problemResults != null ? problemResults : new ArrayList<>(); }
}

