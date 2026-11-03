package com.kuet.codearena.data.remote;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kuet.codearena.BuildConfig;
import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.remote.model.Platform;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * PlatformSource aggregating upcoming contests across platforms via clist.by API with API key authentication.
 */
public class ClistPlatformSource {
    private static final String TAG = "ClistPlatformSource";

    public static boolean isConfigured() {
        return BuildConfig.CLIST_USERNAME != null && !BuildConfig.CLIST_USERNAME.trim().isEmpty() &&
                BuildConfig.CLIST_API_KEY != null && !BuildConfig.CLIST_API_KEY.trim().isEmpty();
    }

    public List<ContestEntity> fetchUpcomingContests() throws Exception {
        List<ContestEntity> list = new ArrayList<>();
        if (!isConfigured()) {
            Log.d(TAG, "CLIST API credentials not configured. Using direct platform sources.");
            return list;
        }

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String nowIso = isoFormat.format(new Date());

        String endpoint = Endpoints.CLIST_BASE_URL + Endpoints.CLIST_CONTESTS +
                "?upcoming=true&order_by=start&limit=50&start__gte=" + nowIso;

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "ApiKey " + BuildConfig.CLIST_USERNAME + ":" + BuildConfig.CLIST_API_KEY);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        if (conn.getResponseCode() == 200) {
            JsonElement root = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()));
            JsonObject obj = root.getAsJsonObject();
            if (obj.has("objects")) {
                JsonArray objects = obj.getAsJsonArray("objects");
                for (JsonElement el : objects) {
                    JsonObject c = el.getAsJsonObject();
                    String event = c.has("event") ? c.get("event").getAsString() : "Contest";
                    String href = c.has("href") ? c.get("href").getAsString() : "";
                    long duration = c.has("duration") ? c.get("duration").getAsLong() : 7200;
                    String resource = c.has("resource") ? c.get("resource").getAsJsonObject().get("name").getAsString().toLowerCase() : "";

                    long startSec = 0;
                    if (c.has("start")) {
                        try {
                            Date d = isoFormat.parse(c.get("start").getAsString());
                            if (d != null) startSec = d.getTime() / 1000L;
                        } catch (Exception ignored) {}
                    }

                    String platformStr = Platform.CODEFORCES.name();
                    if (resource.contains("codeforces")) platformStr = Platform.CODEFORCES.name();
                    else if (resource.contains("atcoder")) platformStr = Platform.ATCODER.name();
                    else if (resource.contains("leetcode")) platformStr = Platform.LEETCODE.name();
                    else if (resource.contains("codechef")) platformStr = Platform.CODECHEF.name();
                    else continue;

                    int id = Math.abs((event + startSec).hashCode());
                    list.add(new ContestEntity(
                            id,
                            event,
                            platformStr,
                            "BEFORE",
                            duration,
                            startSec,
                            platformStr,
                            platformStr,
                            href
                    ));
                }
            }
        }
        conn.disconnect();
        return list;
    }
}
