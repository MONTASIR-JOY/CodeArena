package com.kuet.codearena.data.remote;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.data.remote.model.Platform;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * PlatformSource implementation for AtCoder using the Kenkoooo AtCoder Problems community API.
 */
public class AtCoderPlatformSource implements PlatformSource {

    @Override
    public Platform getPlatform() {
        return Platform.ATCODER;
    }

    @Override
    public List<ContestEntity> fetchUpcomingContests() throws Exception {
        List<ContestEntity> list = new ArrayList<>();
        URL url = new URL(Endpoints.ATCODER_BASE_URL + Endpoints.ATCODER_CONTESTS);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        if (conn.getResponseCode() == 200) {
            JsonElement root = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()));
            if (root.isJsonArray()) {
                JsonArray array = root.getAsJsonArray();
                long nowSec = System.currentTimeMillis() / 1000L;

                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();
                    long startSec = obj.has("start_epoch_second") ? obj.get("start_epoch_second").getAsLong() : 0;
                    long duration = obj.has("duration_second") ? obj.get("duration_second").getAsLong() : 7200;
                    String title = obj.has("title") ? obj.get("title").getAsString() : "AtCoder Contest";
                    String contestIdStr = obj.has("id") ? obj.get("id").getAsString() : "atcoder";

                    if (startSec > nowSec) {
                        int numericId = Math.abs(contestIdStr.hashCode());
                        String webUrl = "https://atcoder.jp/contests/" + contestIdStr;
                        list.add(new ContestEntity(
                                numericId,
                                title,
                                "AtCoder",
                                "BEFORE",
                                duration,
                                startSec,
                                "AtCoder",
                                Platform.ATCODER.name(),
                                webUrl
                        ));
                    }
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

        String endpoint = Endpoints.ATCODER_BASE_URL + Endpoints.ATCODER_SUBMISSIONS + "?user=" + handle + "&from_second=0";
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        if (conn.getResponseCode() == 200) {
            JsonElement root = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()));
            if (root.isJsonArray()) {
                JsonArray array = root.getAsJsonArray();
                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();
                    String result = obj.has("result") ? obj.get("result").getAsString() : "";
                    if ("AC".equalsIgnoreCase(result)) {
                        String problemId = obj.has("problem_id") ? obj.get("problem_id").getAsString() : "at_problem";
                        long epochSec = obj.has("epoch_second") ? obj.get("epoch_second").getAsLong() : 0;
                        int point = obj.has("point") ? (int) obj.get("point").getAsDouble() : 300;

                        solves.add(new SolveEntity(
                                Platform.ATCODER.name(),
                                "AT-" + problemId,
                                problemId,
                                Math.max(800, point * 3), // Estimated rating conversion
                                "atcoder",
                                epochSec * 1000L
                        ));
                    }
                }
            }
        }
        conn.disconnect();
        return solves;
    }
}

