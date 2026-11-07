package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CfStandingsResult implements Serializable {
    @SerializedName("contest")
    private CfContest contest;

    @SerializedName("problems")
    private List<CfProblem> problems = new ArrayList<>();

    @SerializedName("rows")
    private List<CfRanklistRow> rows = new ArrayList<>();

    public CfStandingsResult() {}

    public CfStandingsResult(CfContest contest, List<CfProblem> problems, List<CfRanklistRow> rows) {
        this.contest = contest;
        this.problems = problems != null ? problems : new ArrayList<>();
        this.rows = rows != null ? rows : new ArrayList<>();
    }

    public CfContest getContest() { return contest; }
    public void setContest(CfContest contest) { this.contest = contest; }

    public List<CfProblem> getProblems() { return problems != null ? problems : new ArrayList<>(); }
    public void setProblems(List<CfProblem> problems) { this.problems = problems != null ? problems : new ArrayList<>(); }

    public List<CfRanklistRow> getRows() { return rows != null ? rows : new ArrayList<>(); }
    public void setRows(List<CfRanklistRow> rows) { this.rows = rows != null ? rows : new ArrayList<>(); }
}

