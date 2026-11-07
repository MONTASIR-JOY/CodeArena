package com.codearena.util;

import com.codearena.data.db.entity.TagXpEntity;
import com.codearena.data.model.CfProblem;
import com.codearena.data.model.CfSubmission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MasteryCalculator: Computes topic mastery and XP progression from Codeforces submission history.
 */
public class MasteryCalculator {

    public static final List<String> ALL_CODEFORCES_TAGS = Arrays.asList(
        "2-sat", "binary search", "bitmasks", "brute force", "chinese remainder theorem",
        "combinatorics", "constructive algorithms", "data structures", "dfs and similar",
        "divide and conquer", "dp", "dsu", "expression parsing", "fft", "flows", "games",
        "geometry", "graph matchings", "graphs", "greedy", "hashing", "implementation",
        "interactive", "math", "matrices", "meet-in-the-middle", "number theory",
        "probabilities", "schedules", "shortest paths", "sortings", "string suffix structures",
        "strings", "ternary search", "trees", "two pointers"
    );

    private static final long FIRST_GYM_ID = 100_000L;

    public static class Result {
        private final List<TagXpEntity> tags;
        private final int solvedProblems;

        public Result(List<TagXpEntity> tags, int solvedProblems) {
            this.tags = tags != null ? tags : new ArrayList<>();
            this.solvedProblems = solvedProblems;
        }

        public List<TagXpEntity> getTags() { return tags; }
        public int getSolvedProblems() { return solvedProblems; }
    }

    public static int xpFor(Integer rating) {
        return Math.max(25, (rating != null ? rating : 1000) / 10);
    }

    public static String problemIdentity(CfProblem problem) {
        if (problem == null) return "";
        String name = problem.getName() != null ? problem.getName().trim().toLowerCase() : "";
        int rating = problem.getRating() != null ? problem.getRating() : 0;
        List<String> tags = new ArrayList<>();
        if (problem.getTags() != null) {
            for (String t : problem.getTags()) {
                if (t != null) tags.add(t.trim().toLowerCase());
            }
        }
        Collections.sort(tags);
        return name + "|" + rating + "|" + tags.toString();
    }

    public static Result compute(List<CfSubmission> submissions) {
        if (submissions == null || submissions.isEmpty()) {
            return new Result(Collections.emptyList(), 0);
        }

        Map<String, CfProblem> solved = new LinkedHashMap<>();
        for (CfSubmission s : submissions) {
            if (!s.isAccepted()) continue;
            Long cid = s.getProblem() != null ? s.getProblem().getContestId() : s.getContestId();
            if (cid != null && cid >= FIRST_GYM_ID) continue;
            if (s.getProblem() != null) {
                String id = problemIdentity(s.getProblem());
                if (!solved.containsKey(id)) {
                    solved.put(id, s.getProblem());
                }
            }
        }

        Map<String, Integer> xpMap = new HashMap<>();
        Map<String, Integer> countMap = new HashMap<>();

        for (CfProblem problem : solved.values()) {
            int earned = xpFor(problem.getRating());
            Set<String> distinctTags = new HashSet<>();
            if (problem.getTags() != null) {
                for (String tag : problem.getTags()) {
                    if (tag != null) {
                        String cleaned = tag.trim().toLowerCase();
                        if (!cleaned.isEmpty()) distinctTags.add(cleaned);
                    }
                }
            }
            for (String tag : distinctTags) {
                xpMap.put(tag, xpMap.getOrDefault(tag, 0) + earned);
                countMap.put(tag, countMap.getOrDefault(tag, 0) + 1);
            }
        }

        List<TagXpEntity> tagEntities = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : xpMap.entrySet()) {
            tagEntities.add(new TagXpEntity(entry.getKey(), entry.getValue(), countMap.getOrDefault(entry.getKey(), 0)));
        }

        Collections.sort(tagEntities, (a, b) -> {
            int cmp = Integer.compare(b.getXp(), a.getXp());
            if (cmp != 0) return cmp;
            return a.getTag().compareTo(b.getTag());
        });

        return new Result(tagEntities, solved.size());
    }
}

