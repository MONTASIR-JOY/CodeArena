package com.codearena.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.codearena.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.codearena.data.db.AppDatabase;
import com.codearena.data.db.entity.UpsolveItemEntity;
import com.codearena.data.model.CfContest;
import com.codearena.data.model.CfRatingChange;
import com.codearena.data.model.CfSubmission;
import com.codearena.data.model.CfUser;
import com.codearena.data.model.CfProblem;
import com.codearena.data.model.CatalogItem;
import com.codearena.data.repository.CatalogRepository;
import com.codearena.data.repository.CodeforcesRepository;
import com.codearena.data.repository.ContestRepository;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentHomeBinding;
import com.codearena.ui.widget.TrajectoryMarkerView;
import com.codearena.util.RankColors;
import com.codearena.util.TimeUtils;
import androidx.core.content.ContextCompat;
import android.graphics.drawable.Drawable;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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
        binding.cardProfile.setOnClickListener(v -> {
            if (prefsRepo.getHandle().isEmpty()) {
                showHandleDialog("Codeforces", "e.g. tourist, ecnerwala", prefsRepo.getHandle(), newHandle -> {
                    prefsRepo.saveHandle(newHandle);
                    loadData();
                });
            } else {
                Navigation.findNavController(v).navigate(R.id.nav_settings);
            }
        });

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

        binding.cardActionCses.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.csesFragment));

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

        // Instant display of cached rating history if available
        String cachedHistoryJson = prefsRepo.getCachedRatingHistoryJson();
        if (cachedHistoryJson != null && !cachedHistoryJson.trim().isEmpty()) {
            try {
                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<CfRatingChange>>() {}.getType();
                List<CfRatingChange> cachedHistory = new com.google.gson.Gson().fromJson(cachedHistoryJson, listType);
                if (cachedHistory != null && !cachedHistory.isEmpty()) {
                    renderRatingChart(cachedHistory);
                }
            } catch (Exception ignored) {}
        }

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
                if (history != null && !history.isEmpty()) {
                    try {
                        String json = new com.google.gson.Gson().toJson(history);
                        prefsRepo.setCachedRatingHistoryJson(json);
                    } catch (Exception ignored) {}
                }
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
        String cses = prefsRepo.getCsesHandle();
        String cc = prefsRepo.getCodeChefHandle();
        String ac = prefsRepo.getAtCoderHandle();
        String hr = prefsRepo.getHackerRankHandle();

        updatePlatformPillDisplay(lc, prefsRepo.getLeetCodeSolvedCount(), binding.tvHomeLeetCode);
        updatePlatformPillDisplay(cses, prefsRepo.getCsesSolvedCount(), binding.tvHomeCses);
        updatePlatformPillDisplay(cc, prefsRepo.getCodeChefSolvedCount(), binding.tvHomeCodeChef);
        updatePlatformPillDisplay(ac, prefsRepo.getAtCoderSolvedCount(), binding.tvHomeAtCoder);
        updatePlatformPillDisplay(hr, prefsRepo.getHackerRankSolvedCount(), binding.tvHomeHackerRank);

        binding.pillLeetCode.setOnClickListener(v ->
                showHandleDialog("LeetCode", "e.g. leetcode_user", prefsRepo.getLeetCodeHandle(), newHandle -> {
                    prefsRepo.saveLeetCodeHandle(newHandle);
                    loadData();
                }));
        binding.pillCses.setOnClickListener(v ->
                showHandleDialog("CSES", "e.g. 12345 or cses_user", prefsRepo.getCsesHandle(), newHandle -> {
                    prefsRepo.saveCsesHandle(newHandle);
                    loadData();
                }));
        binding.pillCodeChef.setOnClickListener(v ->
                showHandleDialog("CodeChef", "e.g. codechef_handle", prefsRepo.getCodeChefHandle(), newHandle -> {
                    prefsRepo.saveCodeChefHandle(newHandle);
                    loadData();
                }));
        binding.pillAtCoder.setOnClickListener(v ->
                showHandleDialog("AtCoder", "e.g. chokudai", prefsRepo.getAtCoderHandle(), newHandle -> {
                    prefsRepo.saveAtCoderHandle(newHandle);
                    loadData();
                }));
        binding.pillHackerRank.setOnClickListener(v ->
                showHandleDialog("HackerRank", "e.g. hackerrank_id", prefsRepo.getHackerRankHandle(), newHandle -> {
                    prefsRepo.saveHackerRankHandle(newHandle);
                    loadData();
                }));
    }

    private interface OnHandleSavedListener {
        void onSaved(String newHandle);
    }

    private void showHandleDialog(String platform, String hint, String currentHandle, OnHandleSavedListener listener) {
        if (!isAdded()) return;
        EditText input = new EditText(requireContext());
        input.setHint(hint);
        input.setText(currentHandle != null ? currentHandle : "");
        input.setSingleLine(true);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(platform + " Handle")
                .setMessage("Enter your official " + platform + " handle / username:")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newHandle = input.getText().toString().trim();
                    listener.onSaved(newHandle);
                    Toast.makeText(requireContext(), platform + " handle saved & synced!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void renderRatingChart(List<CfRatingChange> history) {
        if (history == null || history.isEmpty()) {
            binding.chartRating.setVisibility(View.GONE);
            binding.tvChartEmpty.setVisibility(View.VISIBLE);
            return;
        }

        binding.tvChartEmpty.setVisibility(View.GONE);
        binding.chartRating.setVisibility(View.VISIBLE);

        // Compute min/max rating from full history
        int minRating = Integer.MAX_VALUE;
        int maxRating = Integer.MIN_VALUE;
        for (int i = 0; i < history.size(); i++) {
            int rating = history.get(i).getNewRating();
            if (rating < minRating) minRating = rating;
            if (rating > maxRating) maxRating = rating;
        }

        // Build chart entries – sample at most MAX_POINTS for performance
        final int MAX_POINTS = 100;
        List<Entry> entries = new ArrayList<>();
        int step = Math.max(1, history.size() / MAX_POINTS);
        for (int i = 0; i < history.size(); i += step) {
            int rating = history.get(i).getNewRating();
            entries.add(new Entry(i, rating));
        }
        // Ensure last point is always included
        int lastIdx = history.size() - 1;
        if (entries.isEmpty() || (int) entries.get(entries.size() - 1).getX() != lastIdx) {
            entries.add(new Entry(lastIdx, history.get(lastIdx).getNewRating()));
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

        int primaryColor = ContextCompat.getColor(requireContext(), R.color.primary);

        LineDataSet dataSet = new LineDataSet(entries, "Rating");
        dataSet.setColor(primaryColor);
        dataSet.setLineWidth(2.5f);

        // Smooth modern sparkline style matching the app's cyan/primary theme:
        // Hide cluttered individual dots along the line; only highlight on tap/drag
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.18f);

        // Highlight indicator on touch
        dataSet.setHighlightEnabled(true);
        dataSet.setHighLightColor(primaryColor);
        dataSet.setHighlightLineWidth(1.5f);
        dataSet.setDrawHorizontalHighlightIndicator(false);

        // Gradient Area Fill under the curve
        dataSet.setDrawFilled(true);
        Drawable fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chart_gradient);
        if (fillDrawable != null) {
            dataSet.setFillDrawable(fillDrawable);
        } else {
            dataSet.setFillColor(primaryColor);
            dataSet.setFillAlpha(35);
        }

        LineData lineData = new LineData(dataSet);
        binding.chartRating.setData(lineData);

        // Disable SwipeRefresh during chart interaction to prevent loading spinner
        binding.chartRating.setOnChartGestureListener(new com.github.mikephil.charting.listener.OnChartGestureListener() {
            @Override public void onChartGestureStart(MotionEvent me, com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture lastPerformedGesture) {
                binding.srlHome.setEnabled(false);
            }
            @Override public void onChartGestureEnd(MotionEvent me, com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture lastPerformedGesture) {
                binding.srlHome.setEnabled(true);
            }
            @Override public void onChartLongPressed(MotionEvent me) {}
            @Override public void onChartDoubleTapped(MotionEvent me) {}
            @Override public void onChartSingleTapped(MotionEvent me) {}
            @Override public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
            @Override public void onChartScale(MotionEvent me, float scaleX, float scaleY) {}
            @Override public void onChartTranslate(MotionEvent me, float dX, float dY) {}
        });
        binding.chartRating.getDescription().setEnabled(false);
        binding.chartRating.getLegend().setEnabled(false);
        binding.chartRating.setTouchEnabled(true);
        binding.chartRating.setDragEnabled(true);
        // Allow independent X and Y scaling (vertical + horizontal zoom)
        binding.chartRating.setScaleXEnabled(true);
        binding.chartRating.setScaleYEnabled(true);
        // setPinchZoom(false) means X and Y scale independently; true locks them together
        binding.chartRating.setPinchZoom(false);
        binding.chartRating.setDoubleTapToZoomEnabled(true);
        binding.chartRating.setDrawGridBackground(false);
        binding.chartRating.resetViewPortOffsets();
        // Snug offsets so numbers are fully visible without wasting extra chart area
        binding.chartRating.setExtraOffsets(2f, 8f, 10f, 4f);

        // Interactive Tooltip / MarkerView
        TrajectoryMarkerView marker = new TrajectoryMarkerView(requireContext(), history);
        marker.setChartView(binding.chartRating);
        binding.chartRating.setMarker(marker);

        int mutedColor = Color.parseColor("#64748B");
        int gridColor = Color.parseColor("#1A334E68");

        // X-Axis
        XAxis xAxis = binding.chartRating.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(mutedColor);
        xAxis.setTextSize(9.5f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(gridColor);
        xAxis.setGranularity(1f);
        xAxis.setYOffset(2f);

        // Left Y-Axis
        YAxis leftAxis = binding.chartRating.getAxisLeft();
        leftAxis.setTextColor(mutedColor);
        leftAxis.setTextSize(9.5f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(gridColor);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setXOffset(6f);
        int padding = Math.max(30, (maxRating - minRating) / 8);
        leftAxis.setAxisMinimum(Math.max(0, minRating - padding));
        leftAxis.setAxisMaximum(maxRating + padding);

        // Right Y-Axis
        binding.chartRating.getAxisRight().setEnabled(false);

        // Smooth entry animation
        binding.chartRating.animateX(700);
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
        String csesHandle = prefsRepo.getCsesHandle();
        if (!csesHandle.isEmpty()) {
            fetchCsesSolvedCount(csesHandle);
        }

        int lcCount = prefsRepo.getLeetCodeSolvedCount();
        int ccCount = prefsRepo.getCodeChefSolvedCount();
        int acCount = prefsRepo.getAtCoderSolvedCount();
        int hrCount = prefsRepo.getHackerRankSolvedCount();
        int csesCount = prefsRepo.getCsesSolvedCount();
        requireActivity().runOnUiThread(() -> {
            if (isAdded()) {
                updatePlatformPillDisplay(lcHandle, lcCount, binding.tvHomeLeetCode);
                updatePlatformPillDisplay(ccHandle, ccCount, binding.tvHomeCodeChef);
                updatePlatformPillDisplay(acHandle, acCount, binding.tvHomeAtCoder);
                updatePlatformPillDisplay(hrHandle, hrCount, binding.tvHomeHackerRank);
                updatePlatformPillDisplay(csesHandle, csesCount, binding.tvHomeCses);
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

    private void fetchCsesSolvedCount(String handle) {
        new Thread(() -> {
            CatalogRepository catalogRepo = new CatalogRepository(requireContext());
            List<CatalogItem> csesItems = catalogRepo.getCsesCatalogItems();
            int inAppSolved = 0;
            if (csesItems != null) {
                for (CatalogItem item : csesItems) {
                    if (item.isRead()) {
                        inAppSolved++;
                    }
                }
            }

            int remoteCount = 0;
            // Only query remote if user entered a purely numeric user ID (e.g. "12345"),
            // not an alphanumeric username like "MontasirJoy03" where stripping letters leaves "03" (user #3)
            String trimmed = handle != null ? handle.trim() : "";
            if (trimmed.matches("^\\d+$")) {
                try {
                    URL url = new URL("https://cses.fi/user/" + trimmed);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    if (conn.getResponseCode() == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        in.close();
                        String html = sb.toString();
                        Matcher m = Pattern.compile("Submission count:</td><td >(\\d+)").matcher(html);
                        if (m.find()) {
                            remoteCount = Integer.parseInt(m.group(1));
                        }
                    }
                } catch (Exception ignored) {}
            }

            final int finalSolved = remoteCount > 0 ? remoteCount : inAppSolved;
            prefsRepo.saveCsesSolvedCount(finalSolved);
            requireActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    updatePlatformPillDisplay(handle, finalSolved, binding.tvHomeCses);
                }
            });
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

