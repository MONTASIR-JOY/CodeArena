package com.kuet.codearena.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kuet.codearena.data.db.entity.RunEntity;
import com.kuet.codearena.databinding.ItemRunHistoryBinding;

import java.util.Locale;

/**
 * RecyclerView ListAdapter presenting historical virtual contest attempts with explicit intent reopening.
 */
public class RunHistoryAdapter extends ListAdapter<RunEntity, RunHistoryAdapter.HistoryViewHolder> {

    public interface OnHistoryClickListener {
        void onHistoryClick(RunEntity run);
    }

    private final OnHistoryClickListener listener;

    private static final DiffUtil.ItemCallback<RunEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RunEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull RunEntity oldItem, @NonNull RunEntity newItem) {
                    return oldItem.getRunId() == newItem.getRunId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull RunEntity oldItem, @NonNull RunEntity newItem) {
                    return oldItem.getScore() == newItem.getScore() &&
                            oldItem.getSolvedCount() == newItem.getSolvedCount() &&
                            oldItem.getTotalPenaltyMinutes() == newItem.getTotalPenaltyMinutes();
                }
            };

    public RunHistoryAdapter(OnHistoryClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRunHistoryBinding binding = ItemRunHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new HistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemRunHistoryBinding binding;

        public HistoryViewHolder(ItemRunHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RunEntity run) {
            binding.tvHistoryContestName.setText(run.getContestName());
            String subtitle = String.format(Locale.getDefault(),
                    "%d solved • %dm penalty • %d wrong",
                    run.getSolvedCount(),
                    run.getTotalPenaltyMinutes(),
                    run.getTotalWrongAttempts()
            );
            binding.tvHistorySubtitle.setText(subtitle);
            binding.tvHistoryScore.setText(String.format(Locale.getDefault(), "%.0f pts", run.getScore()));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onHistoryClick(run);
            });
        }
    }
}
