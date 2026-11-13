package com.codearena.ui.cses;

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
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentCsesBinding;
import com.codearena.ui.adapter.CatalogAdapter;
import java.util.ArrayList;
import java.util.List;

public class CsesFragment extends Fragment {

    private static final int PAGE_SIZE = 20;

    private FragmentCsesBinding binding;
    private CatalogRepository catalogRepo;
    private CatalogAdapter adapter;

    private List<CatalogItem> allCsesItems = new ArrayList<>();
    private List<CatalogItem> filteredItems = new ArrayList<>();
    private int displayedCount = PAGE_SIZE;
    private String selectedCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCsesBinding.inflate(inflater, container, false);
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
        loadCsesTasks();
    }

    private void setupToolbar() {
        binding.toolbarCses.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());
    }

    private void setupRecyclerView() {
        adapter = new CatalogAdapter(new CatalogAdapter.OnCatalogActionListener() {
            @Override
            public void onItemClick(CatalogItem item) {
                Bundle args = new Bundle();
                args.putLong("blog_id", 0);
                args.putLong("catalog_item_id", item.getId());
                args.putString("blog_title", item.getTitle());
                args.putString("blog_author", item.getSubBlock() != null ? item.getSubBlock() : "CSES Problem Set");
                if (item.getUrl() != null && !item.getUrl().isEmpty()) {
                    args.putString("web_url", item.getUrl());
                }
                Navigation.findNavController(requireView()).navigate(R.id.blogDetailFragment, args);
            }

            @Override
            public void onToggleRead(CatalogItem item, boolean isRead) {
                catalogRepo.setItemRead(item.getId(), isRead);
                item.setRead(isRead);
                // CatalogRepository.setItemRead() now updates CSES solved count automatically
                updateProgress();

                int checkedStatus = binding.chipGroupCsesStatus.getCheckedChipId();
                if (checkedStatus != R.id.chipCsesAll) {
                    applyFilters();
                }
            }
        });

        binding.rvCses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCses.setAdapter(adapter);
    }

    private void setupLoadMore() {
        binding.btnLoadMoreCses.setOnClickListener(v -> {
            displayedCount += PAGE_SIZE;
            renderVisibleBatch();
        });
    }

    private void setupSearchAndFilters() {
        binding.etCsesSearch.addTextChangedListener(new TextWatcher() {
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

        binding.chipGroupCsesStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            displayedCount = PAGE_SIZE;
            applyFilters();
        });

        binding.chipGroupCsesCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            displayedCount = PAGE_SIZE;
            int checkedId = group.getCheckedChipId();
            if (checkedId == R.id.chipCatIntro) {
                selectedCategory = "Introductory Problems";
            } else if (checkedId == R.id.chipCatSortSearch) {
                selectedCategory = "Sorting and Searching";
            } else if (checkedId == R.id.chipCatDP) {
                selectedCategory = "Dynamic Programming";
            } else if (checkedId == R.id.chipCatGraph) {
                selectedCategory = "Graph Algorithms";
            } else if (checkedId == R.id.chipCatRange) {
                selectedCategory = "Range Queries";
            } else if (checkedId == R.id.chipCatTree) {
                selectedCategory = "Tree Algorithms";
            } else if (checkedId == R.id.chipCatMath) {
                selectedCategory = "Mathematics";
            } else if (checkedId == R.id.chipCatString) {
                selectedCategory = "String Algorithms";
            } else if (checkedId == R.id.chipCatGeom) {
                selectedCategory = "Geometry";
            } else if (checkedId == R.id.chipCatAdvanced) {
                selectedCategory = "Advanced Techniques";
            } else {
                selectedCategory = "All";
            }
            applyFilters();
        });
    }

    private void loadCsesTasks() {
        allCsesItems = catalogRepo.getCsesCatalogItems();
        updateProgress();
        applyFilters();
    }

    private void updateProgress() {
        if (allCsesItems == null || allCsesItems.isEmpty()) return;

        int solvedCount = 0;
        for (CatalogItem item : allCsesItems) {
            if (item.isRead()) solvedCount++;
        }

        int total = allCsesItems.size();
        int percent = (int) Math.round(((double) solvedCount / total) * 100);

        binding.progressCses.setProgress(percent);
        binding.tvCsesProgressPercent.setText(percent + "%");
        binding.tvCsesProgressDetail.setText(solvedCount + " of " + total + " tasks completed");
    }

    private void applyFilters() {
        if (allCsesItems == null) return;

        String query = "";
        if (binding.etCsesSearch.getText() != null) {
            query = binding.etCsesSearch.getText().toString().trim().toLowerCase();
        }

        int checkedStatus = binding.chipGroupCsesStatus.getCheckedChipId();
        filteredItems = new ArrayList<>();

        for (CatalogItem item : allCsesItems) {
            // Solved filter (defaults to Unsolved Only)
            if (checkedStatus == R.id.chipCsesUnsolvedOnly && item.isRead()) {
                continue;
            }
            if (checkedStatus == R.id.chipCsesSolvedOnly && !item.isRead()) {
                continue;
            }

            // Category filter
            if (!"All".equals(selectedCategory) && !selectedCategory.equalsIgnoreCase(item.getSubBlock())) {
                continue;
            }

            // Search filter
            if (!query.isEmpty()) {
                boolean matchesTitle = item.getTitle() != null && item.getTitle().toLowerCase().contains(query);
                boolean matchesSub = item.getSubBlock() != null && item.getSubBlock().toLowerCase().contains(query);
                boolean matchesDesc = item.getDescription() != null && item.getDescription().toLowerCase().contains(query);
                boolean matchesTime = item.getTimeAgo() != null && item.getTimeAgo().toLowerCase().contains(query);
                boolean matchesId = String.valueOf(item.getId()).contains(query);

                if (!matchesTitle && !matchesSub && !matchesDesc && !matchesTime && !matchesId) {
                    continue;
                }
            }

            filteredItems.add(item);
        }

        binding.tvCsesResultCount.setText(filteredItems.size() + " tasks");
        binding.tvCsesEmpty.setVisibility(filteredItems.isEmpty() ? View.VISIBLE : View.GONE);

        renderVisibleBatch();
    }

    private void renderVisibleBatch() {
        if (filteredItems == null || filteredItems.isEmpty()) {
            adapter.setItems(new ArrayList<>());
            binding.btnLoadMoreCses.setVisibility(View.GONE);
            return;
        }

        int count = Math.min(displayedCount, filteredItems.size());
        List<CatalogItem> visibleSubList = new ArrayList<>(filteredItems.subList(0, count));
        adapter.setItems(visibleSubList);

        if (count < filteredItems.size()) {
            int remaining = filteredItems.size() - count;
            binding.btnLoadMoreCses.setText("Load More (" + remaining + " remaining)");
            binding.btnLoadMoreCses.setVisibility(View.VISIBLE);
        } else {
            binding.btnLoadMoreCses.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

