package com.codearena.ui.history;

import android.content.Intent;
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
import com.codearena.data.db.entity.VirtualRunEntity;
import com.codearena.databinding.FragmentHistoryBinding;
import com.codearena.ui.adapter.HistoryAdapter;
import com.codearena.util.CsvExporter;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private AppDatabase db;
    private HistoryAdapter adapter;
    private List<VirtualRunEntity> cachedRuns = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());

        adapter = new HistoryAdapter(run -> {
            Bundle args = new Bundle();
            args.putLong("contest_id", run.getContestId());
            args.putString("contest_name", run.getContestName());
            args.putInt("solved_count", run.getSolvedCount());
            args.putInt("total_count", run.getTotalProblemsCount());
            Navigation.findNavController(requireView()).navigate(R.id.analysisFragment, args);
        });

        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapter);

        binding.btnExportCsv.setOnClickListener(v -> {
            String csv = CsvExporter.runsToCsv(cachedRuns);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, csv);
            sendIntent.setType("text/csv");
            startActivity(Intent.createChooser(sendIntent, "Export Contest History (CSV)"));
        });

        db.virtualRunDao().getAllCompletedRunsLiveData().observe(getViewLifecycleOwner(), runs -> {
            cachedRuns = runs != null ? runs : new ArrayList<>();
            adapter.setRuns(runs);
            binding.tvHistoryEmpty.setVisibility(cachedRuns.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
