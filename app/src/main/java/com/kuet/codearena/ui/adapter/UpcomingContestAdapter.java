package com.kuet.codearena.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.remote.model.Platform;
import com.kuet.codearena.databinding.ItemUpcomingContestBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RecyclerView ListAdapter displaying multi-platform upcoming contests with calendar and alarm reminder actions.
 */
public class UpcomingContestAdapter extends ListAdapter<ContestEntity, UpcomingContestAdapter.UpcomingViewHolder> {

    public interface OnUpcomingContestActionListener {
        void onContestClick(ContestEntity contest);
        void onAddToCalendarClick(ContestEntity contest);
        void onSetReminderClick(ContestEntity contest);
    }

    private final OnUpcomingContestActionListener listener;

    private static final DiffUtil.ItemCallback<ContestEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ContestEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull ContestEntity oldItem, @NonNull ContestEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull ContestEntity oldItem, @NonNull ContestEntity newItem) {
                    return oldItem.getName().equals(newItem.getName()) &&
                            oldItem.getStartTimeSeconds() == newItem.getStartTimeSeconds() &&
                            oldItem.getPlatform().equals(newItem.getPlatform());
                }
            };

    public UpcomingContestAdapter(OnUpcomingContestActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public UpcomingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUpcomingContestBinding binding = ItemUpcomingContestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new UpcomingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class UpcomingViewHolder extends RecyclerView.ViewHolder {
        private final ItemUpcomingContestBinding binding;

        public UpcomingViewHolder(ItemUpcomingContestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ContestEntity contest) {
            binding.tvUpcomingTitle.setText(contest.getName());

            String platform = contest.getPlatform();
            binding.tvPlatformBadge.setText(platform);

            int platformColor;
            if (Platform.CODEFORCES.name().equalsIgnoreCase(platform)) {
                platformColor = Color.parseColor("#3182CE");
            } else if (Platform.ATCODER.name().equalsIgnoreCase(platform)) {
                platformColor = Color.parseColor("#222222");
            } else if (Platform.LEETCODE.name().equalsIgnoreCase(platform)) {
                platformColor = Color.parseColor("#FFA116");
            } else if (Platform.CODECHEF.name().equalsIgnoreCase(platform)) {
                platformColor = Color.parseColor("#5B4638");
            } else {
                platformColor = Color.parseColor("#475569");
            }
            binding.tvPlatformBadge.setBackgroundTintList(ColorStateList.valueOf(platformColor));

            long startMs = contest.getStartTimeSeconds() * 1000L;
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d • HH:mm", Locale.getDefault());
            long hours = contest.getDurationSeconds() / 3600;
            long mins = (contest.getDurationSeconds() % 3600) / 60;
            String timeText = sdf.format(new Date(startMs)) + String.format(Locale.getDefault(), " (%dh %02dm)", hours, mins);
            binding.tvUpcomingTime.setText(timeText);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onContestClick(contest);
            });

            binding.btnAddToCalendar.setOnClickListener(v -> {
                if (listener != null) listener.onAddToCalendarClick(contest);
            });

            binding.btnSetReminder.setOnClickListener(v -> {
                if (listener != null) listener.onSetReminderClick(contest);
            });
        }
    }
}
