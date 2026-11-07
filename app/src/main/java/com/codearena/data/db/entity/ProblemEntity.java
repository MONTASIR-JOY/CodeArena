package com.codearena.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import com.codearena.data.model.CfProblem;
import java.util.ArrayList;
import java.util.List;

@Entity(
    tableName = "problems",
    primaryKeys = {"contestId", "index"}
)
public class ProblemEntity {
    private long contestId;

    @NonNull
    private String index = "";

    private String name;
    private Double points;
    private Integer rating;
    private List<String> tags = new ArrayList<>();

    public ProblemEntity() {}

    public ProblemEntity(long contestId, @NonNull String index, String name, Double points, Integer rating, List<String> tags) {
        this.contestId = contestId;
        this.index = index;
        this.name = name;
        this.points = points;
        this.rating = rating;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public long getContestId() { return contestId; }
    public void setContestId(long contestId) { this.contestId = contestId; }

    @NonNull
    public String getIndex() { return index; }
    public void setIndex(@NonNull String index) { this.index = index; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPoints() { return points; }
    public void setPoints(Double points) { this.points = points; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public List<String> getTags() { return tags != null ? tags : new ArrayList<>(); }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }

    public CfProblem toCfProblem() {
        return new CfProblem(contestId, index, name, points, rating, tags);
    }

    public static ProblemEntity fromCfProblem(long contestId, CfProblem problem) {
        return new ProblemEntity(
            contestId,
            problem.getIndex(),
            problem.getName(),
            problem.getPoints(),
            problem.getRating(),
            problem.getTags()
        );
    }
}

