package com.kuet.codearena.data.remote.model;

/**
 * Model representing solved count statistics for a Codeforces problem.
 */
public class CfProblemStatistics {
    private int contestId;
    private String index;
    private int solvedCount;

    public int getContestId() {
        return contestId;
    }

    public void setContestId(int contestId) {
        this.contestId = contestId;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public int getSolvedCount() {
        return solvedCount;
    }

    public void setSolvedCount(int solvedCount) {
        this.solvedCount = solvedCount;
    }
}
