package com.codearena.data.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.ArrayList;
import java.util.List;

@Entity(
    tableName = "problem_results",
    indices = {@Index("runId"), @Index({"contestId", "problemIndex"})}
)
public class ProblemResultEntity {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;

    private long runId;
    private long contestId;
    private String problemIndex;
    private String problemName;
    private Integer rating;
    private List<String> tags = new ArrayList<>();
    private boolean isSolved = false;
    private Long solveTimeSeconds; // seconds elapsed from contest start when solved
    private int wrongAttempts = 0;
    private long timeSpentSeconds = 0; // from per-problem stopwatch

    public ProblemResultEntity() {}

    public ProblemResultEntity(long runId, long contestId, String problemIndex, String problemName,
                               Integer rating, List<String> tags, boolean isSolved,
                               Long solveTimeSeconds, int wrongAttempts, long timeSpentSeconds) {
        this.runId = runId;
        this.contestId = contestId;
        this.problemIndex = problemIndex;
        this.problemName = problemName;
        this.rating = rating;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.isSolved = isSolved;
        this.solveTimeSeconds = solveTimeSeconds;
        this.wrongAttempts = wrongAttempts;
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRunId() { return runId; }
    public void setRunId(long runId) { this.runId = runId; }

    public long getContestId() { return contestId; }
    public void setContestId(long contestId) { this.contestId = contestId; }

    public String getProblemIndex() { return problemIndex; }
    public void setProblemIndex(String problemIndex) { this.problemIndex = problemIndex; }

    public String getProblemName() { return problemName; }
    public void setProblemName(String problemName) { this.problemName = problemName; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public List<String> getTags() { return tags != null ? tags : new ArrayList<>(); }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }

    public boolean isSolved() { return isSolved; }
    public void setSolved(boolean solved) { isSolved = solved; }

    public Long getSolveTimeSeconds() { return solveTimeSeconds; }
    public void setSolveTimeSeconds(Long solveTimeSeconds) { this.solveTimeSeconds = solveTimeSeconds; }

    public int getWrongAttempts() { return wrongAttempts; }
    public void setWrongAttempts(int wrongAttempts) { this.wrongAttempts = wrongAttempts; }

    public long getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(long timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }
}

