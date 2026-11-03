package com.kuet.codearena.data.remote.model;

import java.util.List;

/**
 * Model representing standings and problem list for a contest.
 */
public class CfStandingsResponse {
    private CfContest contest;
    private List<CfProblem> problems;

    public CfContest getContest() {
        return contest;
    }

    public void setContest(CfContest contest) {
        this.contest = contest;
    }

    public List<CfProblem> getProblems() {
        return problems;
    }

    public void setProblems(List<CfProblem> problems) {
        this.problems = problems;
    }
}
