package com.kuet.codearena.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.kuet.codearena.data.repository.FirestoreRepository;
import com.kuet.codearena.databinding.FragmentLeaderboardBinding;
import com.kuet.codearena.ui.adapter.LeaderboardAdapter;
import com.kuet.codearena.ui.model.LeaderboardUserModel;
import com.kuet.codearena.util.PreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment rendering global competitive programming leaderboards across solved counts, ratings, and practice tiers.
 */
public class LeaderboardFragment extends Fragment {

    private FragmentLeaderboardBinding binding;
    private FirestoreRepository firestoreRepository;
    private PreferenceHelper preferenceHelper;
    private LeaderboardAdapter adapter;

    private final List<LeaderboardUserModel> cachedUsers = new ArrayList<>();
    private final List<String> activeDateKeys = new ArrayList<>();
    private LeaderboardAdapter.Mode currentMode = LeaderboardAdapter.Mode.SOLVED;
    private int selectedTab = 0; // 0: Solved, 1: CF Rating, 2: Practice Rating

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreRepository = FirestoreRepository.getInstance(requireContext());
        preferenceHelper = new PreferenceHelper(requireContext());

        setupRecyclerView();
        setupTabs();
        setupFilters();

        // Upload current user profile & stats to cloud
        firestoreRepository.uploadUserStatsToFirestore();

