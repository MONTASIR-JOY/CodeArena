package com.kuet.codearena.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kuet.codearena.R;
import com.kuet.codearena.data.db.entity.ProblemEntity;
import com.kuet.codearena.databinding.ItemLadderStepBinding;
import com.kuet.codearena.util.RatingColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter presenting progressive problem rungs in a ladder format with locked and unlocked states.
 */
public class LadderStepAdapter extends RecyclerView.Adapter<LadderStepAdapter.ViewHolder> {

    public interface OnLadderStepClickListener {
        void onStepClick(ProblemEntity problem, boolean isUnlocked);
    }

    private final List<ProblemEntity> problems = new ArrayList<>();
    private final OnLadderStepClickListener listener;
    private int unlockedUpToStep = 1; // Step 1 is unlocked by default

    public LadderStepAdapter(OnLadderStepClickListener listener) {
        this.listener = listener;
    }

    public void setProblems(List<ProblemEntity> list, int unlockedStep) {
        this.problems.clear();
        if (list != null) {
            this.problems.addAll(list);
        }
        this.unlockedUpToStep = unlockedStep;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLadderStepBinding binding = ItemLadderStepBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(problems.get(position), position + 1);
    }

    @Override
    public int getItemCount() {
        return problems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLadderStepBinding binding;

        ViewHolder(ItemLadderStepBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ProblemEntity problem, int stepNumber) {
            binding.tvStepNumber.setText(String.valueOf(stepNumber));
            binding.tvProblemTitle.setText(problem.getContestId() + problem.getProblemIndex() + ". " + problem.getName());

            int rating = problem.getRating() != null && problem.getRating() > 0 ? problem.getRating() : 800;
            binding.tvProblemRatingAndSolved.setText(String.format(Locale.getDefault(),
                    "Rating: %d • %,d solves", rating, problem.getSolvedCount()));
            binding.tvProblemRatingAndSolved.setTextColor(RatingColorUtil.getRatingColor(itemView.getContext(), rating));

            boolean isUnlocked = stepNumber <= unlockedUpToStep;
            if (isUnlocked) {
                binding.cardLadderStep.setAlpha(1.0f);
                binding.cardLadderStep.setClickable(true);
                binding.ivStepStatus.setImageResource(R.drawable.ic_play);
                binding.ivStepStatus.setColorFilter(itemView.getContext().getColor(R.color.cf_blue));
            } else {
                binding.cardLadderStep.setAlpha(0.45f);
                binding.cardLadderStep.setClickable(false);
                binding.ivStepStatus.setImageResource(R.drawable.ic_clock);
                binding.ivStepStatus.setColorFilter(itemView.getContext().getColor(R.color.cf_gray));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStepClick(problem, isUnlocked);
                }
            });
        }
    }
}

