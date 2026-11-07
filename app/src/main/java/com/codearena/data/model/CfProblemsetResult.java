package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CfProblemsetResult implements Serializable {
    @SerializedName("problems")
    private List<CfProblem> problems = new ArrayList<>();

    @SerializedName("problemStatistics")
    private List<CfProblemStatistics> problemStatistics = new ArrayList<>();

    public CfProblemsetResult() {}

    public List<CfProblem> getProblems() { return problems != null ? problems : new ArrayList<>(); }
    public void setProblems(List<CfProblem> problems) { this.problems = problems != null ? problems : new ArrayList<>(); }

    public List<CfProblemStatistics> getProblemStatistics() { return problemStatistics != null ? problemStatistics : new ArrayList<>(); }
    public void setProblemStatistics(List<CfProblemStatistics> problemStatistics) { this.problemStatistics = problemStatistics != null ? problemStatistics : new ArrayList<>(); }
}

