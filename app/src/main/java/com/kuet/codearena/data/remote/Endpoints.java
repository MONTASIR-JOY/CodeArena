package com.kuet.codearena.data.remote;

/**
 * Central repository holding all external API base URLs and endpoint paths.
 */
public final class Endpoints {
    private Endpoints() {}

    // Codeforces Official API
    public static final String CODEFORCES_BASE_URL = "https://codeforces.com/api/";
    public static final String CF_CONTEST_LIST = "contest.list";
    public static final String CF_CONTEST_STANDINGS = "contest.standings";
    public static final String CF_USER_STATUS = "user.status";
    public static final String CF_USER_INFO = "user.info";
    public static final String CF_PROBLEMSET_PROBLEMS = "problemset.problems";
    public static final String CF_PROBLEM_WEB_BASE = "https://codeforces.com/contest/";

    // AtCoder Community API (Kenkoooo)
    public static final String ATCODER_BASE_URL = "https://kenkoooo.com/";
    public static final String ATCODER_CONTESTS = "atcoder/resources/contests.json";
    public static final String ATCODER_SUBMISSIONS = "atcoder/atcoder-api/v3/user/submissions";

    // LeetCode Unofficial Public GraphQL API
    public static final String LEETCODE_BASE_URL = "https://leetcode.com/";
    public static final String LEETCODE_GRAPHQL = "graphql";

    // CLIST.BY Competitive Programming Calendar API
    public static final String CLIST_BASE_URL = "https://clist.by/api/v2/";
    public static final String CLIST_CONTESTS = "contest/";

    // Google Gemini REST API
    public static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/";
    public static final String GEMINI_MODEL = "gemini-1.5-flash";
}
