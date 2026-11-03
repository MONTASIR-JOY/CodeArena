package com.kuet.codearena.ui.model;

/**
 * Model representing calculated mastery metrics and tier assessment for a problem category tag.
 */
public class TagMasteryModel {
    private final String tag;
    private final int solvedCount;
    private final int averageRating;
    private final double accuracy; // percentage 0-100%
    private final String level; // Beginner, Intermediate, Advanced
    private final int unsolvedCount;

    public TagMasteryModel(String tag, int solvedCount, int averageRating, double accuracy, String level, int unsolvedCount) {
        this.tag = tag;
        this.solvedCount = solvedCount;
        this.averageRating = averageRating;
        this.accuracy = accuracy;
        this.level = level;
        this.unsolvedCount = unsolvedCount;
    }

    public String getTag() {
        return tag;
    }

    public int getSolvedCount() {
        return solvedCount;
    }

    public int getAverageRating() {
        return averageRating;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public String getLevel() {
        return level;
    }

    public int getUnsolvedCount() {
        return unsolvedCount;
    }
}

