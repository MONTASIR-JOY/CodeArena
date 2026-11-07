package com.codearena.ui.contests;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.R;
import com.codearena.data.model.CfContest;
import com.codearena.data.repository.CodeforcesRepository;
import com.codearena.databinding.FragmentContestsBinding;
import com.codearena.ui.adapter.ContestAdapter;
import java.util.ArrayList;
import java.util.List;

public class ContestsFragment extends Fragment {

    private FragmentContestsBinding binding;
    private CodeforcesRepository cfRepo;
    private ContestAdapter adapter;

    private final List<CfContest> allContests = new ArrayList<>();
    private final List<CfContest> currentFilteredList = new ArrayList<>();
    private static final int PAGE_SIZE = 50;
    private int displayedCount = PAGE_SIZE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentContestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cfRepo = new CodeforcesRepository(requireContext());

        setupRecyclerView();
        setupSearchAndFilters();
        setupSwipeRefresh();

        // 1. INSTANT (0 ms) load from local Room DB cache
        loadCachedContests();

        // 2. Fetch fresh contests in background
        loadContests(allContests.isEmpty());
    }

    private void setupRecyclerView() {
        adapter = new ContestAdapter(contest -> {
            Bundle args = new Bundle();
            args.putLong("contest_id", contest.getId());
            args.putString("contest_name", contest.getName());
            args.putLong("duration_seconds", contest.getDurationSeconds());
            Navigation.findNavController(requireView()).navigate(R.id.contestDetailFragment, args);
        });

        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        binding.rvContests.setLayoutManager(lm);
        binding.rvContests.setAdapter(adapter);

        // Infinite scroll pagination to avoid binding 2,000 items at once
        binding.rvContests.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    int visibleItemCount = lm.getChildCount();
                    int totalItemCount = lm.getItemCount();
                    int firstVisibleItemPosition = lm.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 8
                            && firstVisibleItemPosition >= 0
                            && displayedCount < currentFilteredList.size()) {
                        displayedCount = Math.min(displayedCount + PAGE_SIZE, currentFilteredList.size());
                        adapter.setContests(new ArrayList<>(currentFilteredList.subList(0, displayedCount)));
                    }
                }
            }
        });
    }

    private void setupSearchAndFilters() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilter());
        binding.btnRetry.setOnClickListener(v -> loadContests(true));
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> loadContests(false));
    }

    private void loadCachedContests() {
        cfRepo.getCachedFinishedContests(new CodeforcesRepository.Callback<List<CfContest>>() {
            @Override
            public void onSuccess(List<CfContest> contests) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null) return;
                    if (allContests.isEmpty() && contests != null && !contests.isEmpty()) {
                        allContests.addAll(contests);
                        applyFilter();
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                // Cache miss, loadContests will fetch from network
            }
        });
    }

    private void loadContests(boolean showSpinner) {
        if (showSpinner && allContests.isEmpty()) {
            binding.swipeRefresh.setRefreshing(true);
        }
        binding.cardError.setVisibility(View.GONE);

        cfRepo.fetchContests(new CodeforcesRepository.Callback<List<CfContest>>() {
            @Override
            public void onSuccess(List<CfContest> contests) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null) return;
                    binding.swipeRefresh.setRefreshing(false);
                    allContests.clear();
                    if (contests != null) {
                        for (CfContest c : contests) {
                            if ("FINISHED".equalsIgnoreCase(c.getPhase())) {
                                allContests.add(c);
                            }
                        }
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
                        binding.tvErrorMessage.setText("Failed to load contests: " +
                                (error != null ? error.getMessage() : "Network error"));
                    }
                });
            }
        });
    }

    private void applyFilter() {
        String query = binding.etSearch.getText() != null ?
                binding.etSearch.getText().toString().trim().toLowerCase() : "";

        int checkedId = binding.chipGroupFilters.getCheckedChipId();
        String divFilter = "";
        if (checkedId == R.id.chipDiv1) divFilter = "div. 1";
        else if (checkedId == R.id.chipDiv2) divFilter = "div. 2";
        else if (checkedId == R.id.chipDiv3) divFilter = "div. 3";
        else if (checkedId == R.id.chipDiv4) divFilter = "div. 4";
        else if (checkedId == R.id.chipIcpc) divFilter = "icpc";

        currentFilteredList.clear();
        for (CfContest c : allContests) {
            String nameLower = c.getName() != null ? c.getName().toLowerCase() : "";
            String idStr = String.valueOf(c.getId());

            boolean matchesQuery = query.isEmpty() || nameLower.contains(query) || idStr.contains(query);
            boolean matchesDiv = divFilter.isEmpty() || nameLower.contains(divFilter);

            if (matchesQuery && matchesDiv) {
                currentFilteredList.add(c);
            }
        }

        // Reset display count to PAGE_SIZE on filter change
        displayedCount = Math.min(PAGE_SIZE, currentFilteredList.size());
        adapter.setContests(new ArrayList<>(currentFilteredList.subList(0, displayedCount)));
        binding.tvEmptyState.setVisibility(currentFilteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
