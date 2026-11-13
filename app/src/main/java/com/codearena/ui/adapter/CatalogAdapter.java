package com.codearena.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.data.model.CatalogItem;
import com.codearena.databinding.ItemCatalogBinding;
import java.util.ArrayList;
import java.util.List;

public class CatalogAdapter extends RecyclerView.Adapter<CatalogAdapter.ViewHolder> {

    public interface OnCatalogActionListener {
        void onItemClick(CatalogItem item);
        void onToggleRead(CatalogItem item, boolean isRead);
    }

    private final List<CatalogItem> items = new ArrayList<>();
    private final OnCatalogActionListener listener;

    public CatalogAdapter(OnCatalogActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CatalogItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public static String cleanTitle(String raw) {
        if (raw == null) return "";
        // Strip escaped backslashes
        String t = raw.replace("\\[", "[").replace("\\]", "]");
        // Strip leading tags like [Tutorial], [Problemsetting], [Insight], etc.
        t = t.replaceAll("(?i)^\\s*\\[(Tutorial|Problemsetting|Insight|Idea|Bugged|Math note|GUIDE)[^\\]]*\\]\\s*", "");
        // Clean common HTML entities
        t = t.replace("&mdash;", "—").replace("&ndash;", "–").replace("&amp;", "&").replace("&quot;", "\"");
        return t.replace("\\", "").trim();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCatalogBinding binding = ItemCatalogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCatalogBinding binding;

        ViewHolder(ItemCatalogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CatalogItem item, OnCatalogActionListener listener) {
            binding.tvCatalogSection.setText(item.getMainBlock());
            binding.tvCatalogCategory.setText(item.getSubBlock());
            
            String timeAgo = item.getTimeAgo();
            if (timeAgo != null && !timeAgo.isEmpty()) {
                binding.tvCatalogLevel.setText("· " + timeAgo);
                binding.tvCatalogLevel.setVisibility(View.VISIBLE);
            } else {
                binding.tvCatalogLevel.setVisibility(View.GONE);
            }

            String cleanTitleStr = cleanTitle(item.getTitle());
            binding.tvCatalogTitle.setText(cleanTitleStr);

            String desc = item.getDescription();
            if (desc != null && !desc.trim().isEmpty()) {
                binding.tvCatalogDescription.setText(desc.trim());
                binding.tvCatalogDescription.setVisibility(View.VISIBLE);
            } else {
                binding.tvCatalogDescription.setVisibility(View.GONE);
            }

            if (item.getBlogId() > 0) {
                binding.tvCatalogAuthor.setText("By " + item.getAuthor() + " · Blog #" + item.getBlogId());
            } else {
                binding.tvCatalogAuthor.setText(item.getAuthor());
            }

            // Detach listener before changing checked state to avoid unwanted triggers
            binding.cbCatalogRead.setOnCheckedChangeListener(null);
            binding.cbCatalogRead.setChecked(item.isRead());
            binding.cbCatalogRead.setOnCheckedChangeListener((btn, isChecked) -> {
                item.setRead(isChecked);
                if (listener != null) {
                    listener.onToggleRead(item, isChecked);
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
