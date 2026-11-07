package com.codearena.ui.mastery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.codearena.R;
import com.codearena.data.db.AppDatabase;
import com.codearena.data.db.entity.UpsolveItemEntity;
import com.codearena.data.model.CfProblem;
import com.codearena.data.model.CfProblemsetResult;
import com.codearena.data.model.CfSubmission;
import com.codearena.data.repository.CodeforcesRepository;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentMasteryBinding;
import com.codearena.ui.adapter.ProblemAdapter;
import com.codearena.util.SpacedRepetition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MasteryFragment extends Fragment {

    private FragmentMasteryBinding binding;
    private AppDatabase db;
    private UserPreferencesRepository prefsRepo;
    private CodeforcesRepository cfRepo;
    private ProblemAdapter problemAdapter;

    private final Set<String> solvedProblemKeys = new HashSet<>();
    private final Map<String, List<CfProblem>> cachedTagProblems = new HashMap<>();
    private final List<CfProblem> currentTagRawProblems = new ArrayList<>();

    private String selectedTag = "dp";
    private String difficultyTier = "ALL";
    private boolean isUserSolvedLoaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMasteryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        prefsRepo = new UserPreferencesRepository(requireContext());
        cfRepo = new CodeforcesRepository(requireContext());

        setupRecyclerView();
        setupFilters();
        updateSubtitleWithHandle();
        loadUserSolvedAndTagProblems(selectedTag);
    }

    private void updateSubtitleWithHandle() {
        String handle = prefsRepo.getHandle();
        if (handle != null && !handle.trim().isEmpty()) {
            binding.tvMasterySubtitle.setText("Unsolved problems for " + handle + " · Tap to practice on CF");
        } else {
            binding.tvMasterySubtitle.setText("Set your handle in Settings to filter out your solved problems");
        }
    }

    private void setupRecyclerView() {
        problemAdapter = new ProblemAdapter(new ProblemAdapter.OnProblemClickListener() {
            @Override
            public void onProblemClick(CfProblem problem) {
                String url = "https://codeforces.com/contest/" + problem.getContestId() + "/problem/" + problem.getIndex();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }

            @Override
            public void onProblemLongClick(CfProblem problem) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    long now = System.currentTimeMillis();
                    long nextReview = SpacedRepetition.calculateNextReviewEpochMillis(0, prefsRepo.isFastReviewDebugMode());
                    UpsolveItemEntity item = new UpsolveItemEntity(
                            problem.getContestId(),
                            problem.getIndex(),
                            problem.getName(),
                            problem.getRating(),
                            problem.getTags(),
                            now,
                            0,
                            nextReview,
                            0,
                            false
                    );
                    db.upsolveDao().insertUpsolveItem(item);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Queued " + problem.getName() + " in Upsolve!", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        binding.rvProblems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvProblems.setAdapter(problemAdapter);
    }

    private void setupFilters() {
        // Tag selection chips
        binding.chipGroupTags.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipTagDp) selectTag("dp");
            else if (id == R.id.chipTagGreedy) selectTag("greedy");
            else if (id == R.id.chipTagMath) selectTag("math");
            else if (id == R.id.chipTagGraphs) selectTag("graphs");
            else if (id == R.id.chipTagDs) selectTag("data structures");
            else if (id == R.id.chipTagStrings) selectTag("strings");
            else if (id == R.id.chipTagTrees) selectTag("trees");
            else if (id == R.id.chipTagBs) selectTag("binary search");
            else if (id == R.id.chipTagGeometry) selectTag("geometry");
            else if (id == R.id.chipTagNumberTheory) selectTag("number theory");
            else if (id == R.id.chipTagTwoPointers) selectTag("two pointers");
            else if (id == R.id.chipTagCombinatorics) selectTag("combinatorics");
            else if (id == R.id.chipTagBitmasks) selectTag("bitmasks");
        });

        // Search bar custom tag entry
        binding.etTagSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String input = binding.etTagSearch.getText() != null ? binding.etTagSearch.getText().toString().trim().toLowerCase() : "";
                if (!input.isEmpty()) {
                    selectTag(input);
                }
                return true;
            }
            return false;
        });

        // Difficulty filter chips
        binding.chipGroupDifficulty.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipDiffAll) difficultyTier = "ALL";
            else if (id == R.id.chipDiff800_900) difficultyTier = "800_900";
            else if (id == R.id.chipDiff1000_1100) difficultyTier = "1000_1100";
            else if (id == R.id.chipDiff1200_1300) difficultyTier = "1200_1300";
            else if (id == R.id.chipDiff1400_1500) difficultyTier = "1400_1500";
            else if (id == R.id.chipDiff1600_1700) difficultyTier = "1600_1700";
            else if (id == R.id.chipDiff1800_1900) difficultyTier = "1800_1900";
            else if (id == R.id.chipDiff2000_2100) difficultyTier = "2000_2100";
            else if (id == R.id.chipDiff2200_2300) difficultyTier = "2200_2300";
            else if (id == R.id.chipDiff2400Plus) difficultyTier = "2400_PLUS";
            applyFilterAndRender();
        });
    }

    private void selectTag(String tag) {
        selectedTag = tag;
        binding.tvStatusTag.setText("Showing unsolved for: " + tag);
        loadProblemsForTag(tag);
    }

    private void loadUserSolvedAndTagProblems(String tag) {
        String handle = prefsRepo.getHandle();
        if (handle == null || handle.trim().isEmpty() || isUserSolvedLoaded) {
            selectTag(tag);
            return;
        }

        binding.progressLoading.setVisibility(View.VISIBLE);
        cfRepo.fetchUserSubmissions(handle, 10000, new CodeforcesRepository.Callback<List<CfSubmission>>() {
            @Override
            public void onSuccess(List<CfSubmission> submissions) {
                if (submissions != null) {
                    for (CfSubmission sub : submissions) {
                        if ("OK".equals(sub.getVerdict()) && sub.getProblem() != null) {
                            if (sub.getContestId() != null && sub.getProblem().getIndex() != null) {
                                solvedProblemKeys.add(sub.getContestId() + "_" + sub.getProblem().getIndex().toUpperCase());
                            }
                            if (sub.getProblem().getName() != null) {
                                solvedProblemKeys.add(sub.getProblem().getName().trim().toLowerCase());
                            }
                        }
                    }
                }
                isUserSolvedLoaded = true;
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> selectTag(tag));
            }

            @Override
            public void onError(Exception error) {
                isUserSolvedLoaded = true;
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> selectTag(tag));
            }
        });
    }

    private void loadProblemsForTag(String tag) {
        if (cachedTagProblems.containsKey(tag)) {
            currentTagRawProblems.clear();
            currentTagRawProblems.addAll(cachedTagProblems.get(tag));
            applyFilterAndRender();
            return;
        }

        binding.progressLoading.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);

        cfRepo.fetchProblemsetByTag(tag, new CodeforcesRepository.Callback<CfProblemsetResult>() {
            @Override
            public void onSuccess(CfProblemsetResult result) {
                if (!isAdded()) return;
                List<CfProblem> list = (result != null && result.getProblems() != null)
                        ? result.getProblems() : new ArrayList<>();
                cachedTagProblems.put(tag, list);

                requireActivity().runOnUiThread(() -> {
                    binding.progressLoading.setVisibility(View.GONE);
                    if (tag.equalsIgnoreCase(selectedTag)) {
                        currentTagRawProblems.clear();
                        currentTagRawProblems.addAll(list);
                        applyFilterAndRender();
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    binding.progressLoading.setVisibility(View.GONE);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    binding.tvEmpty.setText("Failed to load problems: " + error.getMessage());
                });
            }
        });
    }

    private void applyFilterAndRender() {
        List<CfProblem> unsolved = new ArrayList<>();
        for (CfProblem p : currentTagRawProblems) {
            String contestKey = p.getContestId() + "_" + (p.getIndex() != null ? p.getIndex().toUpperCase() : "");
            String nameKey = p.getName() != null ? p.getName().trim().toLowerCase() : "";

            // Exclude already solved problems
            if (solvedProblemKeys.contains(contestKey) || solvedProblemKeys.contains(nameKey)) {
                continue;
            }

            // Apply difficulty tier filter
            Integer rating = p.getRating();
            if ("ALL".equals(difficultyTier)) {
                unsolved.add(p);
            } else if ("800_900".equals(difficultyTier)) {
                if (rating != null && rating >= 800 && rating <= 900) unsolved.add(p);
            } else if ("1000_1100".equals(difficultyTier)) {
                if (rating != null && rating >= 1000 && rating <= 1100) unsolved.add(p);
            } else if ("1200_1300".equals(difficultyTier)) {
                if (rating != null && rating >= 1200 && rating <= 1300) unsolved.add(p);
            } else if ("1400_1500".equals(difficultyTier)) {
                if (rating != null && rating >= 1400 && rating <= 1500) unsolved.add(p);
            } else if ("1600_1700".equals(difficultyTier)) {
                if (rating != null && rating >= 1600 && rating <= 1700) unsolved.add(p);
            } else if ("1800_1900".equals(difficultyTier)) {
                if (rating != null && rating >= 1800 && rating <= 1900) unsolved.add(p);
            } else if ("2000_2100".equals(difficultyTier)) {
                if (rating != null && rating >= 2000 && rating <= 2100) unsolved.add(p);
            } else if ("2200_2300".equals(difficultyTier)) {
                if (rating != null && rating >= 2200 && rating <= 2300) unsolved.add(p);
            } else if ("2400_PLUS".equals(difficultyTier)) {
                if (rating != null && rating >= 2400) unsolved.add(p);
            }
        }

        // Sort: lowest rating first (or rating non-null first)
        Collections.sort(unsolved, (a, b) -> {
            int rA = a.getRating() != null ? a.getRating() : 0;
            int rB = b.getRating() != null ? b.getRating() : 0;
            return Integer.compare(rA, rB);
        });

        problemAdapter.setProblems(unsolved);
        binding.tvProblemCount.setText(unsolved.size() + " unsolved");
        binding.tvEmpty.setVisibility(unsolved.isEmpty() ? View.VISIBLE : View.GONE);
        if (unsolved.isEmpty()) {
            binding.tvEmpty.setText("No unsolved problems found for " + selectedTag + " in this difficulty range!");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
