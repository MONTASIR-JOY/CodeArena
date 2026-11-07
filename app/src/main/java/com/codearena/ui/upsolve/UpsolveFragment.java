package com.codearena.ui.upsolve;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.codearena.R;
import com.codearena.data.db.AppDatabase;
import com.codearena.data.db.entity.UpsolveItemEntity;
import com.codearena.data.model.CfSubmission;
import com.codearena.data.repository.CodeforcesRepository;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentUpsolveBinding;
import com.codearena.ui.adapter.UpsolveAdapter;
import com.codearena.util.SpacedRepetition;
import java.util.List;

public class UpsolveFragment extends Fragment {

    private FragmentUpsolveBinding binding;
    private AppDatabase db;
    private UserPreferencesRepository prefsRepo;
    private UpsolveAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUpsolveBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        prefsRepo = new UserPreferencesRepository(requireContext());

        setupRecyclerView();
        setupButtons();
        observeUpsolves();
        syncSubmissionsWithCodeforces();
    }

    private void syncSubmissionsWithCodeforces() {
        String handle = prefsRepo.getHandle();
        if (handle == null || handle.isEmpty()) return;

        CodeforcesRepository cfRepo = new CodeforcesRepository(requireContext());
        cfRepo.fetchUserSubmissions(handle, 50, new CodeforcesRepository.Callback<List<CfSubmission>>() {
            @Override
            public void onSuccess(List<CfSubmission> submissions) {
                if (submissions == null || !isAdded()) return;
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    for (CfSubmission sub : submissions) {
                        if ("OK".equals(sub.getVerdict())) {
                            if (sub.getContestId() != null && sub.getProblem() != null && sub.getProblem().getIndex() != null) {
                                long contestId = sub.getContestId();
                                String index = sub.getProblem().getIndex();
                                long solvedTime = sub.getCreationTimeSeconds() * 1000L;
                                UpsolveItemEntity item = db.upsolveDao().getUpsolveItem(contestId, index);
                                if (item != null && !item.isSolved()) {
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

    private void setupRecyclerView() {
        adapter = new UpsolveAdapter(new UpsolveAdapter.OnUpsolveActionListener() {
            @Override
            public void onSolved(UpsolveItemEntity item) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    db.upsolveDao().markAsSolved(item.getContestId(), item.getProblemIndex(), System.currentTimeMillis());
                });
            }

            @Override
            public void onStillStuck(UpsolveItemEntity item) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    int nextIdx = Math.min(4, item.getIntervalIndex() + 1);
                    boolean isFast = prefsRepo.isFastReviewDebugMode();
                    long nextReview = SpacedRepetition.calculateNextReviewEpochMillis(nextIdx, isFast);

                    item.setIntervalIndex(nextIdx);
                    item.setNextReviewEpochMillis(nextReview);
                    item.setReviewCount(item.getReviewCount() + 1);
                    db.upsolveDao().updateUpsolveItem(item);
                });
            }
        });

        binding.rvUpsolve.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUpsolve.setAdapter(adapter);
    }

    private void setupButtons() {
        binding.btnHistory.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.historyFragment));


    }

    private void observeUpsolves() {
        db.upsolveDao().getAllActiveUpsolvesLiveData().observe(getViewLifecycleOwner(), list -> {
            int count = list != null ? list.size() : 0;
            binding.tvUpsolveSummary.setText(count + " problems queued for spaced review");
            adapter.setItems(list);
            binding.tvUpsolveEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

