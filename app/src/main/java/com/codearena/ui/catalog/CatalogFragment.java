package com.codearena.ui.catalog;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.codearena.R;
import com.codearena.data.model.CatalogItem;
import com.codearena.data.repository.CatalogRepository;
import com.codearena.databinding.FragmentCatalogBinding;
import com.codearena.ui.adapter.CatalogAdapter;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CatalogFragment extends Fragment {

    private static final int PAGE_SIZE = 25;

    private FragmentCatalogBinding binding;
    private CatalogRepository catalogRepo;
    private CatalogAdapter adapter;

    private List<CatalogItem> allItems = new ArrayList<>();
    private List<CatalogItem> filteredItems = new ArrayList<>();
    private int displayedCount = PAGE_SIZE;

    // Start on Block 1: "General Advice" (only 21 items) so loading is instantaneous
    private String selectedMainBlock = "General Advice";
    private String selectedSubBlock = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCatalogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        catalogRepo = new CatalogRepository(requireContext());

        setupToolbar();
        setupRecyclerView();
        setupSearchAndFilters();
        setupLoadMore();
        loadCatalog();
    }

    private void setupToolbar() {
        binding.toolbarCatalog.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());
    }

    private void setupRecyclerView() {
        adapter = new CatalogAdapter(new CatalogAdapter.OnCatalogActionListener() {
            @Override
            public void onItemClick(CatalogItem item) {
                Bundle args = new Bundle();
                args.putLong("blog_id", item.getBlogId());
                args.putString("blog_title", CatalogAdapter.cleanTitle(item.getTitle()));
                args.putString("blog_author", item.getAuthor());
                Navigation.findNavController(requireView()).navigate(R.id.blogDetailFragment, args);
            }

            @Override
            public void onToggleRead(CatalogItem item, boolean isRead) {
                catalogRepo.setItemRead(item.getId(), isRead);
                updateProgress();

                int readFilterId = binding.chipGroupReadStatus.getCheckedChipId();
                if (readFilterId != R.id.chipReadAll) {
                    applyFilters();
                }
            }
        });

        binding.rvCatalog.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCatalog.setAdapter(adapter);
    }

    private void setupLoadMore() {
        binding.btnLoadMore.setOnClickListener(v -> {
            displayedCount += PAGE_SIZE;
            renderVisibleBatch();
        });
    }

    private void setupSearchAndFilters() {
        binding.etCatalogSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                displayedCount = PAGE_SIZE;
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipGroupReadStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            displayedCount = PAGE_SIZE;
            applyFilters();
        });

        binding.chipGroupMainBlocks.setOnCheckedStateChangeListener((group, checkedIds) -> {
            displayedCount = PAGE_SIZE;
            int checkedId = group.getCheckedChipId();
            if (checkedId == R.id.chipBlockGeneral) {
                selectedMainBlock = "General Advice";
            } else if (checkedId == R.id.chipBlockDS) {
                selectedMainBlock = "Data Structures & Searches";
            } else if (checkedId == R.id.chipBlockDP) {
                selectedMainBlock = "Dynamic Programming";
            } else if (checkedId == R.id.chipBlockGraphs) {
                selectedMainBlock = "Graphs & Trees";
            } else if (checkedId == R.id.chipBlockMath) {
                selectedMainBlock = "Mathematics";
            } else if (checkedId == R.id.chipBlockStringsGeom) {
                selectedMainBlock = "Strings, Geometry & Core";
            } else if (checkedId == R.id.chipBlockMeta) {
                selectedMainBlock = "Languages, Tools & Meta";
            } else if (checkedId == R.id.chipBlockAll) {
                selectedMainBlock = "All";
            } else {
                selectedMainBlock = "General Advice";
            }

            rebuildSubBlockChips();
            applyFilters();
        });
    }

    private void rebuildSubBlockChips() {
        binding.chipGroupSubBlocks.removeAllViews();
        Context context = getContext();
        if (context == null || allItems == null) return;

        Set<String> subBlocks = new LinkedHashSet<>();
        for (CatalogItem item : allItems) {
            if ("All".equals(selectedMainBlock) || selectedMainBlock.equalsIgnoreCase(item.getMainBlock())) {
                if (item.getSubBlock() != null && !item.getSubBlock().isEmpty()) {
                    subBlocks.add(item.getSubBlock());
                }
            }
        }

        List<String> chipList = new ArrayList<>();
        chipList.add("All Sub-Blocks");
        chipList.addAll(subBlocks);

        selectedSubBlock = "All";

        for (int i = 0; i < chipList.size(); i++) {
            String name = chipList.get(i);
            Chip chip = new Chip(context, null, com.google.android.material.R.style.Widget_Material3_Chip_Filter);
            chip.setText(name);
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            if (i == 0) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    displayedCount = PAGE_SIZE;
                    if (name.startsWith("All")) {
                        selectedSubBlock = "All";
                    } else {
                        selectedSubBlock = name;
                    }
                    applyFilters();
                }
            });

            binding.chipGroupSubBlocks.addView(chip);
        }
    }

    private void loadCatalog() {
        allItems = catalogRepo.getCatalogItems();
        rebuildSubBlockChips();
        updateProgress();
        applyFilters();
    }

    private void updateProgress() {
        if (allItems == null || allItems.isEmpty()) return;

        int readCount = 0;
        for (CatalogItem item : allItems) {
            if (item.isRead()) {
                readCount++;
            }
        }

        int total = allItems.size();
        int percent = (int) Math.round(((double) readCount / total) * 100);

        binding.progressCatalog.setProgress(percent);
        binding.tvCatalogProgressPercent.setText(percent + "%");
        binding.tvCatalogProgressDetail.setText(readCount + " of " + total + " tutorials marked as read");
    }

    private void applyFilters() {
        if (allItems == null) return;

        String query = "";
        if (binding.etCatalogSearch.getText() != null) {
            query = binding.etCatalogSearch.getText().toString().trim().toLowerCase();
        }

        int readFilterId = binding.chipGroupReadStatus.getCheckedChipId();
        filteredItems = new ArrayList<>();

        for (CatalogItem item : allItems) {
            // Read status filter
            if (readFilterId == R.id.chipReadUnreadOnly && item.isRead()) {
                continue;
            }
            if (readFilterId == R.id.chipReadCompletedOnly && !item.isRead()) {
                continue;
            }

            // Main block filter
            if (!"All".equals(selectedMainBlock) && !selectedMainBlock.equalsIgnoreCase(item.getMainBlock())) {
                continue;
            }

            // Sub-block filter
            if (!"All".equals(selectedSubBlock) && !selectedSubBlock.equalsIgnoreCase(item.getSubBlock())) {
                continue;
            }

            // Search query filter
            if (!query.isEmpty()) {
                boolean matchesTitle = item.getTitle().toLowerCase().contains(query);
                boolean matchesAuthor = item.getAuthor().toLowerCase().contains(query);
                boolean matchesSub = item.getSubBlock().toLowerCase().contains(query);
                boolean matchesMain = item.getMainBlock().toLowerCase().contains(query);
                boolean matchesDesc = item.getDescription().toLowerCase().contains(query);
                boolean matchesBlogId = String.valueOf(item.getBlogId()).contains(query);

                if (!matchesTitle && !matchesAuthor && !matchesSub && !matchesMain && !matchesDesc && !matchesBlogId) {
                    continue;
                }
            }

            filteredItems.add(item);
        }

        binding.tvResultCount.setText(filteredItems.size() + " tutorials");
        binding.tvCatalogEmpty.setVisibility(filteredItems.isEmpty() ? View.VISIBLE : View.GONE);

        renderVisibleBatch();
    }

    private void renderVisibleBatch() {
        if (filteredItems == null || filteredItems.isEmpty()) {
            adapter.setItems(new ArrayList<>());
            binding.btnLoadMore.setVisibility(View.GONE);
            return;
        }

        int count = Math.min(displayedCount, filteredItems.size());
        List<CatalogItem> visibleSubList = new ArrayList<>(filteredItems.subList(0, count));
        adapter.setItems(visibleSubList);

        if (count < filteredItems.size()) {
            int remaining = filteredItems.size() - count;
            binding.btnLoadMore.setText("Load More (" + remaining + " remaining)");
            binding.btnLoadMore.setVisibility(View.VISIBLE);
        } else {
            binding.btnLoadMore.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
