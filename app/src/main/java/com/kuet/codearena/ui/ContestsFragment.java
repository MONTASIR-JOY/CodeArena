package com.kuet.codearena.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.kuet.codearena.R;
import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.repository.ContestRepository;
import com.kuet.codearena.databinding.FragmentContestsBinding;
import com.kuet.codearena.ui.adapter.ContestAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment allowing browsing, searching, and division filtering of past Codeforces contests.
 */
public class ContestsFragment extends Fragment implements ContestAdapter.OnContestClickListener {

    private FragmentContestsBinding binding;
    private ContestRepository repository;
    private ContestAdapter adapter;

    private List<ContestEntity> allContests = new ArrayList<>();
    private String currentSearchQuery = "";
    private String selectedDivision = "ALL";

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

        repository = ContestRepository.getInstance(requireContext());
        setupRecyclerView();
        setupFiltersAndSearch();
        observeContests();
    }

    private void setupRecyclerView() {
        adapter = new ContestAdapter(this);
        binding.rvContests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvContests.setAdapter(adapter);

        binding.swipeRefreshContests.setOnRefreshListener(this::fetchRemoteContests);
        binding.btnRefreshContests.setOnClickListener(v -> fetchRemoteContests());
    }

    private void setupFiltersAndSearch() {
        binding.etSearchContest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim().toLowerCase();
                binding.ivClearSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.ivClearSearch.setOnClickListener(v -> {
            binding.etSearchContest.setText("");
        });

        binding.chipGroupDivisions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipDiv1) {
                selectedDivision = "Div. 1";
            } else if (checkedId == R.id.chipDiv2) {
                selectedDivision = "Div. 2";
            } else if (checkedId == R.id.chipDiv3) {
                selectedDivision = "Div. 3";
            } else if (checkedId == R.id.chipEdu) {
                selectedDivision = "Educational";
            } else {
                selectedDivision = "ALL";
            }
            applyFilter();
        });
    }

    private void observeContests() {
        binding.progressContests.setVisibility(View.VISIBLE);
        repository.getPastContests().observe(getViewLifecycleOwner(), contests -> {
            binding.progressContests.setVisibility(View.GONE);
            binding.swipeRefreshContests.setRefreshing(false);
            if (contests != null && !contests.isEmpty()) {
                allContests = contests;
                applyFilter();
            } else {
                fetchRemoteContests();
            }
        });
    }

    private void fetchRemoteContests() {
        binding.progressContests.setVisibility(View.VISIBLE);
        repository.refreshPastContests(new ContestRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    binding.progressContests.setVisibility(View.GONE);
                    binding.swipeRefreshContests.setRefreshing(false);
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    binding.progressContests.setVisibility(View.GONE);
                    binding.swipeRefreshContests.setRefreshing(false);
                    if (adapter.getItemCount() == 0) {
                        binding.emptyContestsView.setVisibility(View.VISIBLE);
                    }
                    Snackbar.make(binding.contestsCoordinator, message, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry, v -> fetchRemoteContests())
                            .show();
                }
            }
        });
    }

    private void applyFilter() {
        List<ContestEntity> filtered = new ArrayList<>();
        for (ContestEntity c : allContests) {
            boolean matchesDivision = "ALL".equals(selectedDivision) ||
                    (c.getDivision() != null && c.getDivision().toLowerCase().contains(selectedDivision.toLowerCase()));
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    c.getName().toLowerCase().contains(currentSearchQuery) ||
                    String.valueOf(c.getId()).contains(currentSearchQuery);

            if (matchesDivision && matchesSearch) {
                filtered.add(c);
            }
        }

        adapter.submitList(filtered);
        binding.emptyContestsView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvContests.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onContestClick(ContestEntity contest) {
        // Explicit intent to ContestDetailActivity
        Intent intent = ContestDetailActivity.newIntent(
                requireContext(),
                contest.getId(),
                contest.getName(),
                contest.getDurationSeconds(),
                contest.getDivision()
        );
        startActivity(intent);
    }

    @Override
    public void onStartVirtualClick(ContestEntity contest) {
        // Explicit intent to VirtualRunActivity
        Intent intent = new Intent(requireContext(), VirtualRunActivity.class);
        intent.putExtra(VirtualRunActivity.EXTRA_CONTEST_ID, contest.getId());
        intent.putExtra(VirtualRunActivity.EXTRA_CONTEST_NAME, contest.getName());
        intent.putExtra(VirtualRunActivity.EXTRA_DURATION_SECONDS, contest.getDurationSeconds());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
