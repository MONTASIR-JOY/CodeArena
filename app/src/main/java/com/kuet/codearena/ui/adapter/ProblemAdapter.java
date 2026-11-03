package com.kuet.codearena.ui.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kuet.codearena.data.db.entity.ProblemEntity;
import com.kuet.codearena.databinding.ItemProblemBinding;
import com.kuet.codearena.util.RatingColorUtil;

/**
 * RecyclerView ListAdapter presenting contest problems with rating chips and problem index badges.
 */
public class ProblemAdapter extends ListAdapter<ProblemEntity, ProblemAdapter.ProblemViewHolder> {

    public interface OnProblemClickListener {
        void onProblemClick(ProblemEntity problem);
    }

    private final OnProblemClickListener listener;

    private static final DiffUtil.ItemCallback<ProblemEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ProblemEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull ProblemEntity oldItem, @NonNull ProblemEntity newItem) {
                    return oldItem.getContestId() == newItem.getContestId() &&
                            oldItem.getProblemIndex().equals(newItem.getProblemIndex());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ProblemEntity oldItem, @NonNull ProblemEntity newItem) {
                    return oldItem.getName().equals(newItem.getName()) &&
                            ((oldItem.getRating() == null && newItem.getRating() == null) ||
                                    (oldItem.getRating() != null && oldItem.getRating().equals(newItem.getRating())));
                }
            };

    public ProblemAdapter(OnProblemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProblemBinding binding = ItemProblemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ProblemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ProblemViewHolder extends RecyclerView.ViewHolder {
        private final ItemProblemBinding binding;

        public ProblemViewHolder(ItemProblemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ProblemEntity problem) {
            binding.tvProblemIndex.setText(problem.getProblemIndex());
            binding.tvProblemName.setText(problem.getName());

            if (problem.getTags() != null && !problem.getTags().trim().isEmpty()) {
                binding.tvProblemTags.setText(problem.getTags());
                binding.tvProblemTags.setVisibility(View.VISIBLE);
            } else {
                binding.tvProblemTags.setVisibility(View.GONE);
            }

            if (problem.getRating() != null && problem.getRating() > 0) {
                binding.tvProblemRating.setText(String.valueOf(problem.getRating()));
                binding.tvProblemRating.setVisibility(View.VISIBLE);
                int color = RatingColorUtil.getRatingColor(problem.getRating());
                binding.tvProblemRating.setBackgroundTintList(ColorStateList.valueOf(color));
            } else {
                binding.tvProblemRating.setText("Unrated");
                binding.tvProblemRating.setVisibility(View.VISIBLE);
                binding.tvProblemRating.setBackgroundTintList(ColorStateList.valueOf(RatingColorUtil.getRatingColor(null)));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onProblemClick(problem);
            });
        }
    }
}
