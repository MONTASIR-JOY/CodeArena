package com.kuet.codearena.data.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room entity representing an accepted problem solve across all supported platforms.
 */
@Entity(tableName = "solves", indices = {@Index(value = {"platform", "problemId"}, unique = true)})
public class SolveEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String platform;
    private String problemId;
    private String title;
    private Integer rating;
    private String tags;
    private long solvedAt; // epoch ms

    public SolveEntity(String platform, String problemId, String title,
                       Integer rating, String tags, long solvedAt) {
        this.platform = platform;
        this.problemId = problemId;
        this.title = title;
        this.rating = rating;
        this.tags = tags;
        this.solvedAt = solvedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getProblemId() {
        return problemId;
    }

    public void setProblemId(String problemId) {
        this.problemId = problemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public long getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(long solvedAt) {
        this.solvedAt = solvedAt;
    }
}
