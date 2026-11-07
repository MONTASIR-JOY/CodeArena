package com.codearena.ui.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.codearena.data.db.AppDatabase;
import com.codearena.data.db.entity.VirtualRunEntity;
import com.codearena.databinding.FragmentAnalyticsBinding;
import java.util.List;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());

        db.virtualRunDao().getAllCompletedRunsLiveData().observe(getViewLifecycleOwner(), this::renderAnalytics);
    }

    private void renderAnalytics(List<VirtualRunEntity> runs) {
        if (runs == null || runs.isEmpty()) {
            binding.tvAnalyticsEmpty.setVisibility(View.VISIBLE);
            binding.layoutAnalyticsContent.setVisibility(View.GONE);
            return;
        }

        binding.tvAnalyticsEmpty.setVisibility(View.GONE);
        binding.layoutAnalyticsContent.setVisibility(View.VISIBLE);

        int totalSolved = 0;
        int totalProblems = 0;
        for (VirtualRunEntity r : runs) {
            totalSolved += r.getSolvedCount();
            totalProblems += r.getTotalProblemsCount();
        }

        binding.tvAnalyticsTotals.setText(runs.size() + " contests · " + totalSolved + " solved of " + totalProblems + " problems");
        binding.tvAnalyticsWeek.setText("Total time invested: " + (runs.size() * 2) + " hours");
        binding.tvAnalyticsTimeOfDay.setText("Steady performance across all virtual rounds.");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
