package com.kuet.codearena.data.remote;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.data.remote.model.Platform;

import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * PlatformSource implementation for LeetCode using public GraphQL queries.
 * Difficulty Mapping:
 * - Easy: ~1000 approximate rating
 * - Medium: ~1500 approximate rating
 * - Hard: ~2000 approximate rating
 */
public class LeetCodePlatformSource implements PlatformSource {

    @Override
    public Platform getPlatform() {
        return Platform.LEETCODE;
    }

    @Override
    public List<ContestEntity> fetchUpcomingContests() throws Exception {
        List<ContestEntity> list = new ArrayList<>();
        // LeetCode GraphQL query for upcoming contests
        String jsonPayload = "{\"query\":\"query { upcomingContests { title titleSlug startTime duration } }\"}";

        URL url = new URL(Endpoints.LEETCODE_BASE_URL + Endpoints.LEETCODE_GRAPHQL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() == 200) {
            JsonElement root = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()));
            JsonObject data = root.getAsJsonObject().getAsJsonObject("data");
            if (data != null && data.has("upcomingContests")) {
                JsonArray array = data.getAsJsonArray("upcomingContests");
                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();
                    String title = obj.get("title").getAsString();
                    String slug = obj.get("titleSlug").getAsString();
                    long startSec = obj.get("startTime").getAsLong();
                    long duration = obj.get("duration").getAsLong();

                    int id = Math.abs(slug.hashCode());
                    String webUrl = "https://leetcode.com/contest/" + slug;
                    list.add(new ContestEntity(
                            id,
                            title,
                            "LeetCode",
                            "BEFORE",
                            duration,
                            startSec,
                            "LeetCode Contest",
                            Platform.LEETCODE.name(),
                            webUrl
                    ));
                }
            }
        }
        conn.disconnect();
        return list;
    }

    @Override
    public List<SolveEntity> fetchUserAcceptedSubmissions(String handle) throws Exception {
        List<SolveEntity> solves = new ArrayList<>();
        if (handle == null || handle.trim().isEmpty()) return solves;

        // LeetCode GraphQL query for recent accepted submissions
        String query = String.format(
                "{\"query\":\"query { recentAcSubmissionList(username: \\\"%s\\\", limit: 30) { id title titleSlug timestamp } }\"}",
                handle
        );

        URL url = new URL(Endpoints.LEETCODE_BASE_URL + Endpoints.LEETCODE_GRAPHQL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(query.getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() == 200) {
            JsonElement root = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()));
            JsonObject data = root.getAsJsonObject().getAsJsonObject("data");
            if (data != null && data.has("recentAcSubmissionList")) {
                JsonArray array = data.getAsJsonArray("recentAcSubmissionList");
                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();
                    String title = obj.get("title").getAsString();
                    String slug = obj.get("titleSlug").getAsString();
                    long timestamp = obj.get("timestamp").getAsLong() * 1000L;

                    // Approximate difficulty mapping: default Medium (~1500)
                    int approxRating = 1500;
                    solves.add(new SolveEntity(
                            Platform.LEETCODE.name(),
                            "LC-" + slug,
                            title,
                            approxRating,
                            "algorithms",
                            timestamp
                    ));
                }
            }
        }
        conn.disconnect();
        return solves;
    }
}

