package com.codearena.util;

import com.codearena.data.db.entity.ProblemResultEntity;
import com.codearena.data.db.entity.VirtualRunEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AnalyticsCalculator: Aggregates historical virtual contest runs and problem outcomes.
 */
public class AnalyticsCalculator {

    public static class TagStat {
        private final String tag;
        private final int attempted;
        private final int solved;

        public TagStat(String tag, int attempted, int solved) {
            this.tag = tag;
            this.attempted = attempted;
            this.solved = solved;
        }

        public String getTag() { return tag; }
        public int getAttempted() { return attempted; }
        public int getSolved() { return solved; }
        public double getSolveRate() { return attempted == 0 ? 0.0 : (double) solved / attempted; }
    }

    public static class RunPoint {
        private final long runId;
        private final long startMillis;
        private final Double avgSolveMinutes;
        private final double solvedRatio;

        public RunPoint(long runId, long startMillis, Double avgSolveMinutes, double solvedRatio) {
            this.runId = runId;
            this.startMillis = startMillis;
            this.avgSolveMinutes = avgSolveMinutes;
            this.solvedRatio = solvedRatio;
        }

        public long getRunId() { return runId; }
        public long getStartMillis() { return startMillis; }
        public Double getAvgSolveMinutes() { return avgSolveMinutes; }
        public double getSolvedRatio() { return solvedRatio; }
    }

    public static class WeekSummary {
        private final int runs;
        private final int solved;
        private final int attempted;

        public WeekSummary(int runs, int solved, int attempted) {
            this.runs = runs;
            this.solved = solved;
            this.attempted = attempted;
        }

        public int getRuns() { return runs; }
        public int getSolved() { return solved; }
        public int getAttempted() { return attempted; }
    }

    public static List<TagStat> tagStats(List<ProblemResultEntity> results) {
        if (results == null) return Collections.emptyList();
        Map<String, Integer> attempted = new HashMap<>();
        Map<String, Integer> solved = new HashMap<>();

        for (ProblemResultEntity result : results) {
            if (result.getTags() != null) {
                for (String tag : result.getTags()) {
                    attempted.put(tag, attempted.getOrDefault(tag, 0) + 1);
                    if (result.isSolved()) {
                        solved.put(tag, solved.getOrDefault(tag, 0) + 1);
                    }
                }
            }
        }

        List<TagStat> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : attempted.entrySet()) {
            list.add(new TagStat(entry.getKey(), entry.getValue(), solved.getOrDefault(entry.getKey(), 0)));
        }

        Collections.sort(list, (a, b) -> {
            int cmp = Integer.compare(b.getAttempted(), a.getAttempted());
            if (cmp != 0) return cmp;
            return a.getTag().compareTo(b.getTag());
        });
        return list;
    }

    public static List<RunPoint> runTrend(List<VirtualRunEntity> runs, List<ProblemResultEntity> results) {
        if (runs == null) return Collections.emptyList();
        Map<Long, List<ProblemResultEntity>> byRun = new HashMap<>();
        if (results != null) {
            for (ProblemResultEntity r : results) {
                byRun.computeIfAbsent(r.getRunId(), k -> new ArrayList<>()).add(r);
            }
        }

        List<VirtualRunEntity> sortedRuns = new ArrayList<>(runs);
        Collections.sort(sortedRuns, (a, b) -> Long.compare(a.getStartTimeEpochMillis(), b.getStartTimeEpochMillis()));

        List<RunPoint> points = new ArrayList<>();
        for (VirtualRunEntity run : sortedRuns) {
            List<ProblemResultEntity> rows = byRun.getOrDefault(run.getRunId(), Collections.emptyList());
            List<ProblemResultEntity> solved = new ArrayList<>();
            for (ProblemResultEntity r : rows) {
                if (r.isSolved() && r.getSolveTimeSeconds() != null) {
                    solved.add(r);
                }
            }

            Double avgMinutes = null;
            if (!solved.isEmpty()) {
                double totalMin = 0.0;
                for (ProblemResultEntity s : solved) {
                    totalMin += s.getSolveTimeSeconds() / 60.0;
                }
                avgMinutes = totalMin / solved.size();
            }

            double ratio = rows.isEmpty() ? 0.0 : (double) solved.size() / rows.size();
            points.add(new RunPoint(run.getRunId(), run.getStartTimeEpochMillis(), avgMinutes, ratio));
        }
        return points;
    }

    public static WeekSummary summary(List<VirtualRunEntity> runs, long fromMillis, long toMillis) {
        if (runs == null) return new WeekSummary(0, 0, 0);
        int runCount = 0;
        int solvedCount = 0;
        int attemptedCount = 0;

        for (VirtualRunEntity run : runs) {
            if (run.getStartTimeEpochMillis() >= fromMillis && run.getStartTimeEpochMillis() < toMillis) {
                runCount++;
                solvedCount += run.getSolvedCount();
                attemptedCount += run.getTotalProblemsCount();
            }
        }
        return new WeekSummary(runCount, solvedCount, attemptedCount);
    }
}

