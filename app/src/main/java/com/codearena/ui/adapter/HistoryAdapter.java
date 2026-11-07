package com.codearena.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.data.db.entity.VirtualRunEntity;
import com.codearena.databinding.ItemHistoryRunBinding;
import com.codearena.util.RankColors;
import com.codearena.util.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnRunClickListener {
        void onRunClick(VirtualRunEntity run);
    }

    private final List<VirtualRunEntity> runs = new ArrayList<>();
    private final OnRunClickListener listener;

    public HistoryAdapter(OnRunClickListener listener) {
        this.listener = listener;
    }

    public void setRuns(List<VirtualRunEntity> newRuns) {
        runs.clear();
        if (newRuns != null) {
            runs.addAll(newRuns);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryRunBinding binding = ItemHistoryRunBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(runs.get(position));
    }

    @Override
    public int getItemCount() {
        return runs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryRunBinding binding;

        ViewHolder(ItemHistoryRunBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VirtualRunEntity run) {
            binding.tvRunContest.setText(run.getContestName());
            binding.tvRunDate.setText(TimeUtils.formatEpochMillis(run.getEndTimeEpochMillis()) + " · " + run.getMode());
            binding.tvRunSolved.setText("Solved " + run.getSolvedCount() + " / " + run.getTotalProblemsCount());
            binding.tvRunPenalty.setText("Penalty " + run.getPenaltyMinutes() + "m");
            binding.tvRunPerformance.setText("Perf " + run.getEstimatedPerformance());
            binding.tvRunPerformance.setTextColor(RankColors.getColorForRating(run.getEstimatedPerformance()));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onRunClick(run);
            });
        }
    }
}

