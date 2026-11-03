package com.kuet.codearena.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kuet.codearena.data.db.entity.RunProblemEntity;
import com.kuet.codearena.databinding.ItemRunProblemBinding;

import java.util.Locale;

/**
 * RecyclerView ListAdapter displaying problem progress, submission verdicts, and penalties in live virtual runs.
 */
public class RunProblemAdapter extends ListAdapter<RunProblemEntity, RunProblemAdapter.RunProblemViewHolder> {

    public interface OnRunProblemClickListener {
        void onProblemClick(RunProblemEntity problem);
    }

    private final OnRunProblemClickListener listener;

    private static final DiffUtil.ItemCallback<RunProblemEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RunProblemEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull RunProblemEntity oldItem, @NonNull RunProblemEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull RunProblemEntity oldItem, @NonNull RunProblemEntity newItem) {
                    return oldItem.isSolved() == newItem.isSolved() &&
                            oldItem.getWrongAttempts() == newItem.getWrongAttempts() &&
                            oldItem.getSolveTimeSeconds() == newItem.getSolveTimeSeconds();
                }
            };

    public RunProblemAdapter(OnRunProblemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public RunProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRunProblemBinding binding = ItemRunProblemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new RunProblemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RunProblemViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class RunProblemViewHolder extends RecyclerView.ViewHolder {
        private final ItemRunProblemBinding binding;

        public RunProblemViewHolder(ItemRunProblemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RunProblemEntity problem) {
            binding.tvRunProblemIndex.setText(problem.getProblemIndex());
            binding.tvRunProblemName.setText(problem.getProblemName());

            if (problem.getRating() != null && problem.getRating() > 0) {
                binding.tvRunProblemRating.setText("Rating: " + problem.getRating());
            } else {
                binding.tvRunProblemRating.setText("Unrated");
            }

            if (problem.isSolved()) {
                long minutes = problem.getSolveTimeSeconds() / 60;
                long seconds = problem.getSolveTimeSeconds() % 60;
                String acText;
                if (problem.getWrongAttempts() > 0) {
                    acText = String.format(Locale.getDefault(), "AC (+%d) %02d:%02d", problem.getWrongAttempts(), minutes, seconds);
                } else {
                    acText = String.format(Locale.getDefault(), "AC %02d:%02d", minutes, seconds);
                }
                binding.tvVerdictBadge.setText(acText);
                binding.tvVerdictBadge.setTextColor(Color.WHITE);
                binding.tvVerdictBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#10B981"))); // Green
            } else if (problem.getWrongAttempts() > 0) {
                binding.tvVerdictBadge.setText("-" + problem.getWrongAttempts());
                binding.tvVerdictBadge.setTextColor(Color.WHITE);
                binding.tvVerdictBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EF4444"))); // Red
            } else {
                binding.tvVerdictBadge.setText("Pending");
                binding.tvVerdictBadge.setTextColor(Color.parseColor("#64748B"));
                binding.tvVerdictBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E2E8F0"))); // Gray
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onProblemClick(problem);
            });
        }
    }
}
