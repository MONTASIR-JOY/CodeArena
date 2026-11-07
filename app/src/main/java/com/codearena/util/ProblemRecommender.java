package com.codearena.util;

import com.codearena.data.model.CfProblem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ProblemRecommender: Recommends practice problems matching user's rating and weakest topics.
 */
public class ProblemRecommender {

    public static final List<String> TOPIC_TAGS = Arrays.asList(
        "dp", "greedy", "math", "graphs", "binary search", "data structures", "implementation",
        "constructive algorithms", "strings", "number theory", "trees", "dfs and similar",
        "brute force", "sortings", "two pointers", "bitmasks", "dsu", "shortest paths",
        "combinatorics", "geometry"
    );

    public static String problemKey(Long contestId, String index) {
        return (contestId != null ? contestId : 0) + (index != null ? index : "");
    }

    public static List<String> weakestTags(Map<String, Integer> xpByTag, int count) {
        List<String> sorted = new ArrayList<>(TOPIC_TAGS);
        Collections.sort(sorted, (a, b) -> {
            int xpA = xpByTag != null ? xpByTag.getOrDefault(a, 0) : 0;
            int xpB = xpByTag != null ? xpByTag.getOrDefault(b, 0) : 0;
            return Integer.compare(xpA, xpB);
        });
        return sorted.subList(0, Math.min(count, sorted.size()));
    }

    public static int targetRating(Integer userRating) {
        int base = userRating != null ? userRating : 800;
        int rounded = ((base + 50) / 100) * 100;
        return Math.max(800, Math.min(3500, rounded));
    }

    private static class ScoredProblem {
        final CfProblem problem;
        final int matches;
        final double score;

        ScoredProblem(CfProblem problem, int matches, double score) {
            this.problem = problem;
            this.matches = matches;
            this.score = score;
        }
    }

    public static List<CfProblem> recommend(
        List<CfProblem> problems,
        Map<String, Integer> solvedCounts,
        Set<String> solvedKeys,
        Integer userRating,
        List<String> focusTags,
        int limit
    ) {
        if (problems == null) return Collections.emptyList();
        int target = targetRating(userRating);
        Set<String> focus = focusTags != null ? new HashSet<>(focusTags) : Collections.emptySet();
        Set<String> solved = solvedKeys != null ? solvedKeys : Collections.emptySet();

        List<ScoredProblem> candidates = new ArrayList<>();
        for (CfProblem p : problems) {
            if (p.getContestId() == null || p.getRating() == null) continue;
            if (Math.abs(p.getRating() - target) > 200) continue;
            String key = problemKey(p.getContestId(), p.getIndex());
            if (solved.contains(key)) continue;

            int matches = 0;
            if (p.getTags() != null) {
                for (String t : p.getTags()) {
                    if (focus.contains(t)) matches++;
                }
            }

            int count = solvedCounts != null ? solvedCounts.getOrDefault(key, 0) : 0;
            double popularity = Math.log(1.0 + count);
            double distance = Math.abs(p.getRating() - target) / 100.0;
            double score = matches * 10.0 + popularity - distance;

            if (focus.isEmpty() || matches > 0) {
                candidates.add(new ScoredProblem(p, matches, score));
            }
        }

        Collections.sort(candidates, (a, b) -> Double.compare(b.score, a.score));

        List<CfProblem> result = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, candidates.size()); i++) {
            result.add(candidates.get(i).problem);
        }
        return result;
    }
}

