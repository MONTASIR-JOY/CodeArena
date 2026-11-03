package com.kuet.codearena.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

/**
 * Room entity representing an individual programming problem cached from a contest or problemset.
 */
@Entity(tableName = "problems", primaryKeys = {"contestId", "problemIndex"})
public class ProblemEntity {
    private int contestId;
    @NonNull
    private String problemIndex;
    private String name;
    private String type;
    private Integer rating;
    private String tags;
    private int solvedCount;

    public ProblemEntity(int contestId, @NonNull String problemIndex, String name, String type,
                         Integer rating, String tags, int solvedCount) {
        this.contestId = contestId;
        this.problemIndex = problemIndex;
        this.name = name;
        this.type = type;
        this.rating = rating;
        this.tags = tags;
        this.solvedCount = solvedCount;
    }

    public int getContestId() {
        return contestId;
    }

    public void setContestId(int contestId) {
        this.contestId = contestId;
    }

    @NonNull
    public String getProblemIndex() {
        return problemIndex;
    }

    public void setProblemIndex(@NonNull String problemIndex) {
        this.problemIndex = problemIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public int getSolvedCount() {
        return solvedCount;
    }

    public void setSolvedCount(int solvedCount) {
        this.solvedCount = solvedCount;
    }
}
