package com.codearena.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.codearena.data.model.CfContest;

/**
 * ContestEntity: Caches finished and upcoming contests locally in Room for offline access.
 */
@Entity(tableName = "contests")
public class ContestEntity {
    @PrimaryKey
    private long id;
    private String name;
    private String type;
    private String phase;
    private long durationSeconds;
    private Long startTimeSeconds;

    public ContestEntity() {}

    public ContestEntity(long id, String name, String type, String phase, long durationSeconds, Long startTimeSeconds) {
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

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public Long getStartTimeSeconds() { return startTimeSeconds; }
    public void setStartTimeSeconds(Long startTimeSeconds) { this.startTimeSeconds = startTimeSeconds; }

    public CfContest toCfContest() {
        return new CfContest(id, name, type, phase, durationSeconds, startTimeSeconds);
    }

    public static ContestEntity fromCfContest(CfContest contest) {
        return new ContestEntity(
            contest.getId(),
            contest.getName(),
            contest.getType(),
            contest.getPhase(),
            contest.getDurationSeconds(),
            contest.getStartTimeSeconds()
        );
    }
}

