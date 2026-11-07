package com.codearena.ui.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.data.model.CfProblem;
import com.codearena.databinding.ItemProblemBinding;
import com.codearena.util.RankColors;
import java.util.ArrayList;
import java.util.List;

public class ProblemAdapter extends RecyclerView.Adapter<ProblemAdapter.ViewHolder> {

    public interface OnProblemClickListener {
        void onProblemClick(CfProblem problem);
        void onProblemLongClick(CfProblem problem);
    }

    private final List<CfProblem> problems = new ArrayList<>();
    private OnProblemClickListener clickListener;

    public ProblemAdapter() {}

    public ProblemAdapter(OnProblemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnProblemClickListener(OnProblemClickListener listener) {
        this.clickListener = listener;
    }

    public void setProblems(List<CfProblem> newProblems) {
        problems.clear();
        if (newProblems != null) {
            problems.addAll(newProblems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProblemBinding binding = ItemProblemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(problems.get(position), clickListener);
    }

    @Override
    public int getItemCount() {
        return problems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProblemBinding binding;

        ViewHolder(ItemProblemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CfProblem problem, OnProblemClickListener listener) {
            binding.tvProblemIndex.setText(problem.getIndex());
            binding.tvProblemName.setText(problem.getName());

            if (problem.getTags() != null && !problem.getTags().isEmpty()) {
                binding.tvProblemTags.setText(String.join(", ", problem.getTags()));
            } else {
                binding.tvProblemTags.setText("unclassified");
            }

            if (problem.getRating() != null && problem.getRating() > 0) {
                binding.tvProblemRating.setText("★ " + problem.getRating());
                binding.tvProblemRating.setTextColor(RankColors.getColorForRating(problem.getRating()));
            } else {
                binding.tvProblemRating.setText("★ ---");
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProblemClick(problem);
                } else {
                    String url = "https://codeforces.com/contest/" + problem.getContestId() + "/problem/" + problem.getIndex();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    v.getContext().startActivity(intent);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onProblemLongClick(problem);
                    return true;
                }
                return false;
            });
        }
    }
}

