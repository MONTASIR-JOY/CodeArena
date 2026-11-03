package com.kuet.codearena.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kuet.codearena.R;
import com.kuet.codearena.databinding.ItemTagStatBinding;
import com.kuet.codearena.ui.model.TagMasteryModel;
import com.kuet.codearena.util.RatingColorUtil;

import java.util.Locale;

/**
 * RecyclerView adapter rendering competitive programming tag metrics including solve count, accuracy, and tier.
 */
public class TagStatAdapter extends ListAdapter<TagMasteryModel, TagStatAdapter.ViewHolder> {

    public interface OnTagClickListener {
        void onTagClick(TagMasteryModel tagModel);
    }

    private final OnTagClickListener listener;

    public TagStatAdapter(OnTagClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<TagMasteryModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<TagMasteryModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull TagMasteryModel oldItem, @NonNull TagMasteryModel newItem) {
            return oldItem.getTag().equals(newItem.getTag());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TagMasteryModel oldItem, @NonNull TagMasteryModel newItem) {
            return oldItem.getSolvedCount() == newItem.getSolvedCount()
                    && oldItem.getAverageRating() == newItem.getAverageRating()
                    && oldItem.getLevel().equals(newItem.getLevel());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTagStatBinding binding = ItemTagStatBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTagStatBinding binding;

        ViewHolder(ItemTagStatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TagMasteryModel item) {
            binding.tvTagName.setText(item.getTag());
            binding.tvTagLevel.setText(item.getLevel());

            int levelBgColor;
            if ("Advanced".equalsIgnoreCase(item.getLevel())) {
                levelBgColor = itemView.getContext().getColor(R.color.accent_secondary);
            } else if ("Intermediate".equalsIgnoreCase(item.getLevel())) {
                levelBgColor = itemView.getContext().getColor(R.color.cf_blue);
            } else {
                levelBgColor = itemView.getContext().getColor(R.color.cf_gray);
            }
            binding.tvTagLevel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(levelBgColor));

            binding.tvTagSolvedCount.setText(String.format(Locale.getDefault(), "Solved: %d", item.getSolvedCount()));
            binding.tvTagAvgRating.setText(String.format(Locale.getDefault(), "Avg: %d", item.getAverageRating()));
            binding.tvTagAvgRating.setTextColor(RatingColorUtil.getRatingColor(itemView.getContext(), item.getAverageRating()));

            int accInt = (int) Math.round(item.getAccuracy());
            binding.tvTagAccuracy.setText(String.format(Locale.getDefault(), "Accuracy: %d%%", accInt));
            binding.progressTagAccuracy.setProgress(accInt);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTagClick(item);
            });
        }
    }
}

