package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CfContest implements Serializable {
    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type; // CF, IOI, ICPC

    @SerializedName("phase")
    private String phase; // BEFORE, CODING, FINISHED

    @SerializedName("frozen")
    private boolean frozen;

    @SerializedName("durationSeconds")
    private long durationSeconds;

    @SerializedName("startTimeSeconds")
    private Long startTimeSeconds;

    @SerializedName("relativeTimeSeconds")
    private Long relativeTimeSeconds;

    @SerializedName("preparedBy")
    private String preparedBy;

    @SerializedName("websiteUrl")
    private String websiteUrl;

    @SerializedName("description")
    private String description;

    @SerializedName("difficulty")
    private Integer difficulty;

    @SerializedName("kind")
    private String kind;

    @SerializedName("icpcRegion")
    private String icpcRegion;

    @SerializedName("country")
    private String country;

    @SerializedName("city")
    private String city;

    @SerializedName("season")
    private String season;

    public CfContest() {}

    public CfContest(long id, String name, String type, String phase, long durationSeconds, Long startTimeSeconds) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.phase = phase;
        this.durationSeconds = durationSeconds;
        this.startTimeSeconds = startTimeSeconds;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public Long getStartTimeSeconds() { return startTimeSeconds; }
    public void setStartTimeSeconds(Long startTimeSeconds) { this.startTimeSeconds = startTimeSeconds; }

    public Long getRelativeTimeSeconds() { return relativeTimeSeconds; }
    public void setRelativeTimeSeconds(Long relativeTimeSeconds) { this.relativeTimeSeconds = relativeTimeSeconds; }

    public String getPreparedBy() { return preparedBy; }
    public void setPreparedBy(String preparedBy) { this.preparedBy = preparedBy; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }

    public boolean isFinished() {
        return "FINISHED".equalsIgnoreCase(phase);
    }

    public boolean isUpcoming() {
        return "BEFORE".equalsIgnoreCase(phase);
    }
}

