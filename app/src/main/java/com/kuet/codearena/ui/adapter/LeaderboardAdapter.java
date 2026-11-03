package com.kuet.codearena.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kuet.codearena.R;
import com.kuet.codearena.databinding.ItemLeaderboardEntryBinding;
import com.kuet.codearena.ui.model.LeaderboardUserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter rendering leaderboard standing ranks, usernames, and competitive metrics.
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    public enum Mode {
        SOLVED,
        POINTS,
        CF_RATING,
        PRACTICE_RATING
    }

    private final List<LeaderboardUserModel> items = new ArrayList<>();
    private final List<String> dateKeys = new ArrayList<>();
    private Mode currentMode = Mode.SOLVED;

    public void setData(List<LeaderboardUserModel> list, Mode mode, List<String> activeDateKeys) {
        this.items.clear();
        if (list != null) {
            this.items.addAll(list);
        }
        this.currentMode = mode;
        this.dateKeys.clear();
        if (activeDateKeys != null) {
            this.dateKeys.addAll(activeDateKeys);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderboardEntryBinding binding = ItemLeaderboardEntryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), position + 1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLeaderboardEntryBinding binding;

        ViewHolder(ItemLeaderboardEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LeaderboardUserModel user, int rank) {
            binding.tvLeaderboardRank.setText(String.valueOf(rank));

            // Gold, Silver, Bronze coloring for top 3
            if (rank == 1) {
                binding.tvLeaderboardRank.setBackgroundColor(Color.parseColor("#FFD700"));
                binding.tvLeaderboardRank.setTextColor(Color.BLACK);
            } else if (rank == 2) {
                binding.tvLeaderboardRank.setBackgroundColor(Color.parseColor("#C0C0C0"));
                binding.tvLeaderboardRank.setTextColor(Color.BLACK);
            } else if (rank == 3) {
                binding.tvLeaderboardRank.setBackgroundColor(Color.parseColor("#CD7F32"));
                binding.tvLeaderboardRank.setTextColor(Color.WHITE);
            } else {
                binding.tvLeaderboardRank.setBackgroundColor(itemView.getContext().getColor(R.color.surface_dark));
                binding.tvLeaderboardRank.setTextColor(Color.WHITE);
            }

            binding.tvLeaderboardName.setText(user.getDisplayName());
            binding.tvLeaderboardSubtext.setText(String.format(Locale.getDefault(),
                    "CF: %d • Practice: %.0f", user.getCfRating(), user.getPracticeRating()));

            if (currentMode == Mode.SOLVED) {
                int solved = user.getSolvedInWindow(dateKeys);
                binding.tvLeaderboardMetric.setText(String.format(Locale.getDefault(), "%d solves", solved));
                binding.tvLeaderboardMetric.setTextColor(itemView.getContext().getColor(R.color.cf_blue));
            } else if (currentMode == Mode.POINTS) {
                int pts = user.getPointsInWindow(dateKeys);
                binding.tvLeaderboardMetric.setText(String.format(Locale.getDefault(), "%,d pts", pts));
                binding.tvLeaderboardMetric.setTextColor(Color.parseColor("#4CAF50"));
            } else if (currentMode == Mode.CF_RATING) {
                binding.tvLeaderboardMetric.setText(String.valueOf(user.getCfRating()));
                binding.tvLeaderboardMetric.setTextColor(itemView.getContext().getColor(R.color.accent_secondary));
            } else {
                binding.tvLeaderboardMetric.setText(String.format(Locale.getDefault(), "%.0f", user.getPracticeRating()));
                binding.tvLeaderboardMetric.setTextColor(itemView.getContext().getColor(R.color.accent_primary));
            }
        }
    }
}

