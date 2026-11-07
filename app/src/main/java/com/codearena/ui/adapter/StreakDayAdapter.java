package com.codearena.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.R;
import com.codearena.databinding.ItemStreakDayBinding;
import java.util.ArrayList;
import java.util.List;

public class StreakDayAdapter extends RecyclerView.Adapter<StreakDayAdapter.ViewHolder> {

    public static class DayData {
        public final String dateStr;
        public final String dayNum;
        public final String status; // "ALL", "PARTIAL", "MISSED", "IN_PROGRESS", "NONE"
        public final boolean isToday;

        public DayData(String dateStr, String dayNum, String status, boolean isToday) {
            this.dateStr = dateStr;
            this.dayNum = dayNum;
            this.status = status;
            this.isToday = isToday;
        }
    }

    private final List<DayData> days = new ArrayList<>();

    public void setDays(List<DayData> newDays) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStreakDayBinding binding = ItemStreakDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(days.get(position));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemStreakDayBinding binding;

        ViewHolder(ItemStreakDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DayData data) {
            Context context = itemView.getContext();
            TextView tv = binding.tvStreakDay;
            tv.setText(data.dayNum);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(10f);

            int solidColor;
            int textColor;

            if (data.isToday) {
                if ("ALL".equalsIgnoreCase(data.status)) {
                    solidColor = ContextCompat.getColor(context, R.color.verdict_ok);
                    textColor = Color.WHITE;
                } else if ("PARTIAL".equalsIgnoreCase(data.status)) {
                    solidColor = Color.parseColor("#F59E0B"); // Amber
                    textColor = Color.WHITE;
                } else {
                    // In progress today: Blue / Primary
                    solidColor = ContextCompat.getColor(context, R.color.primary);
                    textColor = Color.WHITE;
                }
                drawable.setStroke(3, Color.WHITE);
            } else if ("ALL".equalsIgnoreCase(data.status)) {
                // Green: all solved
                solidColor = ContextCompat.getColor(context, R.color.verdict_ok);
                textColor = Color.WHITE;
            } else if ("PARTIAL".equalsIgnoreCase(data.status)) {
                // Amber: partly solved
                solidColor = Color.parseColor("#F59E0B");
                textColor = Color.WHITE;
            } else if ("MISSED".equalsIgnoreCase(data.status)) {
                // Red: missed
                solidColor = ContextCompat.getColor(context, R.color.verdict_wa);
                textColor = Color.WHITE;
            } else {
                // Inactive / No challenge recorded
                solidColor = ContextCompat.getColor(context, R.color.surface_dark);
                textColor = ContextCompat.getColor(context, R.color.text_muted_dark);
                drawable.setStroke(1, ContextCompat.getColor(context, R.color.stroke_dark));
            }

            drawable.setColor(solidColor);
            tv.setBackground(drawable);
            tv.setTextColor(textColor);
        }
    }
}

