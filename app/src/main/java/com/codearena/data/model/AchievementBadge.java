package com.codearena.data.model;

import java.io.Serializable;

public class AchievementBadge implements Serializable {
    private String id;
    private String title;
    private String description;
    private int progress;
    private int maxProgress;
    private boolean isUnlocked;

    public AchievementBadge(String id, String title, String description, int progress, int maxProgress, boolean isUnlocked) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.isUnlocked = isUnlocked;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }
}

