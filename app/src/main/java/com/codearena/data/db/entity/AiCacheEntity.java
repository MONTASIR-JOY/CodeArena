package com.codearena.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ai_cache")
public class AiCacheEntity {
    @PrimaryKey
    @NonNull
    private String cacheKey = "";
    private String type; // "coach_review", "hints", "training_plan"
    private String jsonContent;
    private long createdAtEpochMillis = System.currentTimeMillis();

    public AiCacheEntity() {}

    public AiCacheEntity(@NonNull String cacheKey, String type, String jsonContent, long createdAtEpochMillis) {
        this.cacheKey = cacheKey;
        this.type = type;
        this.jsonContent = jsonContent;
        this.createdAtEpochMillis = createdAtEpochMillis;
    }

    @NonNull
    public String getCacheKey() { return cacheKey; }
    public void setCacheKey(@NonNull String cacheKey) { this.cacheKey = cacheKey; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getJsonContent() { return jsonContent; }
    public void setJsonContent(String jsonContent) { this.jsonContent = jsonContent; }

    public long getCreatedAtEpochMillis() { return createdAtEpochMillis; }
    public void setCreatedAtEpochMillis(long createdAtEpochMillis) { this.createdAtEpochMillis = createdAtEpochMillis; }
}

