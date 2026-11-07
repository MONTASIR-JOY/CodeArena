package com.codearena.data.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.ArrayList;
import java.util.List;

@Entity(
    tableName = "upsolve_items",
    indices = {@Index(value = {"contestId", "problemIndex"}, unique = true)}
)
public class UpsolveItemEntity {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;

    private long contestId;
    private String problemIndex;
    private String problemName;
    private Integer rating;
    private List<String> tags = new ArrayList<>();
    private long dateAddedEpochMillis;
    private int intervalIndex = 0; // 0..4
    private long nextReviewEpochMillis;
    private int reviewCount = 0;
    private boolean isSolved = false;

    public UpsolveItemEntity() {}

    public UpsolveItemEntity(long contestId, String problemIndex, String problemName,
                             Integer rating, List<String> tags, long dateAddedEpochMillis,
                             int intervalIndex, long nextReviewEpochMillis, int reviewCount,
                             boolean isSolved) {
        this.contestId = contestId;
        this.problemIndex = problemIndex;
        this.problemName = problemName;
        this.rating = rating;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.dateAddedEpochMillis = dateAddedEpochMillis;
        this.intervalIndex = intervalIndex;
        this.nextReviewEpochMillis = nextReviewEpochMillis;
        this.reviewCount = reviewCount;
        this.isSolved = isSolved;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

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

    public long getDateAddedEpochMillis() { return dateAddedEpochMillis; }
    public void setDateAddedEpochMillis(long dateAddedEpochMillis) { this.dateAddedEpochMillis = dateAddedEpochMillis; }

    public int getIntervalIndex() { return intervalIndex; }
    public void setIntervalIndex(int intervalIndex) { this.intervalIndex = intervalIndex; }

    public long getNextReviewEpochMillis() { return nextReviewEpochMillis; }
    public void setNextReviewEpochMillis(long nextReviewEpochMillis) { this.nextReviewEpochMillis = nextReviewEpochMillis; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public boolean isSolved() { return isSolved; }
    public void setSolved(boolean solved) { isSolved = solved; }
}

