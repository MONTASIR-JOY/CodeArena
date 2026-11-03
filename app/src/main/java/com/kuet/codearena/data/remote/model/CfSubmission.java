package com.kuet.codearena.data.remote.model;

/**
 * Model representing a Codeforces user submission returned by user.status.
 */
public class CfSubmission {
    private long id;
    private int contestId;
    private long creationTimeSeconds;
    private long relativeTimeSeconds;
    private CfProblem problem;
    private String programmingLanguage;
    private String verdict; // OK, WRONG_ANSWER, TIME_LIMIT_EXCEEDED, etc.
    private int passedTestCount;
    private long timeConsumedMillis;
    private long memoryConsumedBytes;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getContestId() {
        return contestId;
    }

    public void setContestId(int contestId) {
        this.contestId = contestId;
    }

    public long getCreationTimeSeconds() {
        return creationTimeSeconds;
    }

    public void setCreationTimeSeconds(long creationTimeSeconds) {
        this.creationTimeSeconds = creationTimeSeconds;
    }

    public long getRelativeTimeSeconds() {
        return relativeTimeSeconds;
    }

    public void setRelativeTimeSeconds(long relativeTimeSeconds) {
        this.relativeTimeSeconds = relativeTimeSeconds;
    }

    public CfProblem getProblem() {
        return problem;
    }

    public void setProblem(CfProblem problem) {
        this.problem = problem;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public int getPassedTestCount() {
        return passedTestCount;
    }

    public void setPassedTestCount(int passedTestCount) {
        this.passedTestCount = passedTestCount;
    }

    public long getTimeConsumedMillis() {
        return timeConsumedMillis;
    }

    public void setTimeConsumedMillis(long timeConsumedMillis) {
        this.timeConsumedMillis = timeConsumedMillis;
    }

    public long getMemoryConsumedBytes() {
        return memoryConsumedBytes;
    }

    public void setMemoryConsumedBytes(long memoryConsumedBytes) {
        this.memoryConsumedBytes = memoryConsumedBytes;
    }
}
