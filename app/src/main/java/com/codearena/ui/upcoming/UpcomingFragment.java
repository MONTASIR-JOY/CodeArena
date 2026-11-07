package com.codearena.ui.upcoming;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.codearena.R;
import com.codearena.data.model.CfContest;
import com.codearena.data.repository.ContestRepository;
import com.codearena.databinding.FragmentUpcomingBinding;
import com.codearena.ui.adapter.UpcomingContestAdapter;
import java.util.ArrayList;
import java.util.List;

public class UpcomingFragment extends Fragment {

    private FragmentUpcomingBinding binding;
    private ContestRepository contestRepo;
    private UpcomingContestAdapter adapter;
    private final List<CfContest> allContests = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUpcomingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contestRepo = new ContestRepository(requireContext());

        setupRecyclerView();
        setupSwipeRefresh();
        setupFilterChips();

        // 1. Instant load from cache
        List<CfContest> cached = contestRepo.getCachedContests();
        if (!cached.isEmpty()) {
            allContests.clear();
            allContests.addAll(cached);
            applyFilter();
        }

        // 2. Fetch fresh multi-platform contests
        loadUpcoming();
    }

    private void setupRecyclerView() {
        adapter = new UpcomingContestAdapter();
        binding.rvUpcoming.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUpcoming.setAdapter(adapter);
        binding.btnRetry.setOnClickListener(v -> loadUpcoming());
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadUpcoming);
    }

    private void setupFilterChips() {
        binding.chipGroupUpcoming.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilter());
    }

    private void loadUpcoming() {
        binding.swipeRefresh.setRefreshing(true);
        binding.cardError.setVisibility(View.GONE);

        contestRepo.fetchUpcomingContests(new ContestRepository.Callback<List<CfContest>>() {
            @Override
            public void onSuccess(List<CfContest> result) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null) return;
                    binding.swipeRefresh.setRefreshing(false);
                    binding.cardError.setVisibility(View.GONE);

                    allContests.clear();
                    if (result != null) {
                        allContests.addAll(result);
                    }
                    applyFilter();
                });
            }

            @Override
            public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null) return;
                    binding.swipeRefresh.setRefreshing(false);
                    if (allContests.isEmpty()) {
                        binding.cardError.setVisibility(View.VISIBLE);
                        binding.tvErrorMessage.setText("Failed to load upcoming contests: " +
                                (error != null ? error.getMessage() : "Unknown error"));
                    }
                    applyFilter();
                });
            }
        });
    }

    private void applyFilter() {
        if (binding == null) return;

        int checkedId = binding.chipGroupUpcoming.getCheckedChipId();
        String platformFilter = null;
        if (checkedId == R.id.chipUpcomingCF) {
            platformFilter = "codeforces";
        } else if (checkedId == R.id.chipUpcomingLC) {
            platformFilter = "leetcode";
        } else if (checkedId == R.id.chipUpcomingCC) {
            platformFilter = "codechef";
        } else if (checkedId == R.id.chipUpcomingAC) {
            platformFilter = "atcoder";
        } else if (checkedId == R.id.chipUpcomingHR) {
            platformFilter = "hackerrank";
        }

        List<CfContest> filtered = new ArrayList<>();
        for (CfContest c : allContests) {
            if (platformFilter == null) {
                filtered.add(c);
            } else {
                String type = c.getType() != null ? c.getType().toLowerCase() : "";
                if (type.contains(platformFilter)) {
                    filtered.add(c);
                }
            }
        }

        adapter.setContests(filtered);
        binding.tvUpcomingCount.setText("Showing " + filtered.size() + " contests");
        binding.tvEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
