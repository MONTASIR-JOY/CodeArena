package com.codearena.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyChallengeState implements Serializable {
    private String date; // "yyyy-MM-dd"
    private int streak;
    private int longestStreak;
    private int totalCompleted;
    private List<DailyProblemItem> problems = new ArrayList<>();
    private Map<String, String> history = new HashMap<>(); // date -> "ALL", "PARTIAL", "MISSED", "IN_PROGRESS"
    private long swapSeed;

    public DailyChallengeState() {
        this.date = "";
        this.streak = 0;
        this.longestStreak = 0;
        this.totalCompleted = 0;
        this.swapSeed = 0;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public int getTotalCompleted() {
        return totalCompleted;
    }

    public void setTotalCompleted(int totalCompleted) {
        this.totalCompleted = totalCompleted;
    }

    public List<DailyProblemItem> getProblems() {
        if (problems == null) problems = new ArrayList<>();
        return problems;
    }

    public void setProblems(List<DailyProblemItem> problems) {
        this.problems = problems;
    }

    public Map<String, String> getHistory() {
        if (history == null) history = new HashMap<>();
        return history;
    }

    public void setHistory(Map<String, String> history) {
        this.history = history;
    }

    public long getSwapSeed() {
        return swapSeed;
    }

    public void setSwapSeed(long swapSeed) {
        this.swapSeed = swapSeed;
    }

    public int getSolvedCount() {
        if (problems == null) return 0;
        int count = 0;
        for (DailyProblemItem item : problems) {
            if (item.isSolved()) count++;
        }
        return count;
    }

    public boolean isAllSolved() {
        if (problems == null || problems.isEmpty()) return false;
        return getSolvedCount() == problems.size();
    }
}

