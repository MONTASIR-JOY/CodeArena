package com.kuet.codearena.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.databinding.ItemContestBinding;

import java.util.Locale;

/**
 * RecyclerView ListAdapter displaying Codeforces contests using DiffUtil for efficient updates.
 */
public class ContestAdapter extends ListAdapter<ContestEntity, ContestAdapter.ContestViewHolder> {

    public interface OnContestClickListener {
        void onContestClick(ContestEntity contest);
        void onStartVirtualClick(ContestEntity contest);
    }

    private final OnContestClickListener listener;

    private static final DiffUtil.ItemCallback<ContestEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ContestEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull ContestEntity oldItem, @NonNull ContestEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull ContestEntity oldItem, @NonNull ContestEntity newItem) {
                    return oldItem.getName().equals(newItem.getName()) &&
                            oldItem.getDurationSeconds() == newItem.getDurationSeconds() &&
                            oldItem.getDivision().equals(newItem.getDivision());
                }
            };

    public ContestAdapter(OnContestClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContestBinding binding = ItemContestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ContestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContestViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ContestViewHolder extends RecyclerView.ViewHolder {
        private final ItemContestBinding binding;

        public ContestViewHolder(ItemContestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ContestEntity contest) {
            binding.tvContestName.setText(contest.getName());
            binding.tvDivisionBadge.setText(contest.getDivision());

            long hours = contest.getDurationSeconds() / 3600;
            long minutes = (contest.getDurationSeconds() % 3600) / 60;
            String durationText;
            if (hours > 0 && minutes > 0) {
                durationText = String.format(Locale.getDefault(), "%d hr %d min", hours, minutes);
            } else if (hours > 0) {
                durationText = String.format(Locale.getDefault(), "%d hrs", hours);
            } else {
                durationText = String.format(Locale.getDefault(), "%d min", minutes);
            }
            binding.tvDuration.setText(durationText);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onContestClick(contest);
            });

            binding.btnStartVirtual.setOnClickListener(v -> {
                if (listener != null) listener.onStartVirtualClick(contest);
            });
        }
    }
}
