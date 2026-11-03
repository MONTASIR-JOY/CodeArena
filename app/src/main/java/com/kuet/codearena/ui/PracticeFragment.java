package com.kuet.codearena.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.kuet.codearena.R;
import com.kuet.codearena.coach.AiCoachClient;
import com.kuet.codearena.coach.CoachEngine;
import com.kuet.codearena.coach.GeminiAiCoachClient;
import com.kuet.codearena.coach.model.AnonymousUserStats;
import com.kuet.codearena.data.db.entity.ProblemEntity;
import com.kuet.codearena.data.db.entity.RunEntity;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.data.remote.Endpoints;
import com.kuet.codearena.data.repository.SyncRepository;
import com.kuet.codearena.data.repository.TagMasteryRepository;
import com.kuet.codearena.databinding.FragmentPracticeBinding;
import com.kuet.codearena.ui.adapter.LadderStepAdapter;
import com.kuet.codearena.ui.adapter.SolvedTodayAdapter;
import com.kuet.codearena.ui.adapter.TagStatAdapter;
import com.kuet.codearena.ui.model.TagMasteryModel;
import com.kuet.codearena.util.AppExecutors;
import com.kuet.codearena.util.PreferenceHelper;
import com.kuet.codearena.util.XmlTipParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment displaying tag mastery radar charts, problem ladder progressions, and AI coaching recommendations.
 */
