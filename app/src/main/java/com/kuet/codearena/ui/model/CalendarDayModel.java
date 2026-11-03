package com.kuet.codearena.ui.model;

/**
 * Model representing a single calendar grid cell, date indicators, and dot event statuses.
 */
public class CalendarDayModel {
    private final int day;
    private final boolean isCurrentMonth;
    private final String dateKey; // yyyyMMdd
    private boolean hasContest;
    private boolean hasUpsolve;
    private boolean hasSolve;
    private boolean isSelected;

    public CalendarDayModel(int day, boolean isCurrentMonth, String dateKey) {
        this.day = day;
        this.isCurrentMonth = isCurrentMonth;
        this.dateKey = dateKey;
    }

    public int getDay() {
        return day;
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public String getDateKey() {
        return dateKey;
    }

    public boolean hasContest() {
        return hasContest;
    }

    public void setHasContest(boolean hasContest) {
        this.hasContest = hasContest;
    }

    public boolean hasUpsolve() {
        return hasUpsolve;
    }

    public void setHasUpsolve(boolean hasUpsolve) {
        this.hasUpsolve = hasUpsolve;
    }

    public boolean hasSolve() {
        return hasSolve;
    }

    public void setHasSolve(boolean hasSolve) {
        this.hasSolve = hasSolve;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

