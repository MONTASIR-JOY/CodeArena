package com.codearena.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.R;
import com.codearena.data.model.CfContest;
import com.codearena.databinding.ItemCalendarDayBinding;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.ViewHolder> {

    public static class DayItem {
        public final LocalDate date;
        public final boolean isCurrentMonth;
        public final boolean isToday;
        public boolean isSelected;
        public final List<CfContest> contests = new ArrayList<>();

        public DayItem(LocalDate date, boolean isCurrentMonth, boolean isToday, boolean isSelected) {
            this.date = date;
            this.isCurrentMonth = isCurrentMonth;
            this.isToday = isToday;
            this.isSelected = isSelected;
        }
    }

    public interface OnDayClickListener {
        void onDayClick(DayItem dayItem);
    }

    private final List<DayItem> days = new ArrayList<>();
    private final OnDayClickListener listener;

    public CalendarDayAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setDays(List<DayItem> newDays) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarDayBinding binding = ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(days.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCalendarDayBinding binding;

        ViewHolder(ItemCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DayItem day, OnDayClickListener listener) {
            Context context = itemView.getContext();

            binding.tvDayNumber.setText(String.valueOf(day.date.getDayOfMonth()));

            // Day number styling
            if (day.isToday) {
                binding.tvDayNumber.setBackgroundResource(R.drawable.bg_calendar_today);
                binding.tvDayNumber.setTextColor(Color.WHITE);
            } else if (day.isSelected) {
                binding.tvDayNumber.setBackgroundResource(R.drawable.bg_calendar_selected);
                binding.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.primary));
            } else {
                binding.tvDayNumber.setBackground(null);
                if (day.isCurrentMonth) {
                    binding.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.text_primary_dark));
                } else {
                    binding.tvDayNumber.setTextColor(Color.parseColor("#556B82"));
                }
            }

            // Cell opacity for non-current month
            itemView.setAlpha(day.isCurrentMonth ? 1.0f : 0.45f);

            // Contests pills
            List<CfContest> list = day.contests;
            if (list == null || list.isEmpty()) {
                binding.tvPill1.setVisibility(View.GONE);
                binding.tvPill2.setVisibility(View.GONE);
                binding.tvPillMore.setVisibility(View.GONE);
            } else {
                // Pill 1
                bindPill(binding.tvPill1, list.get(0));

                // Pill 2
                if (list.size() > 1) {
                    bindPill(binding.tvPill2, list.get(1));
                } else {
                    binding.tvPill2.setVisibility(View.GONE);
                }

                // More Pill
                if (list.size() > 2) {
                    binding.tvPillMore.setVisibility(View.VISIBLE);
                    binding.tvPillMore.setText("+" + (list.size() - 2) + " more");
                } else {
                    binding.tvPillMore.setVisibility(View.GONE);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClick(day);
                }
            });
        }

        private void bindPill(android.widget.TextView tv, CfContest contest) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(formatShortTitle(contest));

            String platform = contest.getType() != null ? contest.getType().toLowerCase() : "";
            int bgColor;
            if (platform.contains("codeforces")) {
                bgColor = Color.parseColor("#1E40AF"); // Blue
            } else if (platform.contains("atcoder")) {
                bgColor = Color.parseColor("#581C87"); // Purple
            } else if (platform.contains("codechef")) {
                bgColor = Color.parseColor("#78350F"); // Amber / Brown
            } else if (platform.contains("leetcode")) {
                bgColor = Color.parseColor("#854D0E"); // Gold / Yellow
            } else if (platform.contains("hackerrank")) {
                bgColor = Color.parseColor("#065F46"); // Green
            } else {
                bgColor = Color.parseColor("#1E293B"); // Slate
            }

            tv.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        }

        private String formatShortTitle(CfContest contest) {
            String name = contest.getName();
            if (name == null || name.trim().isEmpty()) {
                return contest.getType() != null ? contest.getType() : "Contest";
            }
            name = name.replaceAll("(?i)<[^>]*>", "").trim();
            // Shorten common prefixes
            if (name.startsWith("Codeforces Round")) {
                name = name.replace("Codeforces Round", "CF Round");
            } else if (name.startsWith("Educational Codeforces Round")) {
                name = name.replace("Educational Codeforces Round", "Edu Round");
            } else if (name.startsWith("AtCoder Beginner Contest")) {
                name = name.replace("AtCoder Beginner Contest", "ABC");
            }
            return name;
        }
    }
}

