package com.codearena.ui.coach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.codearena.data.api.GeminiClient;
import com.codearena.databinding.FragmentCoachBinding;

public class CoachHintFragment extends Fragment {

    private FragmentCoachBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCoachBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String problemName = "Problem";
        if (getArguments() != null) {
            problemName = getArguments().getString("problem_name", "this problem");
        }

        binding.tvCoachTitle.setText("AI Progressive Hints");
        binding.tvCoachSubtitle.setText("Socratic hints without spoiling the full solution.");
        binding.btnGenerate.setText("Get Progressive Hint");

        final String finalProblem = problemName;
        binding.btnGenerate.setOnClickListener(v -> {
            binding.progressCoach.setVisibility(View.VISIBLE);
            binding.tvCoachError.setVisibility(View.GONE);
            binding.btnGenerate.setEnabled(false);

            String prompt = "Give a 3-tier progressive hint (Hint 1: Conceptual idea, Hint 2: Data structure/algorithm hint, Hint 3: Key invariant) for: " + finalProblem + ". Do not give complete code.";
            GeminiClient.generateAsync(prompt, new GeminiClient.Callback() {
                @Override
                public void onSuccess(String responseText) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        binding.progressCoach.setVisibility(View.GONE);
                        binding.btnGenerate.setEnabled(true);
                        binding.tvCoachResult.setText(responseText);
                    });
                }

                @Override
                public void onError(Exception error) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        binding.progressCoach.setVisibility(View.GONE);
                        binding.btnGenerate.setEnabled(true);
                        binding.tvCoachError.setVisibility(View.VISIBLE);
                        binding.tvCoachError.setText(error.getMessage());
                    });
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

