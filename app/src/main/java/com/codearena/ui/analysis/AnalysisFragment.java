package com.codearena.ui.analysis;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.codearena.R;
import com.codearena.databinding.FragmentAnalysisBinding;

public class AnalysisFragment extends Fragment {

    private FragmentAnalysisBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalysisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        long contestId = 0;
        String contestName = "Contest";
        int solved = 0;
        int total = 0;

        if (getArguments() != null) {
            contestId = getArguments().getLong("contest_id", 0);
            contestName = getArguments().getString("contest_name", "Contest #" + contestId);
            solved = getArguments().getInt("solved_count", 0);
            total = getArguments().getInt("total_count", 0);
        }

        binding.tvAnalysisTitle.setText("Virtual Run Summary");
        binding.tvAnalysisContest.setText(contestName);
        binding.tvAnalysisSolved.setText(solved + " / " + total);
        binding.tvAnalysisRank.setText("~" + (Math.max(1, 1000 - (solved * 180))));
        binding.tvAnalysisPerformance.setText(String.valueOf(1200 + (solved * 200)));

        binding.btnCoachReview.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.coachReviewFragment));

        binding.btnUpsolveQueue.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_upsolve));

        final String shareText = "I solved " + solved + "/" + total + " in virtual contest " + contestName + " on CodeArena!";
        binding.btnShare.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share Result"));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

