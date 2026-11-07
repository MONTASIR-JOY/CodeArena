package com.codearena.data.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ClistContest implements Serializable {
    @SerializedName("id")
    private long id;

    @SerializedName("event")
    private String event;

    @SerializedName("start")
    private String start;

    @SerializedName("end")
    private String end;

    @SerializedName("duration")
    private long durationSeconds;

    @SerializedName("href")
    private String href;

    @SerializedName("resource")
    private JsonElement resource;

    public ClistContest() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getStart() { return start; }
    public void setStart(String start) { this.start = start; }

    public String getEnd() { return end; }
    public void setEnd(String end) { this.end = end; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    public JsonElement getResource() { return resource; }
    public void setResource(JsonElement resource) { this.resource = resource; }
}

