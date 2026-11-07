package com.codearena.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.R;
import com.codearena.data.model.CfBlogEntry;
import com.codearena.databinding.ItemBlogBinding;
import com.codearena.util.BlogFeed;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.ViewHolder> {

    public interface OnFriendToggleListener {
        void onToggleFriend(String authorHandle);
    }

    public interface OnReadToggleListener {
        void onToggleRead(CfBlogEntry blog, boolean isRead);
    }

    private final List<CfBlogEntry> blogs = new ArrayList<>();
    private final Set<String> friendHandles = new HashSet<>();
    private final Set<String> readBlogIds = new HashSet<>();
    private OnFriendToggleListener friendToggleListener;
    private OnReadToggleListener readToggleListener;

    public void setBlogs(List<CfBlogEntry> newBlogs) {
        blogs.clear();
        if (newBlogs != null) {
            blogs.addAll(newBlogs);
        }
        notifyDataSetChanged();
    }

    public void setFriendHandles(Set<String> friends) {
        friendHandles.clear();
        if (friends != null) {
            friendHandles.addAll(friends);
        }
        notifyDataSetChanged();
    }

    public void setReadBlogIds(Set<String> readIds) {
        readBlogIds.clear();
        if (readIds != null) {
            readBlogIds.addAll(readIds);
        }
        notifyDataSetChanged();
    }

    public void setOnFriendToggleListener(OnFriendToggleListener listener) {
        this.friendToggleListener = listener;
    }

    public void setOnReadToggleListener(OnReadToggleListener listener) {
        this.readToggleListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBlogBinding binding = ItemBlogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(blogs.get(position), friendHandles, readBlogIds, friendToggleListener, readToggleListener);
    }

    @Override
    public int getItemCount() {
        return blogs.size();
    }

    public static String cleanHtml(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";
        String parsed = Html.fromHtml(raw, Html.FROM_HTML_MODE_LEGACY).toString().trim();
        return parsed.replaceAll("<[^>]*>", "").trim();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBlogBinding binding;

        ViewHolder(ItemBlogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CfBlogEntry blog, Set<String> friends, Set<String> readIds,
                  OnFriendToggleListener friendListener, OnReadToggleListener readListener) {
            Context context = itemView.getContext();

            // Author & Avatar Initial
            String author = blog.getAuthorHandle();
            if (author == null || author.trim().isEmpty()) {
                author = "Codeforces Member";
            }
            binding.tvBlogAuthor.setText(author);
            String initial = author.length() > 0 ? author.substring(0, 1).toUpperCase() : "C";
            binding.tvBlogAvatarInitial.setText(initial);

            // Friend Status & Toggle
            boolean isFriend = friends != null && friends.contains(author);
            binding.btnBlogFriend.setImageResource(isFriend ? R.drawable.ic_friends : R.drawable.ic_person_add);
            binding.btnBlogFriend.setColorFilter(ContextCompat.getColor(context, isFriend ? R.color.primary : R.color.text_muted_dark));
            final String toggleAuthor = author;
            binding.btnBlogFriend.setOnClickListener(v -> {
                if (friendListener != null && !toggleAuthor.isEmpty() && !toggleAuthor.equals("Codeforces Member")) {
                    friendListener.onToggleFriend(toggleAuthor);
                }
            });

            // Time Ago
            long nowSec = System.currentTimeMillis() / 1000;
            long blogSec = blog.getCreationTimeSeconds();
            if (blogSec > 0) {
                binding.tvBlogTimeAgo.setText(BlogFeed.timeAgo(nowSec, blogSec));
                binding.tvBlogTimeAgo.setVisibility(View.VISIBLE);
            } else {
                binding.tvBlogTimeAgo.setVisibility(View.GONE);
            }

            // Rating Badge
            int rating = blog.getRating();
            if (rating > 0) {
                binding.tvBlogRating.setText("▲ +" + rating);
                binding.tvBlogRating.setTextColor(ContextCompat.getColor(context, R.color.verdict_ok));
            } else if (rating < 0) {
                binding.tvBlogRating.setText("▼ " + rating);
                binding.tvBlogRating.setTextColor(ContextCompat.getColor(context, R.color.verdict_wa));
            } else {
                binding.tvBlogRating.setText("0");
                binding.tvBlogRating.setTextColor(ContextCompat.getColor(context, R.color.text_muted_dark));
            }

            // Clean Title
            String cleanTitle = cleanHtml(blog.getTitle());
            if (cleanTitle.isEmpty()) {
                cleanTitle = "Codeforces Blog Post";
            }
            binding.tvBlogTitle.setText(cleanTitle);

            // Tags
            List<String> tags = blog.getTags();
            if (tags != null && !tags.isEmpty()) {
                binding.layoutBlogTags.setVisibility(View.VISIBLE);
                binding.tvBlogTag1.setText(tags.get(0));
                binding.tvBlogTag1.setVisibility(View.VISIBLE);

                if (tags.size() > 1) {
                    binding.tvBlogTag2.setText(tags.get(1));
                    binding.tvBlogTag2.setVisibility(View.VISIBLE);
                } else {
                    binding.tvBlogTag2.setVisibility(View.GONE);
                }

                if (tags.size() > 2) {
                    binding.tvBlogMoreTags.setText("+" + (tags.size() - 2) + " more");
                    binding.tvBlogMoreTags.setVisibility(View.VISIBLE);
                } else {
                    binding.tvBlogMoreTags.setVisibility(View.GONE);
                }
            } else {
                binding.layoutBlogTags.setVisibility(View.GONE);
            }

            // Blog ID
            binding.tvBlogId.setText("Blog #" + blog.getId());

            // Read / Unread status
            boolean isRead = readIds != null && readIds.contains(String.valueOf(blog.getId()));
            binding.cbBlogRead.setOnCheckedChangeListener(null);
            binding.cbBlogRead.setChecked(isRead);
            binding.getRoot().setAlpha(isRead ? 0.72f : 1.0f);
            binding.cbBlogRead.setOnCheckedChangeListener((btn, isChecked) -> {
                binding.getRoot().setAlpha(isChecked ? 0.72f : 1.0f);
                if (readListener != null) {
                    readListener.onToggleRead(blog, isChecked);
                }
            });

            final String finalTitle = cleanTitle;
            final String finalAuthor = author;
            binding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong("blog_id", blog.getId());
                args.putString("blog_title", finalTitle);
                args.putString("blog_author", finalAuthor);
                Navigation.findNavController(v).navigate(R.id.blogDetailFragment, args);
            });
        }
    }
}
