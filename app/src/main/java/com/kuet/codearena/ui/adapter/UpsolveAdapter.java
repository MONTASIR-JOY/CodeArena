package com.kuet.codearena.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kuet.codearena.data.db.entity.UpsolveItemEntity;
import com.kuet.codearena.databinding.ItemUpsolveBinding;
import com.kuet.codearena.util.RatingColorUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RecyclerView ListAdapter presenting spaced-repetition upsolve queue items with verify actions and due alerts.
 */
public class UpsolveAdapter extends ListAdapter<UpsolveItemEntity, UpsolveAdapter.UpsolveViewHolder> {

    public interface OnUpsolveActionListener {
        void onProblemClick(UpsolveItemEntity item);
        void onVerifyClick(UpsolveItemEntity item);
    }

    private final OnUpsolveActionListener listener;

    private static final DiffUtil.ItemCallback<UpsolveItemEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<UpsolveItemEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull UpsolveItemEntity oldItem, @NonNull UpsolveItemEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull UpsolveItemEntity oldItem, @NonNull UpsolveItemEntity newItem) {
                    return oldItem.isSolved() == newItem.isSolved() &&
                            oldItem.getIntervalStep() == newItem.getIntervalStep() &&
                            oldItem.getDueDateMs() == newItem.getDueDateMs();
                }
            };

    public UpsolveAdapter(OnUpsolveActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public UpsolveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUpsolveBinding binding = ItemUpsolveBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new UpsolveViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UpsolveViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class UpsolveViewHolder extends RecyclerView.ViewHolder {
        private final ItemUpsolveBinding binding;

        public UpsolveViewHolder(ItemUpsolveBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UpsolveItemEntity item) {
            binding.tvUpsolveIndex.setText(item.getProblemIndex());
            binding.tvUpsolveName.setText(item.getProblemName());

            if (item.getRating() != null && item.getRating() > 0) {
                binding.tvUpsolveRating.setText(String.valueOf(item.getRating()));
                int color = RatingColorUtil.getRatingColor(item.getRating());
                binding.tvUpsolveRating.setBackgroundTintList(ColorStateList.valueOf(color));
            } else {
                binding.tvUpsolveRating.setText("Unrated");
                binding.tvUpsolveRating.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#64748B")));
            }

            binding.tvUpsolveIntervalBadge.setText("Day " + item.getIntervalStep() + " Repetition");

            long now = System.currentTimeMillis();
            if (item.getDueDateMs() < now) {
                binding.tvUpsolveDue.setText("Overdue");
                binding.tvUpsolveDue.setTextColor(Color.parseColor("#EF4444"));
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());
                binding.tvUpsolveDue.setText("Due " + sdf.format(new Date(item.getDueDateMs())));
                binding.tvUpsolveDue.setTextColor(Color.parseColor("#64748B"));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onProblemClick(item);
            });

            binding.btnVerifyAc.setOnClickListener(v -> {
                if (listener != null) listener.onVerifyClick(item);
            });
        }
    }
}
