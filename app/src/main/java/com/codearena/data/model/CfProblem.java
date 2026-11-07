package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CfProblem implements Serializable {
    @SerializedName("contestId")
    private Long contestId;

    @SerializedName("problemsetName")
    private String problemsetName;

    @SerializedName("index")
    private String index;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("points")
    private Double points;

    @SerializedName("rating")
    private Integer rating;

    @SerializedName("tags")
    private List<String> tags = new ArrayList<>();

    public CfProblem() {}

    public CfProblem(Long contestId, String index, String name, Double points, Integer rating, List<String> tags) {
        this.contestId = contestId;
        this.index = index;
        this.name = name;
        this.points = points;
        this.rating = rating;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public Long getContestId() { return contestId; }
    public void setContestId(Long contestId) { this.contestId = contestId; }

    public String getProblemsetName() { return problemsetName; }
    public void setProblemsetName(String problemsetName) { this.problemsetName = problemsetName; }

    public String getIndex() { return index; }
    public void setIndex(String index) { this.index = index; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getPoints() { return points; }
    public void setPoints(Double points) { this.points = points; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public List<String> getTags() { return tags != null ? tags : new ArrayList<>(); }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }
}

