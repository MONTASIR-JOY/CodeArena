package com.codearena.ui.recommender;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.codearena.data.model.CfProblem;
import com.codearena.data.model.CfProblemsetResult;
import com.codearena.data.repository.CodeforcesRepository;
import com.codearena.databinding.FragmentRecommenderBinding;
import com.codearena.ui.adapter.ProblemAdapter;
import java.util.ArrayList;
import java.util.List;

public class RecommenderFragment extends Fragment {

    private FragmentRecommenderBinding binding;
    private CodeforcesRepository cfRepo;
    private ProblemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecommenderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cfRepo = new CodeforcesRepository(requireContext());

        binding.tvRecommenderFocus.setText("Problems hand-picked near your rating target to maximize learning.");

        adapter = new ProblemAdapter();
        binding.rvRecommended.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecommended.setAdapter(adapter);

        binding.btnRecommenderRefresh.setOnClickListener(v -> loadRecommendations());

        loadRecommendations();
    }

    private void loadRecommendations() {
        binding.progressRecommender.setVisibility(View.VISIBLE);
        binding.tvRecommenderError.setVisibility(View.GONE);

        cfRepo.fetchProblemset(new CodeforcesRepository.Callback<CfProblemsetResult>() {
            @Override
            public void onSuccess(CfProblemsetResult result) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    binding.progressRecommender.setVisibility(View.GONE);
                    if (result != null && result.getProblems() != null) {
                        List<CfProblem> list = new ArrayList<>();
                        for (CfProblem p : result.getProblems()) {
                            if (p.getRating() != null && p.getRating() >= 1200 && p.getRating() <= 1800) {
                                list.add(p);
                                if (list.size() >= 20) break;
                            }
                        }
                        adapter.setProblems(list);
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    binding.progressRecommender.setVisibility(View.GONE);
                    binding.tvRecommenderError.setVisibility(View.VISIBLE);
                    binding.tvRecommenderError.setText("Error loading recommendations: " + error.getMessage());
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