public class PracticeFragment extends Fragment implements
        SolvedTodayAdapter.OnSolveClickListener,
        TagStatAdapter.OnTagClickListener,
        LadderStepAdapter.OnLadderStepClickListener {

    private FragmentPracticeBinding binding;
    private SyncRepository syncRepository;
    private TagMasteryRepository masteryRepository;
    private PreferenceHelper preferenceHelper;
    private AiCoachClient aiCoachClient;

    private SolvedTodayAdapter solvedTodayAdapter;
    private TagStatAdapter tagStatAdapter;
    private LadderStepAdapter ladderAdapter;

    private String currentLadderTag = "dp";
    private int unlockedLadderStep = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPracticeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        syncRepository = SyncRepository.getInstance(requireContext());
        masteryRepository = TagMasteryRepository.getInstance(requireContext());
        preferenceHelper = new PreferenceHelper(requireContext());
        aiCoachClient = new GeminiAiCoachClient();

        setupAdapters();
        observeSolvedToday();
        loadAnalyticsAndLadder();
    }

    private void setupAdapters() {
        solvedTodayAdapter = new SolvedTodayAdapter(this);
        binding.rvSolvedToday.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvSolvedToday.setAdapter(solvedTodayAdapter);

        tagStatAdapter = new TagStatAdapter(this);
        binding.rvTagStats.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTagStats.setAdapter(tagStatAdapter);

        ladderAdapter = new LadderStepAdapter(this);
        binding.rvPracticeLadder.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPracticeLadder.setAdapter(ladderAdapter);
    }

    private void observeSolvedToday() {
        syncRepository.getTodaySolvesLive().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                binding.tvEmptySolvedToday.setVisibility(View.VISIBLE);
                binding.rvSolvedToday.setVisibility(View.GONE);
            } else {
                binding.tvEmptySolvedToday.setVisibility(View.GONE);
                binding.rvSolvedToday.setVisibility(View.VISIBLE);
                solvedTodayAdapter.submitList(list);
            }
        });
    }

    private void loadAnalyticsAndLadder() {
        masteryRepository.loadMasteryData((tagStats, recentRuns, weakestTag) -> {
            if (!isAdded() || binding == null) return;

            // 1. Submit Tag stats
            tagStatAdapter.submitList(tagStats);

            // 2. Setup Top 8 Radar Chart
            setupRadarChart(tagStats);

            // 3. Setup Weakness Bar Chart
            setupWeaknessChart(tagStats);

            // 4. Setup Contest Score Line Chart
            setupScoreLineChart(recentRuns);

            // 5. Setup Recommended Focus Chips & XML Tip
            currentLadderTag = weakestTag;
            setupFocusChipsAndTip(tagStats, weakestTag);

            // 6. Build Ladder for weakest tag
            loadLadderForTag(currentLadderTag);

            // 7. Request AI coaching message
            requestCoachingAdvice(tagStats);
        });
    }

    private void setupRadarChart(List<TagMasteryModel> stats) {
        List<RadarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int count = Math.min(8, stats.size());
        for (int i = 0; i < count; i++) {
            TagMasteryModel m = stats.get(i);
            entries.add(new RadarEntry((float) m.getSolvedCount()));
            labels.add(m.getTag());
        }

        RadarDataSet dataSet = new RadarDataSet(entries, "Solves per Tag");
        dataSet.setColor(Color.parseColor("#1877F2"));
        dataSet.setFillColor(Color.parseColor("#1877F2"));
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(120);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(9f);

        RadarData data = new RadarData(dataSet);
        binding.radarChartTags.setData(data);
        binding.radarChartTags.getDescription().setEnabled(false);
        binding.radarChartTags.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.radarChartTags.getXAxis().setTextColor(Color.LTGRAY);
        binding.radarChartTags.getYAxis().setTextColor(Color.LTGRAY);
        binding.radarChartTags.getLegend().setTextColor(Color.LTGRAY);
        binding.radarChartTags.invalidate();
    }

    private void setupWeaknessChart(List<TagMasteryModel> stats) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int count = Math.min(6, stats.size());
        for (int i = 0; i < count; i++) {
            TagMasteryModel m = stats.get(i);
            entries.add(new BarEntry(i, m.getUnsolvedCount()));
            labels.add(m.getTag());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Pending / Unsolved");
        dataSet.setColor(Color.parseColor("#FF5252"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        binding.barChartWeakness.setData(barData);
        binding.barChartWeakness.getDescription().setEnabled(false);
        binding.barChartWeakness.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.barChartWeakness.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.barChartWeakness.getXAxis().setTextColor(Color.LTGRAY);
        binding.barChartWeakness.getXAxis().setGranularity(1f);
        binding.barChartWeakness.getAxisLeft().setTextColor(Color.LTGRAY);
        binding.barChartWeakness.getAxisRight().setEnabled(false);
        binding.barChartWeakness.getLegend().setTextColor(Color.LTGRAY);
        binding.barChartWeakness.invalidate();
    }

    private void setupScoreLineChart(List<RunEntity> recentRuns) {
        List<Entry> entries = new ArrayList<>();
        if (recentRuns != null && !recentRuns.isEmpty()) {
            for (int i = 0; i < recentRuns.size(); i++) {
                RunEntity run = recentRuns.get(i);
                entries.add(new Entry(i + 1, (float) run.getTotalScore()));
            }
        } else {
            // Placeholder demo points if no runs completed yet
            entries.add(new Entry(1, 450f));
            entries.add(new Entry(2, 620f));
            entries.add(new Entry(3, 890f));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Contest Score");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(9f);

        LineData lineData = new LineData(dataSet);
        binding.lineChartScores.setData(lineData);
        binding.lineChartScores.getDescription().setEnabled(false);
        binding.lineChartScores.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.lineChartScores.getXAxis().setTextColor(Color.LTGRAY);
        binding.lineChartScores.getAxisLeft().setTextColor(Color.LTGRAY);
        binding.lineChartScores.getAxisRight().setEnabled(false);
        binding.lineChartScores.getLegend().setTextColor(Color.LTGRAY);
        binding.lineChartScores.invalidate();
    }

    private void setupFocusChipsAndTip(List<TagMasteryModel> stats, String weakestTag) {
        binding.chipGroupFocusTags.removeAllViews();
        List<String> top3 = CoachEngine.recommendWeakTags(stats);

        for (String tag : top3) {
            Chip chip = new Chip(requireContext());
            chip.setText(tag);
            chip.setCheckable(true);
            if (tag.equalsIgnoreCase(weakestTag)) {
                chip.setChecked(true);
            }
            chip.setOnClickListener(v -> {
                currentLadderTag = tag;
                loadLadderForTag(tag);
                updateXmlTip(tag);
            });
            binding.chipGroupFocusTags.addView(chip);
        }

        updateXmlTip(weakestTag);
    }

    private void updateXmlTip(String tag) {
        XmlTipParser.TagTip tip = XmlTipParser.getTipForTag(requireContext(), tag);
        if (tip != null) {
            binding.tvTipTitle.setText("💡 " + tip.getTitle());
            binding.tvTipAdvice.setText(tip.getAdvice());
        }
    }

    private void loadLadderForTag(String tag) {
        double rating = preferenceHelper.getPracticeRating();
        binding.tvLadderSubtext.setText(String.format(Locale.getDefault(),
                "Topic: %s • Baseline Rating: %.0f • Climb 5 progressive rungs to level up",
                tag.toUpperCase(), rating));

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<ProblemEntity> ladder = CoachEngine.buildPracticeLadder(requireContext(), tag, rating);
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (isAdded() && binding != null) {
                    ladderAdapter.setProblems(ladder, unlockedLadderStep);
                }
            });
        });
    }

    private void requestCoachingAdvice(List<TagMasteryModel> stats) {
        double rating = preferenceHelper.getPracticeRating();
        List<String> weakTags = CoachEngine.recommendWeakTags(stats);

        AnonymousUserStats anonymousStats = new AnonymousUserStats(
                rating,
                1, // current streak placeholder
                3, // pending upsolves
                weakTags,
                solvedTodayAdapter.getItemCount(),
                2
        );

        aiCoachClient.getCoachingMessage(anonymousStats, (coachingMessage, isAiGenerated) -> {
            if (isAdded() && binding != null) {
                binding.tvCoachAdvice.setText(coachingMessage);
                if (isAiGenerated) {
                    binding.tvAiBadge.setText("AI-generated (Gemini)");
                    binding.tvAiBadge.setBackgroundColor(requireContext().getColor(R.color.accent_secondary));
                } else {
                    binding.tvAiBadge.setText("Rule-based Engine");
                    binding.tvAiBadge.setBackgroundColor(requireContext().getColor(R.color.cf_blue));
                }
            }
        });
    }

    @Override
    public void onSolveClick(SolveEntity solve) {
        String url;
        if (solve.getProblemId().startsWith("CF-")) {
            url = Endpoints.CF_PROBLEM_WEB_BASE + solve.getProblemId().substring(3);
        } else if (solve.getProblemId().startsWith("AT-")) {
            url = "https://atcoder.jp/contests";
        } else {
            url = "https://leetcode.com/problems/" + solve.getTitle().toLowerCase().replace(" ", "-");
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void onTagClick(TagMasteryModel tagModel) {
        currentLadderTag = tagModel.getTag();
        loadLadderForTag(currentLadderTag);
        updateXmlTip(currentLadderTag);
        Snackbar.make(binding.practiceCoordinator, "Loaded ladder for: " + currentLadderTag, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onStepClick(ProblemEntity problem, boolean isUnlocked) {
        if (!isUnlocked) {
            Snackbar.make(binding.practiceCoordinator, "Solve previous rung to unlock this step!", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Implicit intent to open problem in browser
        String url = Endpoints.CF_PROBLEM_WEB_BASE + problem.getContestId() + "/problem/" + problem.getProblemIndex();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);

        // Advance ladder unlock on practice
        if (unlockedLadderStep < 5) {
            unlockedLadderStep++;
            loadLadderForTag(currentLadderTag);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
