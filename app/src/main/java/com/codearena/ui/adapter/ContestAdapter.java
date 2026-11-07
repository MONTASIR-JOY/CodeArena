package com.codearena.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.data.model.CfContest;
import com.codearena.databinding.ItemContestBinding;
import com.codearena.util.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public class ContestAdapter extends RecyclerView.Adapter<ContestAdapter.ViewHolder> {

    public interface OnContestClickListener {
        void onContestClick(CfContest contest);
    }

    private final List<CfContest> contests = new ArrayList<>();
    private final OnContestClickListener listener;

    public ContestAdapter(OnContestClickListener listener) {
        this.listener = listener;
    }

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
        ItemContestBinding binding = ItemContestBinding.inflate(
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

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemContestBinding binding;

        ViewHolder(ItemContestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CfContest contest) {
            binding.tvContestId.setText("#" + contest.getId());
            binding.tvContestType.setText(contest.getType() != null ? contest.getType() : "CF");
            binding.tvContestDuration.setText(TimeUtils.formatDurationHours(contest.getDurationSeconds()));
            binding.tvContestName.setText(contest.getName());

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContestClick(contest);
                }
            });
        }
    }
}

