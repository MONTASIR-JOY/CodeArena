package com.codearena.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tag_xp")
public class TagXpEntity {
    @PrimaryKey
    @NonNull
    private String tag = "";
    private int xp = 0;
    private int problemsSolvedCount = 0;

    public TagXpEntity() {}

    public TagXpEntity(@NonNull String tag, int xp, int problemsSolvedCount) {
        this.tag = tag;
        this.xp = xp;
        this.problemsSolvedCount = problemsSolvedCount;
    }

    @NonNull
    public String getTag() { return tag; }
    public void setTag(@NonNull String tag) { this.tag = tag; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getProblemsSolvedCount() { return problemsSolvedCount; }
    public void setProblemsSolvedCount(int problemsSolvedCount) { this.problemsSolvedCount = problemsSolvedCount; }

    public int getLevel() {
        return (xp / 100) + 1;
    }

    public int getProgressInLevel() {
        return xp % 100;
    }
}

