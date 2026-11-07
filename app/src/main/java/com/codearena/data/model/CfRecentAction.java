package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CfRecentAction implements Serializable {
    @SerializedName("timeSeconds")
    private long timeSeconds;

    @SerializedName("blogEntry")
    private CfBlogEntry blogEntry;

    public CfRecentAction() {}

    public long getTimeSeconds() { return timeSeconds; }
    public void setTimeSeconds(long timeSeconds) { this.timeSeconds = timeSeconds; }

    public CfBlogEntry getBlogEntry() { return blogEntry; }
    public void setBlogEntry(CfBlogEntry blogEntry) { this.blogEntry = blogEntry; }
}

