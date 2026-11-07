package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CfBlogEntry implements Serializable {
    @SerializedName("id")
    private long id;

    @SerializedName("authorHandle")
    private String authorHandle;

    @SerializedName("title")
    private String title = "";

    @SerializedName("content")
    private String content;

    @SerializedName("creationTimeSeconds")
    private long creationTimeSeconds;

    @SerializedName("rating")
    private int rating;

    @SerializedName("tags")
    private List<String> tags = new ArrayList<>();

    public CfBlogEntry() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getAuthorHandle() { return authorHandle; }
    public void setAuthorHandle(String authorHandle) { this.authorHandle = authorHandle; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getCreationTimeSeconds() { return creationTimeSeconds; }
    public void setCreationTimeSeconds(long creationTimeSeconds) { this.creationTimeSeconds = creationTimeSeconds; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public List<String> getTags() { return tags != null ? tags : new ArrayList<>(); }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }
}

