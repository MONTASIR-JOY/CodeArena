package com.codearena.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.R;
import com.codearena.data.model.CfContest;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.ItemCalendarContestBinding;
import com.codearena.receiver.ContestAlarmReceiver;
import com.codearena.util.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public class CalendarContestAdapter extends RecyclerView.Adapter<CalendarContestAdapter.ViewHolder> {

    private final List<CfContest> contests = new ArrayList<>();

    public void setContests(List<CfContest> newContests) {
        contests.clear();
        if (newContests != null) {
            contests.addAll(newContests);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarContestBinding binding = ItemCalendarContestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(contests.get(position));
    }

    @Override
    public int getItemCount() {
        return contests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCalendarContestBinding binding;

        ViewHolder(ItemCalendarContestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CfContest contest) {
            Context context = itemView.getContext();
            UserPreferencesRepository prefsRepo = new UserPreferencesRepository(context);

            String platform = contest.getType() != null ? contest.getType() : "Codeforces";
            binding.tvCalContestPlatform.setText(platform);

            int colorRes = R.color.platform_codeforces;
            if ("LeetCode".equalsIgnoreCase(platform)) {
                colorRes = R.color.platform_leetcode;
            } else if ("CodeChef".equalsIgnoreCase(platform)) {
                colorRes = R.color.platform_codechef;
            } else if ("AtCoder".equalsIgnoreCase(platform)) {
                colorRes = R.color.platform_atcoder;
            } else if ("HackerRank".equalsIgnoreCase(platform)) {
                colorRes = R.color.platform_hackerrank;
            }
            binding.tvCalContestPlatform.setTextColor(ContextCompat.getColor(context, colorRes));

            binding.tvCalContestDuration.setText(TimeUtils.formatDurationHours(contest.getDurationSeconds()));
            binding.tvCalContestName.setText(contest.getName());

            if (contest.getStartTimeSeconds() != null && contest.getStartTimeSeconds() > 0) {
                binding.tvCalContestTime.setText(TimeUtils.formatEpochSeconds(contest.getStartTimeSeconds()));
            } else {
                binding.tvCalContestTime.setText("Date unannounced");
            }

            // Prevent unwanted trigger during RecyclerView view recycling
            binding.switchCalReminder.setOnCheckedChangeListener(null);
            binding.switchCalReminder.setChecked(prefsRepo.isContestReminderSet(contest.getId()));

            binding.switchCalReminder.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked && contest.getStartTimeSeconds() != null) {
                    ContestAlarmReceiver.scheduleContestReminder(
                            btn.getContext(),
                            contest.getId(),
                            contest.getName(),
                            contest.getStartTimeSeconds() * 1000L
                    );
                    Toast.makeText(btn.getContext(), "Reminder set for " + contest.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    ContestAlarmReceiver.cancelContestReminder(btn.getContext(), contest.getId());
                    Toast.makeText(btn.getContext(), "Reminder cancelled", Toast.LENGTH_SHORT).show();
                }
            });

            binding.btnCalOpenWeb.setOnClickListener(v -> {
                String url = contest.getWebsiteUrl();
                if (url != null && !url.isEmpty()) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        v.getContext().startActivity(intent);
                    } catch (Exception ignored) {}
                }
            });

            itemView.setOnClickListener(v -> binding.btnCalOpenWeb.performClick());
        }
    }
}

