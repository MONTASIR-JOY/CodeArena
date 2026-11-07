package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CfParty implements Serializable {
    @SerializedName("contestId")
    private Long contestId;

    @SerializedName("members")
    private List<CfMember> members = new ArrayList<>();

    @SerializedName("participantType")
    private String participantType;

    @SerializedName("ghost")
    private boolean ghost;

    @SerializedName("room")
    private Integer room;

    @SerializedName("startTimeSeconds")
    private Long startTimeSeconds;

    public CfParty() {}

    public Long getContestId() { return contestId; }
    public void setContestId(Long contestId) { this.contestId = contestId; }

    public List<CfMember> getMembers() { return members != null ? members : new ArrayList<>(); }
    public void setMembers(List<CfMember> members) { this.members = members != null ? members : new ArrayList<>(); }

    public String getParticipantType() { return participantType; }
    public void setParticipantType(String participantType) { this.participantType = participantType; }

    public boolean isGhost() { return ghost; }
    public void setGhost(boolean ghost) { this.ghost = ghost; }

    public Integer getRoom() { return room; }
    public void setRoom(Integer room) { this.room = room; }

    public Long getStartTimeSeconds() { return startTimeSeconds; }
    public void setStartTimeSeconds(Long startTimeSeconds) { this.startTimeSeconds = startTimeSeconds; }
}

