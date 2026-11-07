package com.codearena.data.model;

import java.io.Serializable;

public class DailyProblemItem implements Serializable {
    private CfProblem problem;
    private boolean isSolved;
    private long solvedAtSeconds;

    public DailyProblemItem() {}

    public DailyProblemItem(CfProblem problem) {
        this.problem = problem;
        this.isSolved = false;
        this.solvedAtSeconds = 0;
    }

    public CfProblem getProblem() {
        return problem;
    }

    public void setProblem(CfProblem problem) {
        this.problem = problem;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }

    public long getSolvedAtSeconds() {
        return solvedAtSeconds;
    }

    public void setSolvedAtSeconds(long solvedAtSeconds) {
        this.solvedAtSeconds = solvedAtSeconds;
    }

    public String getKey() {
        if (problem == null) return "";
        return (problem.getContestId() != null ? problem.getContestId() : 0) +
                (problem.getIndex() != null ? problem.getIndex() : "");
    }
}

