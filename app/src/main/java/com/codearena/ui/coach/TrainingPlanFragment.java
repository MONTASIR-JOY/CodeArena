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

public class TrainingPlanFragment extends Fragment {

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

        binding.tvCoachTitle.setText("7-Day Training Plan");
        binding.tvCoachSubtitle.setText("AI-tailored schedule designed to boost your rating step-by-step.");
        binding.btnGenerate.setText("Generate 7-Day Plan");

        binding.btnGenerate.setOnClickListener(v -> {
            String handle = prefsRepo.getHandle();
            binding.progressCoach.setVisibility(View.VISIBLE);
            binding.tvCoachError.setVisibility(View.GONE);
            binding.btnGenerate.setEnabled(false);

            String prompt = "Create a detailed 7-Day Competitive Programming training syllabus for handle '" + handle + "'. Day 1 to Day 7: Specific problem tags, practice routines, mock contests, and review sessions.";
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

