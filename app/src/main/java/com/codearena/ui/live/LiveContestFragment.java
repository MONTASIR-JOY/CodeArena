package com.codearena.ui.live;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.codearena.data.db.entity.VirtualRunEntity;
import com.codearena.data.model.CfProblem;
import com.codearena.data.model.CfStandingsResult;
import com.codearena.data.repository.CodeforcesRepository;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentLiveContestBinding;
import com.codearena.ui.adapter.LiveProblemAdapter;
import com.codearena.util.SpacedRepetition;
import java.util.List;

public class LiveContestFragment extends Fragment {

    private FragmentLiveContestBinding binding;
    private CodeforcesRepository cfRepo;
    private AppDatabase db;
    private LiveProblemAdapter adapter;

    private long contestId = 0;
    private String contestName = "";
    private long totalSeconds = 7200;
    private long remainingSeconds = 7200;
    private boolean isPaused = false;
    private long startTimeEpochMillis;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPaused && remainingSeconds > 0) {
                remainingSeconds--;
                binding.circularTimer.setTime(remainingSeconds, totalSeconds);
            }
            if (remainingSeconds > 0) {
                timerHandler.postDelayed(this, 1000);
            } else {
                finishContest();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLiveContestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cfRepo = new CodeforcesRepository(requireContext());
        db = AppDatabase.getInstance(requireContext());

        if (getArguments() != null) {
            contestId = getArguments().getLong("contest_id", 0);
            contestName = getArguments().getString("contest_name", "Virtual Contest #" + contestId);
            totalSeconds = getArguments().getLong("duration_seconds", 7200);
            remainingSeconds = totalSeconds;
        }

        startTimeEpochMillis = System.currentTimeMillis();
        binding.tvLiveContestName.setText(contestName);
        binding.circularTimer.setTime(remainingSeconds, totalSeconds);

        adapter = new LiveProblemAdapter(problem -> {
            // User focused on problem
        });
        binding.rvLiveProblems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLiveProblems.setAdapter(adapter);

        binding.btnPauseResume.setOnClickListener(v -> togglePause());
        binding.btnFinish.setOnClickListener(v -> finishContest());

        loadProblems();
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void togglePause() {
        isPaused = !isPaused;
        binding.circularTimer.setPaused(isPaused);
        binding.btnPauseResume.setText(isPaused ? "Resume" : "Pause");
    }

    private void finishContest() {
        timerHandler.removeCallbacks(timerRunnable);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int solved = adapter != null ? adapter.getSolvedCount() : 0;
            int total = adapter != null ? adapter.getItemCount() : 0;
            VirtualRunEntity run = new VirtualRunEntity(
                    contestId,
                    contestName,
                    startTimeEpochMillis,
                    System.currentTimeMillis(),
                    totalSeconds - remainingSeconds,
                    solved,
                    total,
                    0,
                    1500,
                    "Standard",
                    true
            );
            db.virtualRunDao().insertRun(run);

            if (adapter != null && getContext() != null) {
                List<CfProblem> unsolved = adapter.getUnsolvedProblems();
                UserPreferencesRepository prefs = new UserPreferencesRepository(requireContext());
                long now = System.currentTimeMillis();
                long nextReview = SpacedRepetition.calculateNextReviewEpochMillis(0, prefs.isFastReviewDebugMode());
                for (CfProblem p : unsolved) {
                    UpsolveItemEntity item = new UpsolveItemEntity(
                            p.getContestId(),
                            p.getIndex(),
                            p.getName(),
                            p.getRating(),
                            p.getTags(),
                            now,
                            0,
                            nextReview,
                            0,
                            false
                    );
                    db.upsolveDao().insertUpsolveItem(item);
                }
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    Bundle args = new Bundle();
                    args.putLong("contest_id", contestId);
                    args.putString("contest_name", contestName);
                    args.putInt("solved_count", solved);
                    args.putInt("total_count", total);
                    Navigation.findNavController(requireView()).navigate(R.id.analysisFragment, args);
                });
            }
        });
    }

    private void loadProblems() {
        if (contestId <= 0) return;
        cfRepo.fetchContestStandings(contestId, new CodeforcesRepository.Callback<CfStandingsResult>() {
            @Override
            public void onSuccess(CfStandingsResult result) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (result != null && result.getProblems() != null) {
                        adapter.setProblems(result.getProblems());
                        binding.tvSolvedSummary.setText("Solved 0 / " + result.getProblems().size());
                    }
                });
            }

            @Override
            public void onError(Exception error) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(timerRunnable);
        binding = null;
    }
}

