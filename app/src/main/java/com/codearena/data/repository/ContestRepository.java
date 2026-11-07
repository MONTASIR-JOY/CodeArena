package com.codearena.data.repository;

import android.content.Context;
import com.codearena.data.api.ApiClient;
import com.codearena.data.db.AppDatabase;
import com.codearena.data.model.CfContest;
import com.codearena.data.model.CfResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ContestRepository {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }

    private final Context context;
    private final UserPreferencesRepository prefsRepo;
    private final ClistRepository clistRepo;

    public ContestRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefsRepo = new UserPreferencesRepository(context);
        this.clistRepo = new ClistRepository();
    }

    public List<CfContest> getCachedContests() {
        String json = prefsRepo.getCachedUpcomingContestsJson();
        if (json != null && !json.trim().isEmpty()) {
            try {
                Type listType = new TypeToken<List<CfContest>>() {}.getType();
                List<CfContest> list = new Gson().fromJson(json, listType);
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Exception ignored) {}
        }
        return new ArrayList<>();
    }

    public void fetchUpcomingContests(Callback<List<CfContest>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CfContest> allContests = new ArrayList<>();
            long nowSec = System.currentTimeMillis() / 1000L;

            // 1. Codeforces
            try {
                retrofit2.Response<CfResponse<List<CfContest>>> cfResp =
                        ApiClient.getCodeforcesApi().getContestList(false).execute();
                if (cfResp.isSuccessful() && cfResp.body() != null && cfResp.body().isOk()) {
                    List<CfContest> cfList = cfResp.body().getResult();
                    if (cfList != null) {
                        for (CfContest c : cfList) {
                            if ("BEFORE".equalsIgnoreCase(c.getPhase()) ||
                                    (c.getStartTimeSeconds() != null && c.getStartTimeSeconds() > nowSec - 3600)) {
                                c.setType("Codeforces");
                                c.setWebsiteUrl("https://codeforces.com/contest/" + c.getId());
                                allContests.add(c);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            OkHttpClient client = ApiClient.getGeneralHttpClient();

            // 2. CodeChef
            try {
                Request ccReq = new Request.Builder()
                        .url("https://www.codechef.com/api/list/contests/all?sort_by=START&sorting_order=asc&offset=0&mode=all")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .build();
                Response ccResp = client.newCall(ccReq).execute();
                if (ccResp.isSuccessful() && ccResp.body() != null) {
                    String body = ccResp.body().string();
                    JsonObject root = new Gson().fromJson(body, JsonObject.class);
                    if (root != null && root.has("future_contests")) {
                        JsonArray future = root.getAsJsonArray("future_contests");
                        for (JsonElement el : future) {
                            JsonObject obj = el.getAsJsonObject();
                            String code = obj.has("contest_code") ? obj.get("contest_code").getAsString() : "";
                            String name = obj.has("contest_name") ? obj.get("contest_name").getAsString() : code;
                            String startIso = obj.has("contest_start_date_iso") ? obj.get("contest_start_date_iso").getAsString() : "";
                            long durationMinutes = 120;
                            if (obj.has("contest_duration")) {
                                try {
                                    durationMinutes = Long.parseLong(obj.get("contest_duration").getAsString());
                                } catch (Exception ignored) {}
                            }

                            Long startEpoch = null;
                            if (!startIso.isEmpty()) {
                                try {
                                    startEpoch = OffsetDateTime.parse(startIso).toEpochSecond();
                                } catch (Exception ex) {
                                    startEpoch = ClistRepository.parseUtcSeconds(startIso);
                                }
                            }

                            if (startEpoch != null && startEpoch > nowSec - 3600) {
                                long id = 20_000_000_000L + Math.abs(code.hashCode() % 1000000000);
                                CfContest cc = new CfContest(id, name, "CodeChef", "BEFORE", durationMinutes * 60, startEpoch);
                                cc.setWebsiteUrl("https://www.codechef.com/" + code);
                                allContests.add(cc);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 3. LeetCode
            try {
                Request lcReq = new Request.Builder()
                        .url("https://competeapi.vercel.app/contests/leetcode/")
                        .header("User-Agent", "Mozilla/5.0")
                        .build();
                Response lcResp = client.newCall(lcReq).execute();
                if (lcResp.isSuccessful() && lcResp.body() != null) {
                    String body = lcResp.body().string();
                    JsonObject root = new Gson().fromJson(body, JsonObject.class);
                    if (root != null && root.has("data") && root.getAsJsonObject("data").has("topTwoContests")) {
                        JsonArray contests = root.getAsJsonObject("data").getAsJsonArray("topTwoContests");
                        for (JsonElement el : contests) {
                            JsonObject obj = el.getAsJsonObject();
                            String title = obj.has("title") ? obj.get("title").getAsString() : "LeetCode Contest";
                            long startTime = obj.has("startTime") ? obj.get("startTime").getAsLong() : 0L;
                            long duration = obj.has("duration") ? obj.get("duration").getAsLong() : 5400L;

                            if (startTime > nowSec - 3600) {
                                long id = 30_000_000_000L + Math.abs(title.hashCode() % 1000000000);
                                CfContest lc = new CfContest(id, title, "LeetCode", "BEFORE", duration, startTime);
                                lc.setWebsiteUrl("https://leetcode.com/contest/");
                                allContests.add(lc);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 4. AtCoder
            try {
                Request acReq = new Request.Builder()
                        .url("https://kenkoooo.com/atcoder/resources/contests.json")
                        .header("User-Agent", "Mozilla/5.0")
                        .build();
                Response acResp = client.newCall(acReq).execute();
                if (acResp.isSuccessful() && acResp.body() != null) {
                    String body = acResp.body().string();
                    JsonArray array = new Gson().fromJson(body, JsonArray.class);
                    if (array != null) {
                        for (int i = Math.max(0, array.size() - 40); i < array.size(); i++) {
                            JsonObject obj = array.get(i).getAsJsonObject();
                            String cid = obj.has("id") ? obj.get("id").getAsString() : "";
                            String title = obj.has("title") ? obj.get("title").getAsString() : cid;
                            long startEpoch = obj.has("start_epoch_second") ? obj.get("start_epoch_second").getAsLong() : 0L;
                            long duration = obj.has("duration_second") ? obj.get("duration_second").getAsLong() : 6000L;

                            if (startEpoch > nowSec - 3600) {
                                long id = 40_000_000_000L + Math.abs(cid.hashCode() % 1000000000);
                                CfContest ac = new CfContest(id, title, "AtCoder", "BEFORE", duration, startEpoch);
                                ac.setWebsiteUrl("https://atcoder.jp/contests/" + cid);
                                allContests.add(ac);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 5. HackerRank
            try {
                Request hrReq = new Request.Builder()
                        .url("https://www.hackerrank.com/rest/contests/upcoming?offset=0&limit=10")
                        .header("User-Agent", "Mozilla/5.0")
                        .build();
                Response hrResp = client.newCall(hrReq).execute();
                if (hrResp.isSuccessful() && hrResp.body() != null) {
                    String body = hrResp.body().string();
                    JsonObject root = new Gson().fromJson(body, JsonObject.class);
                    if (root != null && root.has("models")) {
                        JsonArray models = root.getAsJsonArray("models");
                        for (JsonElement el : models) {
                            JsonObject obj = el.getAsJsonObject();
                            long id = obj.has("id") ? obj.get("id").getAsLong() : 0L;
                            String name = obj.has("name") ? obj.get("name").getAsString() : "HackerRank Contest";
                            String slug = obj.has("slug") ? obj.get("slug").getAsString() : "";
                            long startEpoch = obj.has("epoch_starttime") ? obj.get("epoch_starttime").getAsLong() : 0L;
                            long endEpoch = obj.has("epoch_endtime") ? obj.get("epoch_endtime").getAsLong() : startEpoch + 7200L;
                            long duration = Math.max(3600, endEpoch - startEpoch);

                            if (startEpoch > nowSec - 3600) {
                                CfContest hr = new CfContest(50_000_000_000L + id, name, "HackerRank", "BEFORE", duration, startEpoch);
                                hr.setWebsiteUrl("https://hackerrank.com/contests/" + slug);
                                allContests.add(hr);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 6. Supplement recurring weekly contests for complete calendar view
            supplementScheduledContests(allContests, nowSec);

            // Deduplicate by name & startTime
            List<CfContest> distinct = deduplicateContests(allContests);

            // Sort chronologically
            Collections.sort(distinct, (a, b) -> {
                Long tA = a.getStartTimeSeconds() != null ? a.getStartTimeSeconds() : 0L;
                Long tB = b.getStartTimeSeconds() != null ? b.getStartTimeSeconds() : 0L;
                return Long.compare(tA, tB);
            });

            // Save to local cache
            try {
                String json = new Gson().toJson(distinct);
                prefsRepo.setCachedUpcomingContestsJson(json);
            } catch (Exception ignored) {}

            callback.onSuccess(distinct);
        });
    }

    private void supplementScheduledContests(List<CfContest> list, long nowSec) {
        Set<String> existingKeys = new HashSet<>();
        for (CfContest c : list) {
            if (c.getStartTimeSeconds() != null && c.getType() != null) {
                LocalDate date = Instant.ofEpochSecond(c.getStartTimeSeconds())
                        .atZone(ZoneOffset.UTC).toLocalDate();
                existingKeys.add(c.getType().toLowerCase() + "_" + date.toString());
            }
        }

        LocalDate today = Instant.ofEpochSecond(nowSec).atZone(ZoneOffset.UTC).toLocalDate();
        for (int i = 0; i <= 35; i++) {
            LocalDate day = today.plusDays(i);
            DayOfWeek dow = day.getDayOfWeek();

            // LeetCode Weekly Contest: Every Sunday at 02:30 UTC
            if (dow == DayOfWeek.SUNDAY) {
                String key = "leetcode_" + day.toString();
                if (!existingKeys.contains(key)) {
                    long epoch = day.atTime(2, 30).toEpochSecond(ZoneOffset.UTC);
                    if (epoch > nowSec) {
                        CfContest lc = new CfContest(
                                31_000_000_000L + day.toEpochDay(),
                                "Weekly Contest",
                                "LeetCode",
                                "BEFORE",
                                5400,
                                epoch
                        );
                        lc.setWebsiteUrl("https://leetcode.com/contest/");
                        list.add(lc);
                        existingKeys.add(key);
                    }
                }
            }

            // AtCoder Beginner Contest: Every Saturday at 12:00 UTC (21:00 JST)
            if (dow == DayOfWeek.SATURDAY) {
                String key = "atcoder_" + day.toString();
                if (!existingKeys.contains(key)) {
                    long epoch = day.atTime(12, 0).toEpochSecond(ZoneOffset.UTC);
                    if (epoch > nowSec) {
                        CfContest ac = new CfContest(
                                41_000_000_000L + day.toEpochDay(),
                                "AtCoder Beginner Contest",
                                "AtCoder",
                                "BEFORE",
                                6000,
                                epoch
                        );
                        ac.setWebsiteUrl("https://atcoder.jp/contests/");
                        list.add(ac);
                        existingKeys.add(key);
                    }
                }

                // LeetCode Biweekly Contest: Every alternate Saturday at 14:30 UTC
                if ((day.toEpochDay() / 7) % 2 == 0) {
                    String lcKey = "leetcode_" + day.toString();
                    if (!existingKeys.contains(lcKey)) {
                        long epoch = day.atTime(14, 30).toEpochSecond(ZoneOffset.UTC);
                        if (epoch > nowSec) {
                            CfContest lcB = new CfContest(
                                    32_000_000_000L + day.toEpochDay(),
                                    "Biweekly Contest",
                                    "LeetCode",
                                    "BEFORE",
                                    5400,
                                    epoch
                            );
                            lcB.setWebsiteUrl("https://leetcode.com/contest/");
                            list.add(lcB);
                            existingKeys.add(lcKey);
                        }
                    }
                }
            }

            // CodeChef Starters: Every Wednesday at 14:30 UTC (20:00 IST)
            if (dow == DayOfWeek.WEDNESDAY) {
                String key = "codechef_" + day.toString();
                if (!existingKeys.contains(key)) {
                    long epoch = day.atTime(14, 30).toEpochSecond(ZoneOffset.UTC);
                    if (epoch > nowSec) {
                        CfContest cc = new CfContest(
                                21_000_000_000L + day.toEpochDay(),
                                "CodeChef Starters",
                                "CodeChef",
                                "BEFORE",
                                7200,
                                epoch
                        );
                        cc.setWebsiteUrl("https://www.codechef.com/");
                        list.add(cc);
                        existingKeys.add(key);
                    }
                }
            }
        }
    }

    private List<CfContest> deduplicateContests(List<CfContest> raw) {
        List<CfContest> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (CfContest c : raw) {
            String key = (c.getType() != null ? c.getType() : "") + "_" +
                         (c.getName() != null ? c.getName().trim().toLowerCase() : "") + "_" +
                         (c.getStartTimeSeconds() != null ? (c.getStartTimeSeconds() / 3600) : 0);
            if (!seen.contains(key)) {
                seen.add(key);
                result.add(c);
            }
        }
        return result;
    }
}

