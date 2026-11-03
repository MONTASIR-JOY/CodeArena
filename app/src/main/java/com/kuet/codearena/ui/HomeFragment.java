package com.kuet.codearena.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.kuet.codearena.data.db.entity.DailyStatEntity;
import com.kuet.codearena.data.db.entity.RunEntity;
import com.kuet.codearena.data.db.entity.UpsolveItemEntity;
import com.kuet.codearena.data.remote.Endpoints;
import com.kuet.codearena.data.repository.SyncRepository;
import com.kuet.codearena.data.repository.UpsolveRepository;
import com.kuet.codearena.data.repository.VirtualRunRepository;
import com.kuet.codearena.databinding.FragmentHomeBinding;
import com.kuet.codearena.ui.adapter.RunHistoryAdapter;
import com.kuet.codearena.ui.adapter.UpsolveAdapter;
import com.kuet.codearena.util.PreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Main dashboard fragment featuring the daily upsolve target ring, active streak counters, and quick queues.
 */
public class HomeFragment extends Fragment implements UpsolveAdapter.OnUpsolveActionListener, RunHistoryAdapter.OnHistoryClickListener {

    private FragmentHomeBinding binding;
    private UpsolveRepository upsolveRepository;
    private VirtualRunRepository runRepository;
    private SyncRepository syncRepository;
    private PreferenceHelper preferenceHelper;

    private UpsolveAdapter upsolveAdapter;
    private RunHistoryAdapter historyAdapter;

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

        upsolveRepository = UpsolveRepository.getInstance(requireContext());
        runRepository = VirtualRunRepository.getInstance(requireContext());
        syncRepository = SyncRepository.getInstance(requireContext());
        preferenceHelper = new PreferenceHelper(requireContext());

        setupRecyclerViews();
        observeDailyProgress();
        observePendingUpsolves();
        observeRunHistory();
        observeStreaks();
    }

    private void setupRecyclerViews() {
        upsolveAdapter = new UpsolveAdapter(this);
        binding.rvHomeUpsolves.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHomeUpsolves.setAdapter(upsolveAdapter);

        historyAdapter = new RunHistoryAdapter(this);
        binding.rvRecentRuns.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentRuns.setAdapter(historyAdapter);
    }

    private void observeDailyProgress() {
        int target = preferenceHelper.getDailyTarget();
        binding.circularDailyTarget.setMaxProgress(target);

        upsolveRepository.getTodaySolvedUpsolves().observe(getViewLifecycleOwner(), solvedList -> {
            int solvedCount = solvedList != null ? solvedList.size() : 0;
            binding.circularDailyTarget.setProgress(solvedCount);
            binding.circularDailyTarget.setCenterText(solvedCount + " / " + target);
            binding.circularDailyTarget.setCenterSubtext("Upsolves Today");
        });

        double rating = preferenceHelper.getPracticeRating();
        binding.tvHomePracticeRating.setText(String.format(Locale.getDefault(), "%.0f", rating));
    }

    private void observePendingUpsolves() {
        upsolveRepository.getPendingUpsolves().observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                upsolveAdapter.submitList(items);
                binding.tvUpsolveCountBadge.setText(items.size() + " Due");
                binding.tvEmptyUpsolve.setVisibility(View.GONE);
                binding.rvHomeUpsolves.setVisibility(View.VISIBLE);
            } else {
                upsolveAdapter.submitList(new ArrayList<>());
                binding.tvUpsolveCountBadge.setText("0 Due");
                binding.tvEmptyUpsolve.setVisibility(View.VISIBLE);
                binding.rvHomeUpsolves.setVisibility(View.GONE);
            }
        });
    }

    private void observeRunHistory() {
        runRepository.getAllRunsLive().observe(getViewLifecycleOwner(), runs -> {
            if (runs != null && !runs.isEmpty()) {
                historyAdapter.submitList(runs);
                binding.tvEmptyRuns.setVisibility(View.GONE);
                binding.rvRecentRuns.setVisibility(View.VISIBLE);
            } else {
                historyAdapter.submitList(new ArrayList<>());
                binding.tvEmptyRuns.setVisibility(View.VISIBLE);
                binding.rvRecentRuns.setVisibility(View.GONE);
            }
        });
    }

    private void observeStreaks() {
        syncRepository.getAllDailyStatsLive().observe(getViewLifecycleOwner(), stats -> {
            int currentStreak = 0;
            int longestStreak = 0;

            if (stats != null && !stats.isEmpty()) {
                Set<String> activeDates = new HashSet<>();
                for (DailyStatEntity s : stats) {
                    if (s.getSolvedCount() > 0) {
                        activeDates.add(s.getDateStr());
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
                Calendar cal = Calendar.getInstance();

                // Compute current streak (counting backwards from today or yesterday)
                String todayStr = sdf.format(cal.getTime());
                cal.add(Calendar.DAY_OF_YEAR, -1);
                String yesterdayStr = sdf.format(cal.getTime());

                Calendar checkCal = Calendar.getInstance();
                if (!activeDates.contains(todayStr) && activeDates.contains(yesterdayStr)) {
                    checkCal.add(Calendar.DAY_OF_YEAR, -1);
                }

                while (activeDates.contains(sdf.format(checkCal.getTime()))) {
                    currentStreak++;
                    checkCal.add(Calendar.DAY_OF_YEAR, -1);
                }

                // Compute longest streak
                List<String> sortedDates = new ArrayList<>(activeDates);
                Collections.sort(sortedDates);
                int tempStreak = 0;
                Calendar streakCal = Calendar.getInstance();

                for (int i = 0; i < sortedDates.size(); i++) {
                    if (i == 0) {
                        tempStreak = 1;
                    } else {
                        try {
                            Date prevDate = sdf.parse(sortedDates.get(i - 1));
                            Date currDate = sdf.parse(sortedDates.get(i));
                            if (prevDate != null && currDate != null) {
                                long diffDays = (currDate.getTime() - prevDate.getTime()) / (24L * 3600L * 1000L);
                                if (diffDays == 1) {
                                    tempStreak++;
                                } else if (diffDays > 1) {
                                    tempStreak = 1;
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                    if (tempStreak > longestStreak) {
                        longestStreak = tempStreak;
                    }
                }
            }

            binding.tvCurrentStreak.setText(currentStreak + " Days");
            binding.tvLongestStreak.setText(longestStreak + " Days");
        });
    }

    @Override
    public void onProblemClick(UpsolveItemEntity item) {
        // Implicit Intent to open problem in browser
        String url = Endpoints.CF_PROBLEM_WEB_BASE + item.getContestId() + "/problem/" + item.getProblemIndex();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void onVerifyClick(UpsolveItemEntity item) {
        Snackbar.make(binding.homeCoordinator, "Verifying solve on Codeforces API...", Snackbar.LENGTH_SHORT).show();
        upsolveRepository.markSolvedAndVerify(item.getId(), new UpsolveRepository.VerificationCallback() {
            @Override
            public void onVerified(boolean apiVerified, String message) {
                if (isAdded()) {
                    Snackbar.make(binding.homeCoordinator, message, Snackbar.LENGTH_LONG).show();
                    observeDailyProgress();
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    Snackbar.make(binding.homeCoordinator, "Verification error: " + error, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onHistoryClick(RunEntity run) {
        // Explicit Intent reopening PostMortemActivity
        Intent intent = PostMortemActivity.newIntent(requireContext(), run.getRunId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        observeDailyProgress();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
