package com.kuet.codearena.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kuet.codearena.data.db.entity.RunProblemEntity;
import com.kuet.codearena.databinding.ItemPostMortemProblemBinding;

import java.util.Locale;

/**
 * RecyclerView ListAdapter displaying per-problem outcome breakdown and earned points on the post-mortem screen.
 */
public class PostMortemAdapter extends ListAdapter<RunProblemEntity, PostMortemAdapter.PostMortemViewHolder> {

    public interface OnProblemClickListener {
        void onProblemClick(RunProblemEntity problem);
    }

    private final long contestMinutes;
    private final OnProblemClickListener listener;

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

    public PostMortemAdapter(long contestMinutes, OnProblemClickListener listener) {
        super(DIFF_CALLBACK);
        this.contestMinutes = contestMinutes > 0 ? contestMinutes : 120;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostMortemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostMortemProblemBinding binding = ItemPostMortemProblemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new PostMortemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostMortemViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class PostMortemViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostMortemProblemBinding binding;

        public PostMortemViewHolder(ItemPostMortemProblemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RunProblemEntity problem) {
            binding.tvPmIndex.setText(problem.getProblemIndex());
            binding.tvPmName.setText(problem.getProblemName());

            if (problem.isSolved()) {
                long minutes = problem.getSolveTimeSeconds() / 60;
                long seconds = problem.getSolveTimeSeconds() % 60;
                String statusStr = String.format(Locale.getDefault(), "Solved at %02d:%02d", minutes, seconds);
                if (problem.getWrongAttempts() > 0) {
                    statusStr += String.format(Locale.getDefault(), " • %d wrong attempt(s)", problem.getWrongAttempts());
                }
                binding.tvPmStatus.setText(statusStr);
                binding.tvPmStatus.setTextColor(Color.parseColor("#10B981"));

                int rating = problem.getRating() != null && problem.getRating() > 0 ? problem.getRating() : 800;
                double solveMins = Math.max(1, problem.getSolveTimeSeconds() / 60.0);
                double timeFactor = Math.max(0.3, 1.0 - (solveMins / (double) contestMinutes));
                double problemScore = rating * timeFactor;
                binding.tvPmPoints.setText(String.format(Locale.getDefault(), "+%.0f pts", problemScore));
                binding.tvPmPoints.setTextColor(Color.parseColor("#10B981"));
            } else {
                if (problem.getWrongAttempts() > 0) {
                    binding.tvPmStatus.setText(problem.getWrongAttempts() + " wrong attempt(s) • Unsolved");
                    binding.tvPmStatus.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    binding.tvPmStatus.setText("Unattempted • Added to Upsolve Queue");
                    binding.tvPmStatus.setTextColor(Color.parseColor("#64748B"));
                }
                binding.tvPmPoints.setText("0 pts");
                binding.tvPmPoints.setTextColor(Color.parseColor("#64748B"));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onProblemClick(problem);
            });
        }
    }
}
