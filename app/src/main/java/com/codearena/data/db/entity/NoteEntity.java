package com.codearena.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(
    tableName = "problem_notes",
    primaryKeys = {"contestId", "problemIndex"}
)
public class NoteEntity {
    private long contestId;

    @NonNull
    private String problemIndex = "";

    private String text = "";
    private long updatedAtEpochMillis = System.currentTimeMillis();

    public NoteEntity() {}

    public NoteEntity(long contestId, @NonNull String problemIndex, String text, long updatedAtEpochMillis) {
        this.contestId = contestId;
        this.problemIndex = problemIndex;
        this.text = text != null ? text : "";
        this.updatedAtEpochMillis = updatedAtEpochMillis;
    }

    public long getContestId() { return contestId; }
    public void setContestId(long contestId) { this.contestId = contestId; }

    @NonNull
    public String getProblemIndex() { return problemIndex; }
    public void setProblemIndex(@NonNull String problemIndex) { this.problemIndex = problemIndex; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getUpdatedAtEpochMillis() { return updatedAtEpochMillis; }
    public void setUpdatedAtEpochMillis(long updatedAtEpochMillis) { this.updatedAtEpochMillis = updatedAtEpochMillis; }
}