        // Default window: Today
        computeDateWindow(1);
        loadLeaderboardData();
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter();
        binding.rvLeaderboard.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLeaderboard.setAdapter(adapter);
    }

    private void setupTabs() {
        binding.tabLayoutLeaderboard.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                updateTabVisibility();
                applyFilterAndSort();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateTabVisibility() {
        if (selectedTab == 0) {
            binding.layoutSolvedFilters.setVisibility(View.VISIBLE);
            binding.layoutPracticeRatingChart.setVisibility(View.GONE);
            currentMode = binding.chipMetricPoints.isChecked()
                    ? LeaderboardAdapter.Mode.POINTS
                    : LeaderboardAdapter.Mode.SOLVED;
        } else if (selectedTab == 1) {
            binding.layoutSolvedFilters.setVisibility(View.GONE);
            binding.layoutPracticeRatingChart.setVisibility(View.GONE);
            currentMode = LeaderboardAdapter.Mode.CF_RATING;
        } else {
            binding.layoutSolvedFilters.setVisibility(View.GONE);
            binding.layoutPracticeRatingChart.setVisibility(View.VISIBLE);
            currentMode = LeaderboardAdapter.Mode.PRACTICE_RATING;
            setupPracticeRatingLineChart();
        }
    }

    private void setupFilters() {
        binding.chipToday.setOnClickListener(v -> {
            computeDateWindow(1);
            applyFilterAndSort();
        });

        binding.chip7Days.setOnClickListener(v -> {
            computeDateWindow(7);
            applyFilterAndSort();
        });

        binding.chip10Days.setOnClickListener(v -> {
            computeDateWindow(10);
            applyFilterAndSort();
        });

        binding.chip30Days.setOnClickListener(v -> {
            computeDateWindow(30);
            applyFilterAndSort();
        });

        binding.chipCustomRange.setOnClickListener(v -> showDateRangePicker());

        binding.chipGroupMetric.setOnCheckedChangeListener((group, checkedId) -> {
            if (selectedTab == 0) {
                currentMode = (checkedId == binding.chipMetricPoints.getId())
                        ? LeaderboardAdapter.Mode.POINTS
                        : LeaderboardAdapter.Mode.SOLVED;
                applyFilterAndSort();
            }
        });
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Custom Window")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null && selection.first != null && selection.second != null) {
                computeDateRange(selection.first, selection.second);
                binding.chipCustomRange.setText("Custom: " + activeDateKeys.size() + "d");
                applyFilterAndSort();
            }
        });

        picker.show(getChildFragmentManager(), "DATE_RANGE_PICKER");
    }

    private void computeDateWindow(int days) {
        activeDateKeys.clear();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < days; i++) {
            activeDateKeys.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
    }

    private void computeDateRange(long startMs, long endMs) {
        activeDateKeys.clear();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startMs);

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(endMs);

        while (!cal.after(endCal)) {
            activeDateKeys.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void loadLeaderboardData() {
        binding.progressLeaderboard.setVisibility(View.VISIBLE);
        firestoreRepository.fetchLeaderboardUsers((users, isCloudData) -> {
            if (!isAdded() || binding == null) return;
            binding.progressLeaderboard.setVisibility(View.GONE);

            cachedUsers.clear();
            if (users != null) {
                cachedUsers.addAll(users);
            }

            if (isCloudData) {
                binding.tvCloudStatus.setText("Cloud Firestore: Live Standings (Top 200)");
                binding.tvCloudStatus.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                binding.tvCloudStatus.setText("Cloud Unavailable: Offline Competitive Benchmarks");
                binding.tvCloudStatus.setTextColor(Color.parseColor("#FF9800"));
            }

            applyFilterAndSort();
        });
    }

    private void applyFilterAndSort() {
        if (cachedUsers.isEmpty()) {
            binding.tvEmptyLeaderboard.setVisibility(View.VISIBLE);
            binding.rvLeaderboard.setVisibility(View.GONE);
            return;
        }

        binding.tvEmptyLeaderboard.setVisibility(View.GONE);
        binding.rvLeaderboard.setVisibility(View.VISIBLE);

        List<LeaderboardUserModel> sorted = new ArrayList<>(cachedUsers);

        if (currentMode == LeaderboardAdapter.Mode.SOLVED) {
            Collections.sort(sorted, (a, b) -> Integer.compare(
                    b.getSolvedInWindow(activeDateKeys),
                    a.getSolvedInWindow(activeDateKeys)
            ));
        } else if (currentMode == LeaderboardAdapter.Mode.POINTS) {
            Collections.sort(sorted, (a, b) -> Integer.compare(
                    b.getPointsInWindow(activeDateKeys),
                    a.getPointsInWindow(activeDateKeys)
            ));
        } else if (currentMode == LeaderboardAdapter.Mode.CF_RATING) {
            Collections.sort(sorted, (a, b) -> Integer.compare(b.getCfRating(), a.getCfRating()));
        } else {
            Collections.sort(sorted, (a, b) -> Double.compare(b.getPracticeRating(), a.getPracticeRating()));
        }

        adapter.setData(sorted, currentMode, activeDateKeys);
    }

    private void setupPracticeRatingLineChart() {
        double currentRating = preferenceHelper.getPracticeRating();
        List<Entry> entries = new ArrayList<>();

        // Generate visual ELO progression path leading up to current practice rating
        entries.add(new Entry(1, 800f));
        entries.add(new Entry(2, (float) Math.max(800, currentRating * 0.75)));
        entries.add(new Entry(3, (float) Math.max(800, currentRating * 0.88)));
        entries.add(new Entry(4, (float) Math.max(800, currentRating * 0.95)));
        entries.add(new Entry(5, (float) currentRating));

        LineDataSet dataSet = new LineDataSet(entries, "Practice Rating History");
        dataSet.setColor(Color.parseColor("#FF9800"));
        dataSet.setCircleColor(Color.parseColor("#FF9800"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(9f);

        LineData data = new LineData(dataSet);
        binding.lineChartPracticeRating.setData(data);
        binding.lineChartPracticeRating.getDescription().setEnabled(false);
        binding.lineChartPracticeRating.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.lineChartPracticeRating.getXAxis().setTextColor(Color.LTGRAY);
        binding.lineChartPracticeRating.getAxisLeft().setTextColor(Color.LTGRAY);
        binding.lineChartPracticeRating.getAxisRight().setEnabled(false);
        binding.lineChartPracticeRating.getLegend().setTextColor(Color.LTGRAY);
        binding.lineChartPracticeRating.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
