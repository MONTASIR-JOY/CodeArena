package com.codearena.util;

import com.codearena.data.model.CfProblem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DailyChallengePlanner {

    public static final int MIN_SIZE = 3;
    public static final int MAX_SIZE = 10;

    public static List<Integer> ladder(int count, Integer userRating) {
        int n = Math.max(MIN_SIZE, Math.min(MAX_SIZE, count));
        int base = ProblemRecommender.targetRating(userRating);
        List<Integer> rungs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double offset = -200.0 + 400.0 * i / (n - 1);
            int rung = (int) Math.round((base + offset) / 100.0) * 100;
            rungs.add(Math.max(800, Math.min(3500, rung)));
        }
        return rungs;
    }

    public static List<CfProblem> plan(
        List<CfProblem> problems,
        Map<String, Integer> solvedCounts,
        Set<String> solvedKeys,
        Integer userRating,
        int count,
        List<String> weakTags,
        long seed
    ) {
        if (problems == null) return new ArrayList<>();
        Random random = new Random(seed);
        Set<String> weak = weakTags != null ? new HashSet<>(weakTags) : new HashSet<>();
        Set<String> solved = solvedKeys != null ? solvedKeys : new HashSet<>();

        List<CfProblem> usable = new ArrayList<>();
        for (CfProblem p : problems) {
            if (p.getContestId() != null && p.getRating() != null) {
                String key = ProblemRecommender.problemKey(p.getContestId(), p.getIndex());
                if (!solved.contains(key)) {
                    usable.add(p);
                }
            }
        }

        Map<String, CfProblem> chosen = new LinkedHashMap<>();
        List<Integer> ladderRungs = ladder(count, userRating);
        List<Integer> tolerances = Arrays.asList(0, 100, 200, 300);

        for (int rung : ladderRungs) {
            for (int tolerance : tolerances) {
                CfProblem bestPick = null;
                double bestScore = -Double.MAX_VALUE;

                for (CfProblem p : usable) {
                    if (Math.abs(p.getRating() - rung) <= tolerance) {
                        String key = ProblemRecommender.problemKey(p.getContestId(), p.getIndex());
                        if (!chosen.containsKey(key)) {
                            int matches = 0;
                            if (p.getTags() != null) {
                                for (String t : p.getTags()) {
                                    if (weak.contains(t)) matches++;
                                }
                            }
                            int sCount = solvedCounts != null ? solvedCounts.getOrDefault(key, 0) : 0;
                            double popularity = Math.log(1.0 + sCount);
                            double score = matches * 10.0 + popularity - Math.abs(p.getRating() - rung) / 50.0 + random.nextDouble() * 3.0;

                            if (score > bestScore) {
                                bestScore = score;
                                bestPick = p;
                            }
                        }
                    }
                }

                if (bestPick != null) {
                    chosen.put(ProblemRecommender.problemKey(bestPick.getContestId(), bestPick.getIndex()), bestPick);
                    break;
                }
            }
        }
        return new ArrayList<>(chosen.values());
    }
}

