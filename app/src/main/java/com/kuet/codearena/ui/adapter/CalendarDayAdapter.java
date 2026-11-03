package com.kuet.codearena.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kuet.codearena.databinding.ItemCalendarDayBinding;
import com.kuet.codearena.ui.model.CalendarDayModel;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter displaying 7-column calendar month grids with multi-colored event indicator dots.
 */
public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(CalendarDayModel day);
    }

    private final List<CalendarDayModel> days = new ArrayList<>();
    private final OnDayClickListener listener;

    public CalendarDayAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setDays(List<CalendarDayModel> newDays) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarDayBinding binding = ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new DayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(days.get(position));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private final ItemCalendarDayBinding binding;

        public DayViewHolder(ItemCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CalendarDayModel item) {
            binding.tvDayNumber.setText(String.valueOf(item.getDay()));

            if (!item.isCurrentMonth()) {
                binding.tvDayNumber.setTextColor(Color.parseColor("#94A3B8"));
            } else {
                binding.tvDayNumber.setTextColor(Color.parseColor("#0F172A"));
            }

            if (item.isSelected()) {
                binding.llDayCell.setBackgroundColor(Color.parseColor("#DBEAFE")); // Light Blue highlight
            } else {
                binding.llDayCell.setBackgroundColor(Color.TRANSPARENT);
            }

            binding.dotContest.setVisibility(item.hasContest() ? View.VISIBLE : View.GONE);
            binding.dotUpsolve.setVisibility(item.hasUpsolve() ? View.VISIBLE : View.GONE);
            binding.dotSolve.setVisibility(item.hasSolve() ? View.VISIBLE : View.GONE);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onDayClick(item);
            });
        }
    }
}
