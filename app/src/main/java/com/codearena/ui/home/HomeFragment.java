package com.codearena.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.codearena.R;
import com.codearena.data.db.AppDatabase;
import com.codearena.data.db.entity.UpsolveItemEntity;
import com.codearena.data.model.CfContest;
import com.codearena.data.model.CfRatingChange;
import com.codearena.data.model.CfSubmission;
import com.codearena.data.model.CfUser;
import com.codearena.data.model.CfProblem;
import com.codearena.data.repository.CodeforcesRepository;
import com.codearena.data.repository.ContestRepository;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentHomeBinding;
import com.codearena.util.RankColors;
import com.codearena.util.TimeUtils;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private UserPreferencesRepository prefsRepo;
    private CodeforcesRepository cfRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefsRepo = new UserPreferencesRepository(requireContext());
        cfRepo = new CodeforcesRepository(requireContext());

        setupNavigationListeners();
        setupSwipeRefresh();
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefsRepo != null && binding != null) {
            renderLinkedHandles();
            loadUpsolveStats();
        }
    }

    private void setupNavigationListeners() {
        binding.cardProfile.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_settings));

        binding.cardActionContests.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_contests));

        binding.cardActionUpcoming.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_upcoming));

        binding.cardActionMastery.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.masteryFragment));

        binding.cardActionCalendar.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_calendar));

        binding.cardActionDaily.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.dailyChallengeFragment));

        binding.cardActionBlogs.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.blogsFragment));

        binding.cardActionCatalog.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.catalogFragment));

        binding.btnOpenUpsolve.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_upsolve));
    }

    private void setupSwipeRefresh() {
        binding.srlHome.setOnRefreshListener(this::loadData);
    }

    private void loadData() {
        binding.srlHome.setRefreshing(true);
        String handle = prefsRepo.getHandle();

        if (handle == null || handle.trim().isEmpty()) {
            binding.tvGreeting.setText("Welcome to CodeArena!");
            binding.tvHandle.setText("Set your handle");
            binding.tvRank.setText("Tap here or go to Settings");
            binding.tvCurrentRating.setText("---");
            binding.tvCurrentRating.setTextColor(Color.parseColor("#9E9E9E"));
            binding.tvMaxRating.setText("---");
            binding.tvMaxRating.setTextColor(Color.parseColor("#9E9E9E"));
            binding.tvContribution.setText("0");
            binding.tvContribution.setTextColor(Color.parseColor("#9E9E9E"));
            binding.tvCfSolvedCount.setText("---");
            binding.tvCfSolvedCount.setTextColor(Color.parseColor("#9E9E9E"));
            binding.tvRatingDelta.setText("");
            binding.tvAvatarInitial.setText("?");
            binding.chartRating.setVisibility(View.GONE);
            binding.tvChartEmpty.setVisibility(View.VISIBLE);
            binding.srlHome.setRefreshing(false);
            loadUpcomingContests();
            loadUpsolveStats();
            renderLinkedHandles();
            return;
        }

        binding.tvGreeting.setText("Welcome back,");
        binding.tvHandle.setText(handle);
        binding.tvAvatarInitial.setText(handle.substring(0, 1).toUpperCase());

        int cachedSolved = prefsRepo.getCfSolvedCount();
        binding.tvCfSolvedCount.setText(cachedSolved > 0 ? String.valueOf(cachedSolved) : "---");

        // reorder: render linked handles before loading solve counts so counts overwrite handles later
        renderLinkedHandles();
        loadUpsolveStats();

        cfRepo.validateAndFetchUser(handle, new CodeforcesRepository.Callback<CfUser>() {
            @Override
            public void onSuccess(CfUser user) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    int rankColor = RankColors.getColorForRating(user.getRating());
                    int maxRankColor = RankColors.getColorForRating(user.getMaxRating());

                    binding.tvHandle.setTextColor(rankColor);
                    binding.tvRank.setText(RankColors.getTitleForRating(user.getRating()));
                    binding.tvRank.setTextColor(rankColor);

                    if (user.getRating() != null && user.getRating() > 0) {
                        binding.tvCurrentRating.setText(String.valueOf(user.getRating()));
                        binding.tvCurrentRating.setTextColor(rankColor);
                        prefsRepo.saveUserRating(user.getRating());
                    } else {
                        binding.tvCurrentRating.setText("---");
                        binding.tvCurrentRating.setTextColor(Color.parseColor("#9E9E9E"));
                    }

                    if (user.getMaxRating() != null && user.getMaxRating() > 0) {
                        binding.tvMaxRating.setText(String.valueOf(user.getMaxRating()));
                        binding.tvMaxRating.setTextColor(maxRankColor);
                    } else {
                        binding.tvMaxRating.setText("---");
                        binding.tvMaxRating.setTextColor(Color.parseColor("#9E9E9E"));
                    }

                    int contrib = user.getContribution();
                    binding.tvContribution.setText((contrib >= 0 ? "+" : "") + contrib);
                    if (contrib > 0) {
                        binding.tvContribution.setTextColor(Color.parseColor("#10B981"));
                    } else if (contrib < 0) {
                        binding.tvContribution.setTextColor(Color.parseColor("#EF4444"));
                    } else {
                        binding.tvContribution.setTextColor(Color.parseColor("#94A3B8"));
                    }

                    binding.tvAvatarInitial.setTextColor(rankColor);
                    binding.cardAvatar.setStrokeColor(rankColor);
                });
            }

            @Override
            public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        binding.tvErrorMessage.setText("Could not update profile: " + error.getMessage()));
            }
        });

        cfRepo.fetchUserRatingHistory(handle, new CodeforcesRepository.Callback<List<CfRatingChange>>() {
            @Override
            public void onSuccess(List<CfRatingChange> history) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> renderRatingChart(history));
            }

            @Override
            public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    binding.chartRating.setVisibility(View.GONE);
                    binding.tvChartEmpty.setVisibility(View.VISIBLE);
                });
            }
        });

        loadUpcomingContests();
    }

    private void updatePlatformPillDisplay(String handle, int solvedCount, TextView textView) {
        if (handle == null || handle.trim().isEmpty()) {
            textView.setText("+ Add");
        } else {
            textView.setText(solvedCount + " Solved");
        }
    }

    private void renderLinkedHandles() {
        String lc = prefsRepo.getLeetCodeHandle();
        String cc = prefsRepo.getCodeChefHandle();
        String ac = prefsRepo.getAtCoderHandle();
        String hr = prefsRepo.getHackerRankHandle();

        updatePlatformPillDisplay(lc, prefsRepo.getLeetCodeSolvedCount(), binding.tvHomeLeetCode);
        updatePlatformPillDisplay(cc, prefsRepo.getCodeChefSolvedCount(), binding.tvHomeCodeChef);
        updatePlatformPillDisplay(ac, prefsRepo.getAtCoderSolvedCount(), binding.tvHomeAtCoder);
        updatePlatformPillDisplay(hr, prefsRepo.getHackerRankSolvedCount(), binding.tvHomeHackerRank);

        binding.pillLeetCode.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_settings));
        binding.pillCodeChef.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_settings));
        binding.pillAtCoder.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_settings));
        binding.pillHackerRank.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_settings));
    }

    private void renderRatingChart(List<CfRatingChange> history) {
        if (history == null || history.isEmpty()) {
            binding.chartRating.setVisibility(View.GONE);
            binding.tvChartEmpty.setVisibility(View.VISIBLE);
            return;
        }

        binding.tvChartEmpty.setVisibility(View.GONE);
        binding.chartRating.setVisibility(View.VISIBLE);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            entries.add(new Entry(i, history.get(i).getNewRating()));
        }

        if (!history.isEmpty()) {
            CfRatingChange last = history.get(history.size() - 1);
            int delta = last.getNewRating() - last.getOldRating();
            binding.tvRatingDelta.setText((delta >= 0 ? "+" : "") + delta + " last round");
            binding.tvRatingDelta.setTextColor(delta >= 0 ? Color.parseColor("#10B981") : Color.parseColor("#EF4444"));
            binding.tvRatingDelta.setVisibility(View.VISIBLE);
        } else {
            binding.tvRatingDelta.setVisibility(View.GONE);
        }

        LineDataSet dataSet = new LineDataSet(entries, "Rating");
        dataSet.setColor(Color.parseColor("#4A90E2"));
        dataSet.setCircleColor(Color.parseColor("#4A90E2"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(9f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        binding.chartRating.setData(lineData);
        binding.chartRating.getDescription().setEnabled(false);
        binding.chartRating.getLegend().setEnabled(false);
        binding.chartRating.getAxisRight().setEnabled(false);
        binding.chartRating.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.chartRating.getXAxis().setTextColor(Color.parseColor("#A0A0A0"));
        binding.chartRating.getAxisLeft().setTextColor(Color.parseColor("#A0A0A0"));
        binding.chartRating.invalidate();
    }

    private void loadUpcomingContests() {
        ContestRepository contestRepo = new ContestRepository(requireContext());

        // Instant display from cache if available
        List<CfContest> cached = contestRepo.getCachedContests();
        if (!cached.isEmpty()) {
            displayNextContest(findNextEarliestCodeforcesContest(cached));
        }

        contestRepo.fetchUpcomingContests(new ContestRepository.Callback<List<CfContest>>() {
            @Override
            public void onSuccess(List<CfContest> contests) {
                if (!isAdded()) return;
                CfContest earliest = findNextEarliestCodeforcesContest(contests);
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null) return;
                    binding.srlHome.setRefreshing(false);
                    displayNextContest(earliest);
                });
            }

            @Override
            public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null) return;
                    binding.srlHome.setRefreshing(false);
                });
            }
        });
    }

    private CfContest findNextEarliestCodeforcesContest(List<CfContest> contests) {
        if (contests == null || contests.isEmpty()) return null;
        long nowSec = System.currentTimeMillis() / 1000L;
        CfContest earliest = null;
        for (CfContest c : contests) {
            String type = c.getType() != null ? c.getType().toLowerCase() : "";
            String name = c.getName() != null ? c.getName().toLowerCase() : "";
            boolean isCodeforces = type.contains("codeforces") || type.contains("cf") ||
                    name.contains("codeforces") || name.contains("round");
            if (isCodeforces && c.getStartTimeSeconds() != null && c.getStartTimeSeconds() > nowSec) {
                if (earliest == null || c.getStartTimeSeconds() < earliest.getStartTimeSeconds()) {
                    earliest = c;
                }
            }
        }
        return earliest;
    }

    private void displayNextContest(CfContest target) {
        if (!isAdded() || binding == null) return;
        if (target != null) {
            binding.cardNextContest.setVisibility(View.VISIBLE);
            binding.tvNextContestName.setText(target.getName());
            if (target.getStartTimeSeconds() != null) {
                binding.tvNextContestTime.setText(TimeUtils.formatEpochSeconds(target.getStartTimeSeconds()));
                binding.tvNextContestCountdown.setText(TimeUtils.formatRelativeCountdown(target.getStartTimeSeconds()));
            }
            binding.cardNextContest.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.nav_upcoming));
        } else {
            binding.tvNextContestName.setText("No official rounds scheduled yet");
            binding.tvNextContestTime.setText("Check upcoming tab for multi-platform contests");
            binding.tvNextContestCountdown.setText("--");
        }
    }

    private void loadUpsolveStats() {
        if (!isAdded()) return;
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDayMillis = cal.getTimeInMillis();

        // 1. Observe active / due upsolves in queue
        db.upsolveDao().getAllActiveUpsolvesLiveData().observe(getViewLifecycleOwner(), list -> {
            int dueCount = list != null ? list.size() : 0;
            binding.tvDueUpsolveCount.setText(dueCount + " problems due in queue");
        });

        // 2. Observe solved today count to track daily target fulfillment
        db.upsolveDao().getSolvedTodayCountLiveData(startOfDayMillis).observe(getViewLifecycleOwner(), solvedCount -> {
            int solved = solvedCount != null ? solvedCount : 0;
            int target = Math.max(1, prefsRepo.getDailyTarget());
            int progress = Math.min(100, (solved * 100) / target);
            binding.progressUpsolve.setProgress(progress);
            if (solved >= target) {
                binding.tvUpsolveProgressRatio.setText(solved + "/" + target + " done (Goal Met! ✓)");
            } else {
                binding.tvUpsolveProgressRatio.setText(solved + "/" + target + " solved today");
            }
        });


        // Fetch real solve counts for platforms
        String lcHandle = prefsRepo.getLeetCodeHandle();
        if (!lcHandle.isEmpty()) {
            fetchLeetCodeSolvedCount(lcHandle);
        }
        String ccHandle = prefsRepo.getCodeChefHandle();
        if (!ccHandle.isEmpty()) {
            fetchCodeChefSolvedCount(ccHandle);
        }
        String acHandle = prefsRepo.getAtCoderHandle();
        if (!acHandle.isEmpty()) {
            fetchAtCoderSolvedCount(acHandle);
        }
        String hrHandle = prefsRepo.getHackerRankHandle();
        if (!hrHandle.isEmpty()) {
            fetchHackerRankSolvedCount(hrHandle);
        }

        int lcCount = prefsRepo.getLeetCodeSolvedCount();
        int ccCount = prefsRepo.getCodeChefSolvedCount();
        int acCount = prefsRepo.getAtCoderSolvedCount();
        int hrCount = prefsRepo.getHackerRankSolvedCount();
        requireActivity().runOnUiThread(() -> {
            if (isAdded()) {
                updatePlatformPillDisplay(lcHandle, lcCount, binding.tvHomeLeetCode);
                updatePlatformPillDisplay(ccHandle, ccCount, binding.tvHomeCodeChef);
                updatePlatformPillDisplay(acHandle, acCount, binding.tvHomeAtCoder);
                updatePlatformPillDisplay(hrHandle, hrCount, binding.tvHomeHackerRank);
            }
        });

        String handle = prefsRepo.getHandle();
        if (!handle.isEmpty()) {
            cfRepo.fetchUserSubmissions(handle, 10000, new CodeforcesRepository.Callback<List<CfSubmission>>() {
                @Override
                public void onSuccess(List<CfSubmission> submissions) {
                    if (submissions == null || !isAdded()) return;

                    // Compute overall actual solved count for Codeforces (all-time unique problems)
                    Set<String> solvedKeys = new HashSet<>();
                    Set<String> canonicalKeys = new HashSet<>();
                    for (CfSubmission sub : submissions) {
                        if ("OK".equals(sub.getVerdict()) && sub.getProblem() != null) {
                            CfProblem p = sub.getProblem();
                            if (sub.getContestId() != null && p.getIndex() != null) {
                                String idx = p.getIndex().trim().toUpperCase();
                                solvedKeys.add(sub.getContestId() + "_" + idx);
                                canonicalKeys.add(sub.getContestId() + idx);
                                canonicalKeys.add(sub.getContestId() + "_" + idx);
                            } else if (p.getName() != null) {
                                solvedKeys.add(p.getName().trim().toLowerCase());
                            }
                        }
                    }
                    if (!canonicalKeys.isEmpty()) {
                        prefsRepo.saveUserSolvedProblemKeys(canonicalKeys);
                    }
                    if (!solvedKeys.isEmpty()) {
                        int count = solvedKeys.size();
                        prefsRepo.saveCfSolvedCount(count);
                        requireActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                binding.tvCfSolvedCount.setText(String.valueOf(count));
                            }
                        });
                    }

                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        for (CfSubmission sub : submissions) {
                            if ("OK".equals(sub.getVerdict()) && (sub.getCreationTimeSeconds() * 1000L) >= startOfDayMillis) {
                                if (sub.getContestId() != null && sub.getProblem() != null && sub.getProblem().getIndex() != null) {
                                    long contestId = sub.getContestId();
                                    String index = sub.getProblem().getIndex();
                                    long solvedTime = sub.getCreationTimeSeconds() * 1000L;

                                    UpsolveItemEntity existing = db.upsolveDao().getUpsolveItem(contestId, index);
                                    if (existing == null) {
                                        UpsolveItemEntity newItem = new UpsolveItemEntity(
                                                contestId,
                                                index,
                                                sub.getProblem().getName(),
                                                sub.getProblem().getRating(),
                                                sub.getProblem().getTags(),
                                                solvedTime,
                                                0,
                                                solvedTime,
                                                1,
                                                true
                                        );
                                        db.upsolveDao().insertUpsolveItem(newItem);
                                    } else if (!existing.isSolved()) {
                                        db.upsolveDao().markAsSolved(contestId, index, solvedTime);
                                    }
                                }
                            }
                        }
                    });
                }

                @Override
                public void onError(Exception error) {}
            });
        }
    }

    private void fetchLeetCodeSolvedCount(String handle) {
        new Thread(() -> {
            try {
                URL url = new URL("https://leetcode.com/graphql");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("query", "query getUserProfile($username: String!) { matchedUser(username: $username) { submitStatsGlobal { acSubmissionNum { difficulty count } } } }");
                JSONObject variables = new JSONObject();
                variables.put("username", handle);
                payload.put("variables", variables);

                byte[] out = payload.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(out);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    in.close();

                    JSONObject res = new JSONObject(sb.toString());
                    JSONObject data = res.optJSONObject("data");
                    if (data != null) {
                        JSONObject matchedUser = data.optJSONObject("matchedUser");
                        if (matchedUser != null) {
                            JSONObject submitStats = matchedUser.optJSONObject("submitStatsGlobal");
                            if (submitStats != null) {
                                JSONArray acList = submitStats.optJSONArray("acSubmissionNum");
                                if (acList != null) {
                                    int totalSolved = 0;
                                    for (int i = 0; i < acList.length(); i++) {
                                        JSONObject item = acList.getJSONObject(i);
                                        if ("All".equalsIgnoreCase(item.optString("difficulty"))) {
                                            totalSolved = item.optInt("count", 0);
                                            break;
                                        }
                                    }
                                    final int solved = totalSolved;
                                    prefsRepo.saveLeetCodeSolvedCount(solved);
                                    requireActivity().runOnUiThread(() -> {
                                        if (isAdded()) {
                                            updatePlatformPillDisplay(handle, solved, binding.tvHomeLeetCode);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchCodeChefSolvedCount(String handle) {
        new Thread(() -> {
            try {
                URL url = new URL("https://www.codechef.com/users/" + handle);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    in.close();

                    String html = sb.toString();
                    Pattern p = Pattern.compile("(?:Total Problems Solved|Problems Solved)[^0-9]{0,50}(\\d+)", Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(html);
                    if (m.find()) {
                        int solved = Integer.parseInt(m.group(1));
                        prefsRepo.saveCodeChefSolvedCount(solved);
                        requireActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                updatePlatformPillDisplay(handle, solved, binding.tvHomeCodeChef);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchAtCoderSolvedCount(String handle) {
        new Thread(() -> {
            try {
                URL url = new URL("https://kenkoooo.com/atcoder/atcoder-api/v3/user/ac_rank?user=" + handle);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    in.close();

                    JSONObject obj = new JSONObject(sb.toString());
                    int solved = obj.optInt("count", 0);
                    prefsRepo.saveAtCoderSolvedCount(solved);
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            updatePlatformPillDisplay(handle, solved, binding.tvHomeAtCoder);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchHackerRankSolvedCount(String handle) {
        new Thread(() -> {
            try {
                URL url = new URL("https://www.hackerrank.com/rest/hackers/" + handle + "/badges");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    in.close();

                    JSONObject obj = new JSONObject(sb.toString());
                    JSONArray models = obj.optJSONArray("models");
                    int totalSolved = 0;
                    if (models != null) {
                        for (int i = 0; i < models.length(); i++) {
                            JSONObject badge = models.optJSONObject(i);
                            if (badge != null) {
                                totalSolved += badge.optInt("solved", 0);
                            }
                        }
                    }

                    if (totalSolved == 0) {
                        try {
                            URL histUrl = new URL("https://www.hackerrank.com/rest/hackers/" + handle + "/submission_histories");
                            HttpURLConnection histConn = (HttpURLConnection) histUrl.openConnection();
                            histConn.setRequestMethod("GET");
                            histConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                            histConn.setConnectTimeout(8000);
                            histConn.setReadTimeout(8000);
                            if (histConn.getResponseCode() == 200) {
                                BufferedReader hIn = new BufferedReader(new InputStreamReader(histConn.getInputStream(), StandardCharsets.UTF_8));
                                StringBuilder hSb = new StringBuilder();
                                String hLine;
                                while ((hLine = hIn.readLine()) != null) {
                                    hSb.append(hLine);
                                }
                                hIn.close();
                                JSONObject hObj = new JSONObject(hSb.toString());
                                java.util.Iterator<String> keys = hObj.keys();
                                while (keys.hasNext()) {
                                    totalSolved += hObj.optInt(keys.next(), 0);
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    final int solved = totalSolved;
                    prefsRepo.saveHackerRankSolvedCount(solved);
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            updatePlatformPillDisplay(handle, solved, binding.tvHomeHackerRank);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

