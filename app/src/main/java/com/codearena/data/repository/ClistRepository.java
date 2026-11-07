package com.codearena.data.repository;

import com.codearena.BuildConfig;
import com.codearena.data.api.ApiClient;
import com.codearena.data.api.ClistApi;
import com.codearena.data.db.AppDatabase;
import com.codearena.data.model.CfContest;
import com.codearena.data.model.ClistContest;
import com.codearena.data.model.ClistResponse;
import com.google.gson.JsonElement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Response;

public class ClistRepository {

    public static final long ID_OFFSET = 10_000_000_000L;

    public static final Map<String, String> PLATFORMS = new HashMap<>();
    static {
        PLATFORMS.put("atcoder.jp", "AtCoder");
        PLATFORMS.put("codechef.com", "CodeChef");
        PLATFORMS.put("leetcode.com", "LeetCode");
    }

    public static boolean isConfigured() {
        return BuildConfig.CLIST_USERNAME != null && !BuildConfig.CLIST_USERNAME.trim().isEmpty() &&
               BuildConfig.CLIST_API_KEY != null && !BuildConfig.CLIST_API_KEY.trim().isEmpty();
    }

    public static Long parseUtcSeconds(String text) {
        if (text == null) return null;
        try {
            String clean = text.endsWith("Z") ? text.substring(0, text.length() - 1) : text;
            return LocalDateTime.parse(clean).toEpochSecond(ZoneOffset.UTC);
        } catch (Exception e) {
            return null;
        }
    }

    public static String platformDomain(ClistContest contest) {
        if (contest == null || contest.getResource() == null) return null;
        JsonElement element = contest.getResource();
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        } else if (element.isJsonObject()) {
            if (element.getAsJsonObject().has("host")) {
                return element.getAsJsonObject().get("host").getAsString();
            } else if (element.getAsJsonObject().has("name")) {
                return element.getAsJsonObject().get("name").getAsString();
            }
        }
        return null;
    }

    public static CfContest toCfContest(ClistContest contest) {
        if (contest == null) return null;
        Long start = parseUtcSeconds(contest.getStart());
        if (start == null) return null;
        String domain = platformDomain(contest);
        if (domain == null) domain = "";
        String label = PLATFORMS.containsKey(domain) ? PLATFORMS.get(domain) : (domain.isEmpty() ? "Other" : domain);

        CfContest cf = new CfContest(
            ID_OFFSET + contest.getId(),
            contest.getEvent(),
            label,
            "BEFORE",
            contest.getDurationSeconds(),
            start
        );
        cf.setWebsiteUrl(contest.getHref());
        return cf;
    }

    public void fetchUpcoming(CodeforcesRepository.Callback<List<CfContest>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (!isConfigured()) {
                callback.onError(new Exception("clist.by key not configured"));
                return;
            }
            try {
                String auth = "ApiKey " + BuildConfig.CLIST_USERNAME + ":" + BuildConfig.CLIST_API_KEY;
                String resources = String.join(",", PLATFORMS.keySet());
                Response<ClistResponse> response = ApiClient.getClistApi()
                        .getUpcoming(auth, resources, true, "start", 100).execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<CfContest> list = new ArrayList<>();
                    for (ClistContest obj : response.body().getObjects()) {
                        CfContest c = toCfContest(obj);
                        if (c != null) list.add(c);
                    }
                    callback.onSuccess(list);
                } else {
                    callback.onError(new Exception("Could not load other platforms: HTTP " + response.code()));
                }
            } catch (Exception e) {
                callback.onError(new Exception("Could not load other platforms: " + e.getMessage(), e));
            }
        });
    }
}

