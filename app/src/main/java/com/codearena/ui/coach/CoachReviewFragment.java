package com.codearena.ui.coach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.codearena.data.api.GeminiClient;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentCoachBinding;

public class CoachReviewFragment extends Fragment {

    private FragmentCoachBinding binding;
    private UserPreferencesRepository prefsRepo;

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
        prefsRepo = new UserPreferencesRepository(requireContext());

        binding.tvCoachTitle.setText("AI Coach Review");
        binding.tvCoachSubtitle.setText("Personalized feedback and strategic advice on your competitive programming journey.");
        binding.btnGenerate.setText("Generate Coach Review");

        binding.btnGenerate.setOnClickListener(v -> generateReview());
    }

    private void generateReview() {
        String handle = prefsRepo.getHandle();
        String prompt = "You are a World Finals Competitive Programming Coach. The student's Codeforces handle is '"
                + (handle.isEmpty() ? "an ambitious programmer" : handle)
                + "'. Provide a structured, encouraging critique: 1) What skills to prioritize next, 2) Time-management tactics for 2-hour rounds, 3) Recommended algorithm topics.";

        binding.progressCoach.setVisibility(View.VISIBLE);
        binding.tvCoachError.setVisibility(View.GONE);
        binding.btnGenerate.setEnabled(false);

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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

