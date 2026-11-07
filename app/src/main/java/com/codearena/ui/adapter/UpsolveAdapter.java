package com.codearena.ui.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.data.db.entity.UpsolveItemEntity;
import com.codearena.databinding.ItemUpsolveBinding;
import java.util.ArrayList;
import java.util.List;

public class UpsolveAdapter extends RecyclerView.Adapter<UpsolveAdapter.ViewHolder> {

    public interface OnUpsolveActionListener {
        void onSolved(UpsolveItemEntity item);
        void onStillStuck(UpsolveItemEntity item);
    }

    private final List<UpsolveItemEntity> items = new ArrayList<>();
    private final OnUpsolveActionListener listener;

    public UpsolveAdapter(OnUpsolveActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<UpsolveItemEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUpsolveBinding binding = ItemUpsolveBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemUpsolveBinding binding;

        ViewHolder(ItemUpsolveBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(UpsolveItemEntity item) {
            binding.tvUpsolveIndex.setText(item.getContestId() + " " + item.getProblemIndex());
            binding.tvUpsolveName.setText(item.getProblemName());

            long diff = item.getNextReviewEpochMillis() - System.currentTimeMillis();
            if (diff <= 0) {
                binding.tvUpsolveDue.setText("Due now");
            } else {
                long hours = diff / (3600 * 1000L);
                binding.tvUpsolveDue.setText("Due in " + Math.max(1, hours) + "h");
            }

            String tags = item.getTags() != null ? String.join(", ", item.getTags()) : "";
            binding.tvUpsolveMeta.setText("Rating: " + (item.getRating() != null ? item.getRating() : "Unrated")
                    + " · " + tags + " · Stage " + (item.getIntervalIndex() + 1) + "/5");

            binding.btnUpsolveOpen.setOnClickListener(v -> {
                String url = "https://codeforces.com/contest/" + item.getContestId() + "/problem/" + item.getProblemIndex();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                v.getContext().startActivity(intent);
            });

            binding.btnUpsolveSolved.setOnClickListener(v -> {
                if (listener != null) listener.onSolved(item);
            });

            binding.btnUpsolveStuck.setOnClickListener(v -> {
                if (listener != null) listener.onStillStuck(item);
            });
        }
    }
}

