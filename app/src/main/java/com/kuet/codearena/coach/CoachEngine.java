package com.kuet.codearena.coach;

import android.content.Context;

import com.kuet.codearena.data.db.AppDatabase;
import com.kuet.codearena.data.db.dao.ContestDao;
import com.kuet.codearena.data.db.dao.ProblemDao;
import com.kuet.codearena.data.db.dao.SolveDao;
import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.db.entity.ProblemEntity;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.ui.model.TagMasteryModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Rule-based offline coaching engine generating targeted practice ladders and 7-day contest participation schedules.
 */
public class CoachEngine {

    public static class CalendarPlanItem {
        private final long dateMs;
        private final String title;
        private final String action; // "JOIN", "SKIP", "UPSOLVE_SESSION"
        private final String reason;

        public CalendarPlanItem(long dateMs, String title, String action, String reason) {
            this.dateMs = dateMs;
            this.title = title;
            this.action = action;
            this.reason = reason;
        }

        public long getDateMs() { return dateMs; }
        public String getTitle() { return title; }
        public String getAction() { return action; }
        public String getReason() { return reason; }
    }

    /**
     * Recommends the top 3 weak tags to practice based on low accuracy, lower average rating, and fewer solves.
     */
    public static List<String> recommendWeakTags(List<TagMasteryModel> stats) {
        List<String> list = new ArrayList<>();
        if (stats == null || stats.isEmpty()) {
            list.add("dp");
            list.add("greedy");
            list.add("graphs");
            return list;
        }

        List<TagMasteryModel> sorted = new ArrayList<>(stats);
        Collections.sort(sorted, (a, b) -> {
            // Rank by weakness score (lower accuracy + lower avg rating + lower solved count)
            double scoreA = (a.getAccuracy() * 0.4) + (a.getAverageRating() * 0.3) + (a.getSolvedCount() * 5.0);
            double scoreB = (b.getAccuracy() * 0.4) + (b.getAverageRating() * 0.3) + (b.getSolvedCount() * 5.0);
            return Double.compare(scoreA, scoreB);
        });

        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            list.add(sorted.get(i).getTag());
        }
        return list;
    }

    /**
     * Builds a 5-step practice ladder for a tag at ratings:
     * (level - 100, level, level + 100, level + 200, level + 300)
     * where level = user's practice rating rounded to 100, preferring higher solvedCount.
     * Excludes already solved problems.
     */
    public static List<ProblemEntity> buildPracticeLadder(Context context, String tag, double practiceRating) {
        AppDatabase db = AppDatabase.getInstance(context);
        ProblemDao problemDao = db.problemDao();
        SolveDao solveDao = db.solveDao();

        // Collect all solved problem keys to exclude them
        List<SolveEntity> solves = solveDao.getAllSolvesSync();
        Set<String> solvedKeys = new HashSet<>();
        if (solves != null) {
            for (SolveEntity s : solves) {
                solvedKeys.add(s.getProblemId());
            }
        }

        int level = (int) (Math.round(practiceRating / 100.0) * 100);
        int[] targetRatings = new int[]{
                Math.max(800, level - 100),
                Math.max(800, level),
                Math.max(900, level + 100),
                Math.max(1000, level + 200),
                Math.max(1100, level + 300)
        };

        List<ProblemEntity> ladder = new ArrayList<>();
        for (int targetRating : targetRatings) {
            List<ProblemEntity> candidates = problemDao.getCandidatesByTagAndRating(tag, targetRating - 50, targetRating + 50);
            ProblemEntity chosen = null;

            if (candidates != null) {
                for (ProblemEntity p : candidates) {
                    String cfKey = "CF-" + p.getContestId() + p.getProblemIndex();
                    if (!solvedKeys.contains(cfKey)) {
                        chosen = p;
                        solvedKeys.add(cfKey); // Avoid duplicate in ladder
                        break;
                    }
                }
            }

            // Fallback synthetic problem if database doesn't have cached problems yet for this rating
            if (chosen == null) {
                chosen = new ProblemEntity(
                        1000 + targetRating / 10,
                        "C",
                        tag.toUpperCase() + " Benchmark (" + targetRating + ")",
                        "PROGRAMMING",
                        targetRating,
                        tag,
                        15000
                );
            }
            ladder.add(chosen);
        }

        return ladder;
    }

    /**
     * Builds a 7-day calendar coaching plan: recommendations to join or skip upcoming contests
     * based on user's current practice rating, plus scheduled upsolve study sessions.
     */
    public static List<CalendarPlanItem> build7DayPlan(Context context, double practiceRating) {
        List<CalendarPlanItem> plan = new ArrayList<>();
        AppDatabase db = AppDatabase.getInstance(context);
        ContestDao contestDao = db.contestDao();

        long nowSec = System.currentTimeMillis() / 1000L;
        long sevenDaysSec = nowSec + (7L * 24L * 3600L);
        List<ContestEntity> upcoming = contestDao.getUpcomingContestsSync();

        if (upcoming != null) {
            for (ContestEntity c : upcoming) {
                if (c.getStartTimeSeconds() >= nowSec && c.getStartTimeSeconds() <= sevenDaysSec) {
                    String name = c.getName();
                    String action;
                    String reason;

                    if (name.contains("Div. 1") && !name.contains("Div. 2")) {
                        if (practiceRating < 1900) {
                            action = "SKIP";
                            reason = "Div. 1 problems exceed your current level (~" + (int) practiceRating + "). Focus on Div. 2/3 rounds first.";
                        } else {
                            action = "JOIN";
                            reason = "Excellent match for your rating to push towards Master tier.";
                        }
                    } else if (name.contains("Div. 3") || name.contains("Div. 4")) {
                        if (practiceRating > 1600) {
                            action = "OPTIONAL";
                            reason = "Good for speed practice, but rating gains will be minimal.";
                        } else {
                            action = "JOIN";
                            reason = "High-priority round for solid rating growth and speed drills.";
                        }
                    } else {
                        action = "JOIN";
                        reason = "Recommended round matching your current competitive skill bracket.";
                    }

                    plan.add(new CalendarPlanItem(c.getStartTimeSeconds() * 1000L, c.getName(), action, reason));
                }
            }
        }

        // Add 2 upsolve session recommendations in the next 7 days
        long twoDaysMs = System.currentTimeMillis() + (2L * 24L * 3600L * 1000L);
        long fiveDaysMs = System.currentTimeMillis() + (5L * 24L * 3600L * 1000L);
        plan.add(new CalendarPlanItem(twoDaysMs, "Spaced Repetition Upsolve Block", "UPSOLVE_SESSION",
                "Dedicated 45-min session to clear due queue items before penalty decay."));
        plan.add(new CalendarPlanItem(fiveDaysMs, "Tag Reinforcement Deep Dive", "UPSOLVE_SESSION",
                "Practice ladder problems focusing on your weakest category."));

        return plan;
    }
}

