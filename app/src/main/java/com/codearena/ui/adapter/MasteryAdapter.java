package com.codearena.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.data.db.entity.TagXpEntity;
import com.codearena.databinding.ItemMasteryBinding;
import java.util.ArrayList;
import java.util.List;

public class MasteryAdapter extends RecyclerView.Adapter<MasteryAdapter.ViewHolder> {

    public interface OnTagClickListener {
        void onTagClick(TagXpEntity item);
    }

    private final List<TagXpEntity> items = new ArrayList<>();
    private String selectedTag = "";
    private OnTagClickListener listener;

    public MasteryAdapter() {}

    public MasteryAdapter(OnTagClickListener listener) {
        this.listener = listener;
    }

    public void setOnTagClickListener(OnTagClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedTag(String tag) {
        this.selectedTag = tag != null ? tag : "";
        notifyDataSetChanged();
    }

    public String getSelectedTag() {
        return selectedTag;
    }

    public void setItems(List<TagXpEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMasteryBinding binding = ItemMasteryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), selectedTag, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMasteryBinding binding;

        ViewHolder(ItemMasteryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TagXpEntity item, String selectedTag, OnTagClickListener listener) {
            binding.tvMasteryTag.setText(item.getTag());
            binding.tvMasteryLevel.setText("Lv " + item.getLevel());
            binding.progressMastery.setProgress(item.getProgressInLevel());
            binding.tvMasteryDetail.setText(item.getXp() + " XP · " + item.getProblemsSolvedCount() + " problems solved");

            boolean isSelected = item.getTag().equalsIgnoreCase(selectedTag);
            if (isSelected) {
                binding.cardMastery.setStrokeColor(android.graphics.Color.parseColor("#0891B2"));
                binding.cardMastery.setStrokeWidth(5);
                binding.cardMastery.setCardElevation(6f);
            } else {
                binding.cardMastery.setStrokeColor(android.graphics.Color.parseColor("#1E293B"));
                binding.cardMastery.setStrokeWidth(2);
                binding.cardMastery.setCardElevation(0f);
            }

            binding.cardMastery.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTagClick(item);
                }
            });
        }
    }
}

