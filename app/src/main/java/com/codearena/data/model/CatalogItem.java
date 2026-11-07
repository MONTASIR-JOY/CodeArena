package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CatalogItem implements Serializable {
    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName("blogId")
    private long blogId;

    @SerializedName("author")
    private String author;

    @SerializedName("timeAgo")
    private String timeAgo;

    @SerializedName("description")
    private String description;

    @SerializedName("mainBlock")
    private String mainBlock;

    @SerializedName("subBlock")
    private String subBlock;

    @SerializedName("subSubBlock")
    private String subSubBlock;

    private boolean isRead;

    public CatalogItem() {}

    public CatalogItem(long id, String title, String mainBlock, String subBlock, String author, String timeAgo, String description, long blogId) {
        this.id = id;
        this.title = title;
        this.mainBlock = mainBlock;
        this.subBlock = subBlock;
        this.author = author;
        this.timeAgo = timeAgo;
        this.description = description;
        this.blogId = blogId;
        this.isRead = false;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public long getBlogId() { return blogId; }
    public String getAuthor() { return author != null ? author : ""; }
    public String getTimeAgo() { return timeAgo != null ? timeAgo : ""; }
    public String getDescription() { return description != null ? description : ""; }
    public String getMainBlock() { return mainBlock != null ? mainBlock : "General Advice"; }
    public String getSubBlock() { return subBlock != null ? subBlock : ""; }
    public String getSubSubBlock() { return subSubBlock != null ? subSubBlock : ""; }

    // Compatibility helpers
    public String getSection() { return getMainBlock(); }
    public String getTopic() { return getSubBlock(); }
    public String getCategory() { return getSubBlock(); }
    public String getLevel() { return getTimeAgo(); }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
