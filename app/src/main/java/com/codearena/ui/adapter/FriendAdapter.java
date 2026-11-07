package com.codearena.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.data.model.CfUser;
import com.codearena.databinding.ItemFriendBinding;
import com.codearena.util.RankColors;
import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    public interface OnFriendLongClickListener {
        void onFriendLongClick(CfUser user);
    }

    private final List<CfUser> friends = new ArrayList<>();
    private int myRating = 0;
    private final OnFriendLongClickListener longClickListener;

    public FriendAdapter(OnFriendLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setFriends(List<CfUser> newFriends, int myRating) {
        this.myRating = myRating;
        friends.clear();
        if (newFriends != null) {
            friends.addAll(newFriends);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFriendBinding binding = ItemFriendBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(friends.get(position), position + 1);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFriendBinding binding;

        ViewHolder(ItemFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CfUser user, int rank) {
            binding.tvFriendPosition.setText("#" + rank);
            binding.tvFriendHandle.setText(user.getHandle());

            int color = RankColors.getColorForRating(user.getRating());
            binding.tvFriendHandle.setTextColor(color);
            binding.tvFriendRating.setText(String.valueOf(user.getRating() != null ? user.getRating() : 0));
            binding.tvFriendRating.setTextColor(color);

            binding.tvFriendMeta.setText(RankColors.getTitleForRating(user.getRating()) + " · max " +
                    (user.getMaxRating() != null ? user.getMaxRating() : 0));

            int diff = (user.getRating() != null ? user.getRating() : 0) - myRating;
            binding.tvFriendDelta.setText((diff >= 0 ? "+" : "") + diff + " vs you");

            binding.getRoot().setOnLongClickListener(v -> {
                if (longClickListener != null) longClickListener.onFriendLongClick(user);
                return true;
            });
        }
    }
}

