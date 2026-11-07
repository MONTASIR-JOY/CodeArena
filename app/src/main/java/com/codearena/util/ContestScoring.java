package com.codearena.util;

import com.codearena.data.db.entity.ProblemResultEntity;
import com.codearena.data.model.CfRanklistRow;
import com.codearena.data.model.CfSubmission;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ContestScoring: Scoring algorithms for virtual contests (CF and ICPC styles).
 */
public class ContestScoring {

    public static final int ICPC_WRONG_PENALTY_MINUTES = 20;
    private static final double CF_MIN_POINTS_FRACTION = 0.3;
    private static final double CF_DECAY_MINUTES = 250.0;
    private static final double CF_WRONG_PENALTY_POINTS = 50.0;

    private static final Set<String> NON_PENALISED_VERDICTS = new HashSet<>(Arrays.asList(
        "OK", "TESTING", "COMPILATION_ERROR", "SKIPPED", "REJECTED", "CHALLENGED"
    ));

    public static class ProblemOutcome {
        private final boolean solved;
        private final Long solveTimeSeconds;
        private final int wrongAttempts;

        public ProblemOutcome(boolean solved, Long solveTimeSeconds, int wrongAttempts) {
            this.solved = solved;
            this.solveTimeSeconds = solveTimeSeconds;
            this.wrongAttempts = wrongAttempts;
        }

        public boolean isSolved() { return solved; }
        public Long getSolveTimeSeconds() { return solveTimeSeconds; }
        public int getWrongAttempts() { return wrongAttempts; }
    }

    public static class Score {
        private final double points;
        private final int penaltyMinutes;
        private final int solvedCount;

        public Score(double points, int penaltyMinutes, int solvedCount) {
            this.points = points;
            this.penaltyMinutes = penaltyMinutes;
            this.solvedCount = solvedCount;
        }

        public double getPoints() { return points; }
        public int getPenaltyMinutes() { return penaltyMinutes; }
        public int getSolvedCount() { return solvedCount; }
    }

    public static boolean isPenalisedVerdict(String verdict) {
        if (verdict == null) return false;
        return !NON_PENALISED_VERDICTS.contains(verdict.toUpperCase());
    }

    public static ProblemOutcome analyzeProblem(List<CfSubmission> submissionsForProblem, long startEpochSeconds) {
        if (submissionsForProblem == null || submissionsForProblem.isEmpty()) {
            return new ProblemOutcome(false, null, 0);
        }

        List<CfSubmission> filtered = new java.util.ArrayList<>();
        for (CfSubmission s : submissionsForProblem) {
            if (s.getCreationTimeSeconds() >= startEpochSeconds) {
                filtered.add(s);
            }
        }
        Collections.sort(filtered, (a, b) -> Long.compare(a.getCreationTimeSeconds(), b.getCreationTimeSeconds()));

        int wrong = 0;
        for (CfSubmission submission : filtered) {
            if (submission.isAccepted()) {
                return new ProblemOutcome(
                    true,
                    submission.getCreationTimeSeconds() - startEpochSeconds,
                    wrong
                );
            }
            if (isPenalisedVerdict(submission.getVerdict())) {
                wrong++;
            }
        }
        return new ProblemOutcome(false, null, wrong);
    }

    public static double cfProblemPoints(double maxPoints, long solveSeconds, int wrongAttempts) {
        double minutes = solveSeconds / 60.0;
        double raw = maxPoints * (1.0 - minutes / CF_DECAY_MINUTES) - CF_WRONG_PENALTY_POINTS * wrongAttempts;
        return Math.floor(Math.max(raw, maxPoints * CF_MIN_POINTS_FRACTION));
    }

    public static Score computeScore(List<ProblemResultEntity> results,
                                     Map<String, Double> problemMaxPoints,
                                     String contestType) {
        if (results == null) {
            return new Score(0.0, 0, 0);
        }

        List<ProblemResultEntity> solved = new java.util.ArrayList<>();
        for (ProblemResultEntity r : results) {
            if (r.isSolved()) solved.add(r);
        }

        boolean allHavePoints = true;
        for (ProblemResultEntity r : solved) {
            Double pts = problemMaxPoints != null ? problemMaxPoints.get(r.getProblemIndex()) : null;
            if (pts == null || pts <= 0.0) {
                allHavePoints = false;
                break;
            }
        }

        boolean isCfStyle = "CF".equalsIgnoreCase(contestType) && allHavePoints;

        if (isCfStyle) {
            double points = 0.0;
            for (ProblemResultEntity r : solved) {
                Double maxPts = problemMaxPoints != null ? problemMaxPoints.get(r.getProblemIndex()) : null;
                long solveSec = r.getSolveTimeSeconds() != null ? r.getSolveTimeSeconds() : 0L;
                points += cfProblemPoints(maxPts != null ? maxPts : 0.0, solveSec, r.getWrongAttempts());
            }
            return new Score(points, 0, solved.size());
        } else {
            int penalty = 0;
            for (ProblemResultEntity r : solved) {
                long solveSec = r.getSolveTimeSeconds() != null ? r.getSolveTimeSeconds() : 0L;
                penalty += (int)(solveSec / 60) + ICPC_WRONG_PENALTY_MINUTES * r.getWrongAttempts();
            }
            return new Score(solved.size(), penalty, solved.size());
        }
    }

    public static int estimateRank(Score score, List<CfRanklistRow> rows) {
        if (rows == null || rows.isEmpty()) return 1;
        int better = 0;
        for (CfRanklistRow row : rows) {
            if (row.getPoints() > score.getPoints() ||
               (row.getPoints() == score.getPoints() && row.getPenalty() < score.getPenaltyMinutes())) {
                better++;
            }
        }
        return better + 1;
    }

    public static int estimatePerformance(int rank, int totalParticipants) {
        int total = Math.max(totalParticipants, Math.max(rank, 1));
        double percentile = 1.0 - (rank - 1.0) / total;
        int perf = (int) Math.round(800 + percentile * 2200);
        return Math.max(800, Math.min(3000, perf));
    }
}

