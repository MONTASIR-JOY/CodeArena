package com.codearena.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.R;
import com.codearena.data.model.CfProblem;
import com.codearena.data.model.DailyProblemItem;
import com.codearena.databinding.ItemDailyProblemBinding;
import com.codearena.util.RankColors;
import java.util.ArrayList;
import java.util.List;

public class DailyProblemAdapter extends RecyclerView.Adapter<DailyProblemAdapter.ViewHolder> {

    public interface OnProblemClickListener {
        void onProblemClick(DailyProblemItem item);
    }

    private final List<DailyProblemItem> items = new ArrayList<>();
    private OnProblemClickListener clickListener;

    public void setItems(List<DailyProblemItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<DailyProblemItem> getItems() {
        return items;
    }

    public void setOnProblemClickListener(OnProblemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDailyProblemBinding binding = ItemDailyProblemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), clickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemDailyProblemBinding binding;

        ViewHolder(ItemDailyProblemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DailyProblemItem item, OnProblemClickListener listener) {
            Context context = itemView.getContext();
            CfProblem problem = item.getProblem();
            if (problem == null) return;

            // Rating
            if (problem.getRating() != null && problem.getRating() > 0) {
                binding.tvDailyRating.setText(String.valueOf(problem.getRating()));
                int color = RankColors.getColorForRating(problem.getRating());
                binding.tvDailyRating.setTextColor(color);
            } else {
                binding.tvDailyRating.setText("---");
                binding.tvDailyRating.setTextColor(ContextCompat.getColor(context, R.color.text_muted_dark));
            }

            // Name
            String code = (problem.getContestId() != null ? problem.getContestId() : "") +
                    (problem.getIndex() != null ? problem.getIndex() : "");
            String title = (code.isEmpty() ? "" : code + " · ") +
                    (problem.getName() != null ? problem.getName() : "Problem");
            binding.tvDailyName.setText(title);

            // Tags
            if (problem.getTags() != null && !problem.getTags().isEmpty()) {
                binding.tvDailyTags.setText(String.join(", ", problem.getTags()));
                binding.tvDailyTags.setVisibility(View.VISIBLE);
            } else {
                binding.tvDailyTags.setVisibility(View.GONE);
            }

            // Solved check status
            if (item.isSolved()) {
                binding.ivDailyDone.setVisibility(View.VISIBLE);
                binding.ivDailyDone.setColorFilter(ContextCompat.getColor(context, R.color.verdict_ok));
                binding.ivDailyDone.setAlpha(1.0f);
            } else {
                binding.ivDailyDone.setVisibility(View.VISIBLE);
                binding.ivDailyDone.setColorFilter(ContextCompat.getColor(context, R.color.stroke_dark));
                binding.ivDailyDone.setAlpha(0.35f);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProblemClick(item);
                } else {
                    if (problem.getContestId() != null && problem.getIndex() != null) {
                        try {
                            String url = "https://codeforces.com/problemset/problem/" +
                                    problem.getContestId() + "/" + problem.getIndex();
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            context.startActivity(browserIntent);
                        } catch (Exception ignored) {}
                    }
                }
            });
        }
    }
}

