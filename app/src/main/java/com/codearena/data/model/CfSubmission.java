package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CfSubmission implements Serializable {
    @SerializedName("id")
    private long id;

    @SerializedName("contestId")
    private Long contestId;

    @SerializedName("creationTimeSeconds")
    private long creationTimeSeconds;

    @SerializedName("relativeTimeSeconds")
    private Long relativeTimeSeconds;

    @SerializedName("problem")
    private CfProblem problem;

    @SerializedName("author")
    private CfParty author;

    @SerializedName("programmingLanguage")
    private String programmingLanguage;

    @SerializedName("verdict")
    private String verdict; // e.g. "OK", "WRONG_ANSWER"

    @SerializedName("testset")
    private String testset;

    @SerializedName("passedTestCount")
    private int passedTestCount;

    @SerializedName("timeConsumedMillis")
    private int timeConsumedMillis;

    @SerializedName("memoryConsumedBytes")
    private long memoryConsumedBytes;

    public CfSubmission() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getContestId() { return contestId; }
    public void setContestId(Long contestId) { this.contestId = contestId; }

    public long getCreationTimeSeconds() { return creationTimeSeconds; }
    public void setCreationTimeSeconds(long creationTimeSeconds) { this.creationTimeSeconds = creationTimeSeconds; }

    public Long getRelativeTimeSeconds() { return relativeTimeSeconds; }
    public void setRelativeTimeSeconds(Long relativeTimeSeconds) { this.relativeTimeSeconds = relativeTimeSeconds; }

    public CfProblem getProblem() { return problem; }
    public void setProblem(CfProblem problem) { this.problem = problem; }

    public CfParty getAuthor() { return author; }
    public void setAuthor(CfParty author) { this.author = author; }

    public String getProgrammingLanguage() { return programmingLanguage; }
    public void setProgrammingLanguage(String programmingLanguage) { this.programmingLanguage = programmingLanguage; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

    public String getTestset() { return testset; }
    public void setTestset(String testset) { this.testset = testset; }

    public int getPassedTestCount() { return passedTestCount; }
    public void setPassedTestCount(int passedTestCount) { this.passedTestCount = passedTestCount; }

    public int getTimeConsumedMillis() { return timeConsumedMillis; }
    public void setTimeConsumedMillis(int timeConsumedMillis) { this.timeConsumedMillis = timeConsumedMillis; }

    public long getMemoryConsumedBytes() { return memoryConsumedBytes; }
    public void setMemoryConsumedBytes(long memoryConsumedBytes) { this.memoryConsumedBytes = memoryConsumedBytes; }

    public boolean isAccepted() {
        return "OK".equalsIgnoreCase(verdict);
    }
}

