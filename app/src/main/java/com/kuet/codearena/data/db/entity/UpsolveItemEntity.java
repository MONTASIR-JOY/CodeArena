package com.kuet.codearena.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing an unsolved problem slated for spaced-repetition upsolving.
 */
@Entity(tableName = "upsolve_items")
public class UpsolveItemEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private int contestId;
    private String problemIndex;
    private String problemName;
    private Integer rating;
    private String tags;
    private long addedTimeMs;
    private long dueDateMs;
    private int intervalStep; // 1, 3, 7, 14 days
    private boolean isSolved;
    private long solvedAtMs;
    private boolean isExpired;

    public UpsolveItemEntity(int contestId, String problemIndex, String problemName,
                             Integer rating, String tags, long addedTimeMs, long dueDateMs,
                             int intervalStep, boolean isSolved, long solvedAtMs, boolean isExpired) {
        this.contestId = contestId;
        this.problemIndex = problemIndex;
        this.problemName = problemName;
        this.rating = rating;
        this.tags = tags;
        this.addedTimeMs = addedTimeMs;
        this.dueDateMs = dueDateMs;
        this.intervalStep = intervalStep;
        this.isSolved = isSolved;
        this.solvedAtMs = solvedAtMs;
        this.isExpired = isExpired;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getContestId() {
        return contestId;
    }

    public void setContestId(int contestId) {
        this.contestId = contestId;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getAddedTimeMs() {
        return addedTimeMs;
    }

    public void setAddedTimeMs(long addedTimeMs) {
        this.addedTimeMs = addedTimeMs;
    }

    public long getDueDateMs() {
        return dueDateMs;
    }

    public void setDueDateMs(long dueDateMs) {
        this.dueDateMs = dueDateMs;
    }

    public int getIntervalStep() {
        return intervalStep;
    }

    public void setIntervalStep(int intervalStep) {
        this.intervalStep = intervalStep;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }

    public long getSolvedAtMs() {
        return solvedAtMs;
    }

    public void setSolvedAtMs(long solvedAtMs) {
        this.solvedAtMs = solvedAtMs;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }
}
