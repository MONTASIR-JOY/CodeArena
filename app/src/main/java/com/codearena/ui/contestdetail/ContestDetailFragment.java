package com.codearena.ui.contestdetail;

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
import com.codearena.data.model.CfStandingsResult;
import com.codearena.data.repository.CodeforcesRepository;
import com.codearena.databinding.FragmentContestDetailBinding;
import com.codearena.ui.adapter.ProblemAdapter;
import com.codearena.util.TimeUtils;

public class ContestDetailFragment extends Fragment {

    private FragmentContestDetailBinding binding;
    private CodeforcesRepository cfRepo;
    private ProblemAdapter adapter;
    private long contestId = 0;
    private String contestName = "";
    private long durationSeconds = 7200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentContestDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cfRepo = new CodeforcesRepository(requireContext());

        if (getArguments() != null) {
            contestId = getArguments().getLong("contest_id", 0);
            contestName = getArguments().getString("contest_name", "Contest #" + contestId);
            durationSeconds = getArguments().getLong("duration_seconds", 7200);
        }

        binding.toolbar.setTitle(contestName);
        binding.toolbar.setNavigationOnClickListener(v -> {
            androidx.navigation.NavController nc = Navigation.findNavController(v);
            if (!nc.popBackStack()) {
                nc.navigate(R.id.nav_home);
            }
        });

        binding.tvContestName.setText(contestName);
        binding.tvContestId.setText("#" + contestId);
        binding.chipDuration.setText(TimeUtils.formatDurationHours(durationSeconds));

        adapter = new ProblemAdapter();
        binding.rvProblems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvProblems.setAdapter(adapter);

        binding.fabStartContest.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong("contest_id", contestId);
            args.putString("contest_name", contestName);
            args.putLong("duration_seconds", durationSeconds);
            Navigation.findNavController(v).navigate(R.id.liveContestFragment, args);
        });

        binding.btnRetry.setOnClickListener(v -> loadProblems());

        loadProblems();
    }

    private void loadProblems() {
        if (contestId <= 0) return;
        binding.progressProblems.setVisibility(View.VISIBLE);
        binding.cardError.setVisibility(View.GONE);

        cfRepo.fetchContestStandings(contestId, new CodeforcesRepository.Callback<CfStandingsResult>() {
            @Override
            public void onSuccess(CfStandingsResult result) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    binding.progressProblems.setVisibility(View.GONE);
                    if (result != null && result.getProblems() != null) {
                        binding.tvProblemCount.setText(String.valueOf(result.getProblems().size()));
                        binding.tvProblemsHeader.setVisibility(View.VISIBLE);
                        binding.fabStartContest.setVisibility(View.VISIBLE);
                        adapter.setProblems(result.getProblems());
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    binding.progressProblems.setVisibility(View.GONE);
                    binding.cardError.setVisibility(View.VISIBLE);
                    binding.tvErrorMessage.setText("Could not load problems: " + error.getMessage());
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

