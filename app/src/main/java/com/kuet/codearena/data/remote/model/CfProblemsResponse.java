package com.kuet.codearena.data.remote.model;

import java.util.List;

/**
 * Model representing the response returned by problemset.problems.
 */
public class CfProblemsResponse {
    private List<CfProblem> problems;
    private List<CfProblemStatistics> problemStatistics;

    public List<CfProblem> getProblems() {
        return problems;
    }

    public void setProblems(List<CfProblem> problems) {
        this.problems = problems;
    }

    public List<CfProblemStatistics> getProblemStatistics() {
        return problemStatistics;
    }

    public void setProblemStatistics(List<CfProblemStatistics> problemStatistics) {
        this.problemStatistics = problemStatistics;
    }
}
