package com.codearena.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.R;
import com.codearena.data.model.AchievementBadge;
import com.codearena.databinding.ItemBadgeBinding;
import java.util.ArrayList;
import java.util.List;

public class AchievementBadgeAdapter extends RecyclerView.Adapter<AchievementBadgeAdapter.ViewHolder> {

    private final List<AchievementBadge> badges = new ArrayList<>();

    public void setBadges(List<AchievementBadge> newBadges) {
        badges.clear();
        if (newBadges != null) {
            badges.addAll(newBadges);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBadgeBinding binding = ItemBadgeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(badges.get(position));
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBadgeBinding binding;

        ViewHolder(ItemBadgeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AchievementBadge badge) {
            Context context = itemView.getContext();

            binding.tvBadgeTitle.setText(badge.getTitle());
            binding.tvBadgeDesc.setText(badge.getDescription());

            if (badge.isUnlocked()) {
                binding.getRoot().setAlpha(1.0f);
                binding.ivBadge.setColorFilter(ContextCompat.getColor(context, R.color.primary));
                binding.tvBadgeProgress.setText("Unlocked ✓");
                binding.tvBadgeProgress.setTextColor(ContextCompat.getColor(context, R.color.verdict_ok));
            } else {
                binding.getRoot().setAlpha(0.6f);
                binding.ivBadge.setColorFilter(ContextCompat.getColor(context, R.color.text_muted_dark));
                binding.tvBadgeProgress.setText(badge.getProgress() + " / " + badge.getMaxProgress());
                binding.tvBadgeProgress.setTextColor(ContextCompat.getColor(context, R.color.text_muted_dark));
            }
        }
    }
}

