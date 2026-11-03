package com.kuet.codearena.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing problem status and solve results inside a virtual run.
 */
@Entity(tableName = "run_problems")
public class RunProblemEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long runId;
    private String problemIndex;
    private String problemName;
    private Integer rating;
    private boolean isSolved;
    private long solveTimeSeconds;
    private int wrongAttempts;

    public RunProblemEntity(long runId, String problemIndex, String problemName,
                            Integer rating, boolean isSolved, long solveTimeSeconds, int wrongAttempts) {
        this.runId = runId;
        this.problemIndex = problemIndex;
        this.problemName = problemName;
        this.rating = rating;
        this.isSolved = isSolved;
        this.solveTimeSeconds = solveTimeSeconds;
        this.wrongAttempts = wrongAttempts;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRunId() {
        return runId;
    }

    public void setRunId(long runId) {
        this.runId = runId;
    }

    public String getProblemIndex() {
        return problemIndex;
    }

    public void setProblemIndex(String problemIndex) {
        this.problemIndex = problemIndex;
    }

    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }

    public long getSolveTimeSeconds() {
        return solveTimeSeconds;
    }

    public void setSolveTimeSeconds(long solveTimeSeconds) {
        this.solveTimeSeconds = solveTimeSeconds;
    }

    public int getWrongAttempts() {
        return wrongAttempts;
    }

    public void setWrongAttempts(int wrongAttempts) {
        this.wrongAttempts = wrongAttempts;
    }
}
