package com.kuet.codearena.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.databinding.ItemSolvedTodayBinding;
import com.kuet.codearena.util.RatingColorUtil;

import java.util.Locale;

/**
 * RecyclerView adapter displaying individual solved problems completed during the current day.
 */
public class SolvedTodayAdapter extends ListAdapter<SolveEntity, SolvedTodayAdapter.ViewHolder> {

    public interface OnSolveClickListener {
        void onSolveClick(SolveEntity solve);
    }

    private final OnSolveClickListener listener;

    public SolvedTodayAdapter(OnSolveClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<SolveEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<SolveEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull SolveEntity oldItem, @NonNull SolveEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SolveEntity oldItem, @NonNull SolveEntity newItem) {
            return oldItem.getProblemId().equals(newItem.getProblemId())
                    && oldItem.getSolvedAt() == newItem.getSolvedAt();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSolvedTodayBinding binding = ItemSolvedTodayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSolvedTodayBinding binding;

        ViewHolder(ItemSolvedTodayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SolveEntity item) {
            binding.tvSolveTitle.setText(item.getTitle());
            binding.tvSolvePlatform.setText(item.getPlatform());

            int rating = item.getRating() != null && item.getRating() > 0 ? item.getRating() : 800;
            binding.tvSolveRating.setText(String.format(Locale.getDefault(), "★ %d", rating));
            binding.tvSolveRating.setTextColor(RatingColorUtil.getRatingColor(itemView.getContext(), rating));

            String tags = item.getTags() != null && !item.getTags().isEmpty() ? item.getTags() : "problem";
            binding.tvSolveTags.setText(tags);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSolveClick(item);
            });
        }
    }
}

