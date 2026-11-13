package com.codearena.ui.blogs;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.codearena.R;
import com.codearena.data.model.CfBlogEntry;
import com.codearena.data.model.CfRecentAction;
import com.codearena.data.repository.CodeforcesRepository;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentBlogsBinding;
import com.codearena.ui.adapter.BlogAdapter;
import com.codearena.util.BlogFeed;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BlogsFragment extends Fragment {

    private static final int PAGE_SIZE = 20;

    private FragmentBlogsBinding binding;
    private CodeforcesRepository cfRepo;
    private UserPreferencesRepository prefsRepo;
    private BlogAdapter blogAdapter;

    private List<CfRecentAction> cachedActions = new ArrayList<>();
    private List<CfBlogEntry> baseBlogs = new ArrayList<>();
    private List<CfBlogEntry> friendsBlogs = new ArrayList<>();
    private List<CfBlogEntry> lastFilteredResult = new ArrayList<>();
    private boolean isFriendsLoading = false;
    private int displayedCount = PAGE_SIZE;
    private String selectedFriendHandle = null; // null = "All friends"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentBlogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cfRepo = new CodeforcesRepository(requireContext());
        prefsRepo = new UserPreferencesRepository(requireContext());

        setupToolbar();
        setupRecyclerView();
        setupSearchAndFilters();
        setupSwipeRefresh();

        loadCachedBlogs();
        loadBlogs(baseBlogs.isEmpty());
        // Set up friend picker button click
        binding.btnChooseFriendDialog.setOnClickListener(v -> showFriendPickerDialog());

    }

    private void setupToolbar() {
        binding.toolbarBlogs.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        binding.toolbarBlogs.inflateMenu(R.menu.menu_blogs);
        binding.toolbarBlogs.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_manage_friends) {
                showManageFriendsDialog();
                return true; 
            } else if (id == R.id.action_open_friends_screen) {
                Navigation.findNavController(requireView()).navigate(R.id.friendsFragment);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        blogAdapter = new BlogAdapter();
        blogAdapter.setFriendHandles(prefsRepo.getFriends());
        blogAdapter.setReadBlogIds(prefsRepo.getReadBlogIds());
        blogAdapter.setOnFriendToggleListener(author -> {
            if (prefsRepo.getFriends().contains(author)) {
                prefsRepo.removeFriend(author);
                Toast.makeText(requireContext(), "Removed " + author + " from friends", Toast.LENGTH_SHORT).show();
            } else {
                prefsRepo.addFriend(author);
                Toast.makeText(requireContext(), "Added " + author + " to friends", Toast.LENGTH_SHORT).show();
            }
            blogAdapter.setFriendHandles(prefsRepo.getFriends());
            friendsBlogs.clear();
            if (binding != null && binding.chipGroupBlogs.getCheckedChipId() == R.id.chipBlogFriends) {
                rebuildFriendSubChips();
                loadFriendsBlogs(true);
            } else {
                applyFilters();
            }
        });
        blogAdapter.setOnReadToggleListener((blog, isRead) -> {
            prefsRepo.setBlogRead(blog.getId(), isRead);
            blogAdapter.setReadBlogIds(prefsRepo.getReadBlogIds());
            if (binding != null && binding.chipGroupBlogReadStatus.getCheckedChipId() != R.id.chipBlogReadAll) {
                applyFilters();
            }
        });
        binding.rvBlogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBlogs.setAdapter(blogAdapter);

        // Load-more button
        binding.btnLoadMoreBlogs.setOnClickListener(v -> {
            displayedCount += PAGE_SIZE;
            renderPage();
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshBlogs.setColorSchemeResources(R.color.primary);
        binding.swipeRefreshBlogs.setOnRefreshListener(() -> {
            displayedCount = PAGE_SIZE;
            if (binding != null && binding.chipGroupBlogs.getCheckedChipId() == R.id.chipBlogFriends) {
                loadFriendsBlogs(true);
            } else {
                loadBlogs(false);
            }
        });
        binding.btnRetryBlogs.setOnClickListener(v -> {
            displayedCount = PAGE_SIZE;
            if (binding != null && binding.chipGroupBlogs.getCheckedChipId() == R.id.chipBlogFriends) {
                loadFriendsBlogs(true);
            } else {
                loadBlogs(true);
            }
        });
    }

    private void setupSearchAndFilters() {
        binding.btnBlogsManageFriends.setOnClickListener(v -> showManageFriendsDialog());
        binding.btnEmptyAddFriends.setOnClickListener(v -> showManageFriendsDialog());

        binding.etBlogSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                displayedCount = PAGE_SIZE;
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.chipGroupBlogs.setOnCheckedStateChangeListener((group, checkedIds) -> {
            displayedCount = PAGE_SIZE;
            int checkedId = binding.chipGroupBlogs.getCheckedChipId();
            if (checkedId == R.id.chipBlogFriends) {
                selectedFriendHandle = null;
                binding.layoutFriendSelector.setVisibility(View.VISIBLE);
                rebuildFriendSubChips();
                if (friendsBlogs.isEmpty()) {
                    loadFriendsBlogs(false);
                    return;
                }
            } else {
                binding.layoutFriendSelector.setVisibility(View.GONE);
            }
            applyFilters();
        });

        binding.chipGroupBlogReadStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            displayedCount = PAGE_SIZE;
            applyFilters();
        });
    }

    // ---------- Friend sub-chips ----------

    private void rebuildFriendSubChips() {
        if (binding == null)
            return;
        Set<String> friends = prefsRepo.getFriends();
        if (friends == null || friends.isEmpty()) {
            binding.scrollFriendChips.setVisibility(View.GONE);
            return;
        }
        binding.scrollFriendChips.setVisibility(View.VISIBLE);
        binding.chipGroupBlogFriendsSub.removeAllViews();
        Context ctx = requireContext();

        // "All" chip
        Chip allChip = new Chip(ctx, null, com.google.android.material.R.style.Widget_Material3_Chip_Filter);
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(selectedFriendHandle == null);
        allChip.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                selectedFriendHandle = null;
                displayedCount = PAGE_SIZE;
                applyFilters();
            }
        });
        binding.chipGroupBlogFriendsSub.addView(allChip);

        for (String f : friends) {
            Chip chip = new Chip(ctx, null, com.google.android.material.R.style.Widget_Material3_Chip_Filter);
            chip.setText(f);
            chip.setCheckable(true);
            chip.setChecked(f.equals(selectedFriendHandle));
            chip.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) {
                    selectedFriendHandle = f;
                    displayedCount = PAGE_SIZE;
                    applyFilters();
                }
            });
            binding.chipGroupBlogFriendsSub.addView(chip);
        }
    }

    // ---------- Data loading ----------

    private void loadCachedBlogs() {
        String json = prefsRepo.getCachedBlogsJson();
        if (json != null && !json.trim().isEmpty()) {
            try {
                Type listType = new TypeToken<List<CfBlogEntry>>() {}
                .getType();
                List<CfBlogEntry> cached = new Gson().fromJson(json, listType);
                if (cached != null && !cached.isEmpty()) {
                    baseBlogs = cached;
                    applyFilters();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void loadBlogs(boolean showProgress) {
        if (showProgress && baseBlogs.isEmpty()) {
            binding.progressBlogs.setVisibility(View.VISIBLE);
        }
        binding.layoutBlogError.setVisibility(View.GONE);

        cfRepo.fetchRecentActions(new CodeforcesRepository.Callback<List<CfRecentAction>>() {
            @Override
            public void onSuccess(List<CfRecentAction> result) {
                if (!isAdded())
                    return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null)
                        return;
                    binding.progressBlogs.setVisibility(View.GONE);
                    binding.swipeRefreshBlogs.setRefreshing(false);
                    binding.layoutBlogError.setVisibility(View.GONE);

                    cachedActions = (result != null) ? result : new ArrayList<>();
                    List<CfBlogEntry> distinct = BlogFeed.blogs(cachedActions, BlogFeed.Sort.NEWEST);
                    if (!distinct.isEmpty()) {
                        baseBlogs = distinct;
                        try {
                            String json = new Gson().toJson(baseBlogs);
                            prefsRepo.setCachedBlogsJson(json);
                        } catch (Exception ignored) {
                        }
                    }
                    applyFilters();
                });
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded())
                    return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null)
                        return;
                    binding.progressBlogs.setVisibility(View.GONE);
                    binding.swipeRefreshBlogs.setRefreshing(false);
                    if (baseBlogs.isEmpty()) {
                        String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "Network error";
                        binding.tvBlogsError.setText("Failed to load community blogs: " + msg);
                        binding.layoutBlogError.setVisibility(View.VISIBLE);
                    }
                    applyFilters();
                });
            }
        });
    }

    private void loadFriendsBlogs(boolean force) {
        Set<String> friends = prefsRepo.getFriends();
        if (friends == null || friends.isEmpty()) {
            friendsBlogs.clear();
            applyFilters();
            return;
        }
        if (!force && !friendsBlogs.isEmpty()) {
            applyFilters();
            return;
        }
        if (binding == null)
            return;
        binding.progressBlogs.setVisibility(View.VISIBLE);
        isFriendsLoading = true;

        List<String> friendList = new ArrayList<>(friends);
        List<CfBlogEntry> collected = Collections.synchronizedList(new ArrayList<>());
        java.util.concurrent.atomic.AtomicInteger remaining = new java.util.concurrent.atomic.AtomicInteger(
                friendList.size());

        for (String handle : friendList) {
            cfRepo.fetchUserBlogEntries(handle, new CodeforcesRepository.Callback<List<CfBlogEntry>>() {
                @Override
                public void onSuccess(List<CfBlogEntry> entries) {
                    if (entries != null) {
                        for (CfBlogEntry e : entries) {
                            if (e.getAuthorHandle() == null || e.getAuthorHandle().isEmpty()) {
                                e.setAuthorHandle(handle);
                            }
                            collected.add(e);
                        }
                    }
                    checkDone();
                }

                @Override
                public void onError(Exception error) {
                    checkDone();
                }

                private void checkDone() {
                    if (remaining.decrementAndGet() == 0) {
                        if (!isAdded())
                            return;
                        requireActivity().runOnUiThread(() -> {
                            isFriendsLoading = false;
                            if (binding != null) {
                                binding.progressBlogs.setVisibility(View.GONE);
                                binding.swipeRefreshBlogs.setRefreshing(false);
                            }
                            Collections.sort(collected,
                                    (a, b) -> Long.compare(b.getCreationTimeSeconds(), a.getCreationTimeSeconds()));
                            friendsBlogs = new ArrayList<>(collected);
                            selectedFriendHandle = null;
                            displayedCount = PAGE_SIZE;
                            rebuildFriendSubChips();
                            applyFilters();
                        });
                    }
                }
            });
        }
    }

    // ---------- Filtering + Pagination ----------

    private void applyFilters() {
        if (binding == null)
            return;

        int checkedId = binding.chipGroupBlogs.getCheckedChipId();
        List<CfBlogEntry> workingList;
        boolean isFriendsFilter = (checkedId == R.id.chipBlogFriends);
        Set<String> friends = prefsRepo.getFriends();

        if (isFriendsFilter) {
            workingList = new ArrayList<>(friendsBlogs);
            // Sub-filter by specific friend
            if (selectedFriendHandle != null) {
                List<CfBlogEntry> filtered = new ArrayList<>();
                for (CfBlogEntry b : workingList) {
                    if (selectedFriendHandle.equalsIgnoreCase(b.getAuthorHandle())) {
                        filtered.add(b);
                    }
                }
                workingList = filtered;
            }
            binding.tvBlogFeedSubtitle.setText(friends.isEmpty() ? "No friends added yet"
                    : "Friends Feed (" + friends.size() + " friends • " + workingList.size() + " blogs)");
        } else if (checkedId == R.id.chipBlogActive && !cachedActions.isEmpty()) {
            workingList = BlogFeed.blogs(cachedActions, BlogFeed.Sort.ACTIVE);
            binding.tvBlogFeedSubtitle.setText("Live discussions & round announcements");
        } else {
            workingList = new ArrayList<>(baseBlogs);
            binding.tvBlogFeedSubtitle.setText("Live discussions & round announcements");
        }

        // Filter: Editorials
        if (checkedId == R.id.chipBlogEditorials) {
            List<CfBlogEntry> editorials = new ArrayList<>();
            for (CfBlogEntry b : workingList) {
                boolean isEditorial = false;
                if (b.getTitle() != null) {
                    String lt = b.getTitle().toLowerCase();
                    if (lt.contains("editorial") || lt.contains("tutorial") ||
                            lt.contains("analysis") || lt.contains("solution") ||
                            lt.contains("round") || lt.contains("div.")) {
                        isEditorial = true;
                    }
                }
                if (!isEditorial && b.getTags() != null) {
                    for (String tag : b.getTags()) {
                        if (tag.toLowerCase().contains("editorial") || tag.toLowerCase().contains("tutorial")) {
                            isEditorial = true;
                            break;
                        }
                    }
                }
                if (isEditorial)
                    editorials.add(b);
            }
            workingList = editorials;
        }

        // Filter: Top Rated
        if (checkedId == R.id.chipBlogTopRated) {
            List<CfBlogEntry> topRated = new ArrayList<>();
            for (CfBlogEntry b : workingList) {
                if (b.getRating() >= 25)
                    topRated.add(b);
            }
            Collections.sort(topRated, (a, b) -> Integer.compare(b.getRating(), a.getRating()));
            workingList = topRated;
        }

        // Filter: Read status
        int readFilterId = binding.chipGroupBlogReadStatus.getCheckedChipId();
        Set<String> readIds = prefsRepo.getReadBlogIds();
        List<CfBlogEntry> readFiltered = new ArrayList<>();
        for (CfBlogEntry b : workingList) {
            boolean isRead = readIds.contains(String.valueOf(b.getId()));
            if (readFilterId == R.id.chipBlogReadUnreadOnly && isRead)
                continue;
            if (readFilterId == R.id.chipBlogReadCompletedOnly && !isRead)
                continue;
            readFiltered.add(b);
        }
        workingList = readFiltered;

        // Search Query Filter
        String query = "";
        if (binding.etBlogSearch.getText() != null) {
            query = binding.etBlogSearch.getText().toString().trim().toLowerCase();
        }

        List<CfBlogEntry> finalResult;
        if (!query.isEmpty()) {
            finalResult = new ArrayList<>();
            for (CfBlogEntry b : workingList) {
                boolean matchesTitle = b.getTitle() != null && b.getTitle().toLowerCase().contains(query);
                boolean matchesAuthor = b.getAuthorHandle() != null
                        && b.getAuthorHandle().toLowerCase().contains(query);
                boolean matchesTag = false;
                if (b.getTags() != null) {
                    for (String t : b.getTags()) {
                        if (t.toLowerCase().contains(query)) {
                            matchesTag = true;
                            break;
                        }
                    }
                }
                boolean matchesId = String.valueOf(b.getId()).contains(query);
                if (matchesTitle || matchesAuthor || matchesTag || matchesId) {
                    finalResult.add(b);
                }
            }
        } else {
            finalResult = workingList;
        }

        lastFilteredResult = finalResult;
        blogAdapter.setFriendHandles(friends);
        blogAdapter.setReadBlogIds(readIds);

        if (readFilterId == R.id.chipBlogReadUnreadOnly) {
            binding.tvBlogCount.setText(finalResult.size() + " unread");
        } else if (readFilterId == R.id.chipBlogReadCompletedOnly) {
            binding.tvBlogCount.setText(finalResult.size() + " read");
        } else {
            binding.tvBlogCount.setText(finalResult.size() + " blogs");
        }

        if (finalResult.isEmpty()) {
            blogAdapter.setBlogs(new ArrayList<>());
            binding.btnLoadMoreBlogs.setVisibility(View.GONE);
            binding.layoutBlogsEmpty.setVisibility(View.VISIBLE);
            if (isFriendsFilter) {
                if (friends.isEmpty()) {
                    binding.tvBlogsEmpty.setText(
                            "You haven't added any friends yet.\nAdd Codeforces handles of authors you want to follow!");
                } else if (isFriendsLoading) {
                    binding.tvBlogsEmpty.setText("Loading blogs from your friends...");
                } else {
                    binding.tvBlogsEmpty.setText("No blogs found from your friends:\n" + String.join(", ", friends));
                }
                binding.btnEmptyAddFriends.setVisibility(View.VISIBLE);
            } else if (readFilterId == R.id.chipBlogReadCompletedOnly) {
                binding.tvBlogsEmpty.setText("No read blogs yet.\nOpen or check off blogs to mark them as read!");
                binding.btnEmptyAddFriends.setVisibility(View.GONE);
            } else if (readFilterId == R.id.chipBlogReadUnreadOnly) {
                binding.tvBlogsEmpty.setText("All caught up!\nNo unread blogs match your current filter.");
                binding.btnEmptyAddFriends.setVisibility(View.GONE);
            } else {
                binding.tvBlogsEmpty.setText("No blogs match your filter");
                binding.btnEmptyAddFriends.setVisibility(View.GONE);
            }
        } else {
            binding.layoutBlogsEmpty.setVisibility(View.GONE);
            renderPage();
        }
    }

    private void renderPage() {
        if (lastFilteredResult == null || lastFilteredResult.isEmpty()) {
            blogAdapter.setBlogs(new ArrayList<>());
            binding.btnLoadMoreBlogs.setVisibility(View.GONE);
            return;
        }
        int count = Math.min(displayedCount, lastFilteredResult.size());
        blogAdapter.setBlogs(new ArrayList<>(lastFilteredResult.subList(0, count)));
        if (count < lastFilteredResult.size()) {
            int rem = lastFilteredResult.size() - count;
            binding.btnLoadMoreBlogs.setText("Load More (" + rem + " remaining)");
            binding.btnLoadMoreBlogs.setVisibility(View.VISIBLE);
        } else {
            binding.btnLoadMoreBlogs.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (blogAdapter != null && prefsRepo != null && binding != null) {
            blogAdapter.setReadBlogIds(prefsRepo.getReadBlogIds());
            blogAdapter.setFriendHandles(prefsRepo.getFriends());
            if (binding.chipGroupBlogs.getCheckedChipId() == R.id.chipBlogFriends) {
                binding.layoutFriendSelector.setVisibility(View.VISIBLE);
                rebuildFriendSubChips();
            } else {
                binding.layoutFriendSelector.setVisibility(View.GONE);
            }
            int readFilterId = binding.chipGroupBlogReadStatus.getCheckedChipId();
            if (readFilterId != R.id.chipBlogReadAll) {
                applyFilters();
            } else {
                blogAdapter.notifyDataSetChanged();
            }
        }
    }

    private void showManageFriendsDialog() {
        if (!isAdded())
            return;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_manage_friends, null, false);
        TextInputEditText etHandle = dialogView.findViewById(R.id.etDialogFriendHandle);
        View btnAdd = dialogView.findViewById(R.id.btnDialogAddFriend);
        ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroupDialogFriends);
        TextView tvNoFriends = dialogView.findViewById(R.id.tvDialogNoFriends);
        View btnComparison = dialogView.findViewById(R.id.btnDialogViewComparison);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Done", null)
                .create();

        Runnable refreshChips = () -> {
            chipGroup.removeAllViews();
            Set<String> currentFriends = prefsRepo.getFriends();
            if (currentFriends.isEmpty()) {
                tvNoFriends.setVisibility(View.VISIBLE);
            } else {
                tvNoFriends.setVisibility(View.GONE);
                for (String friend : currentFriends) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(friend);
                    chip.setCloseIconVisible(true);
                    chip.setOnCloseIconClickListener(v -> {
                        prefsRepo.removeFriend(friend);
                        chipGroup.removeView(chip);
                        blogAdapter.setFriendHandles(prefsRepo.getFriends());
                        friendsBlogs.clear();
                        if (binding != null && binding.chipGroupBlogs.getCheckedChipId() == R.id.chipBlogFriends) {
                            rebuildFriendSubChips();
                            loadFriendsBlogs(true);
                        } else {
                            applyFilters();
                        }
                        if (prefsRepo.getFriends().isEmpty()) {
                            tvNoFriends.setVisibility(View.VISIBLE);
                        }
                    });
                    chipGroup.addView(chip);
                }
            }
        };

        refreshChips.run();

        btnAdd.setOnClickListener(v -> {
            String handle = etHandle.getText() != null ? etHandle.getText().toString().trim() : "";
            if (!handle.isEmpty()) {
                prefsRepo.addFriend(handle);
                etHandle.setText("");
                refreshChips.run();
                blogAdapter.setFriendHandles(prefsRepo.getFriends());
                friendsBlogs.clear();
                if (binding != null && binding.chipGroupBlogs.getCheckedChipId() == R.id.chipBlogFriends) {
                    rebuildFriendSubChips();
                    loadFriendsBlogs(true);
                } else {
                    applyFilters();
                }
                Toast.makeText(requireContext(), "Added " + handle + " to friends", Toast.LENGTH_SHORT).show();
            }
        });

        btnComparison.setOnClickListener(v -> {
            dialog.dismiss();
            Navigation.findNavController(requireView()).navigate(R.id.friendsFragment);
        });

        dialog.show();
    }

    // ---------------------------------------------------------------------
    // Friend Picker Dialog – allows choosing a specific friend to view blogs
    // ---------------------------------------------------------------------
    private void showFriendPickerDialog() {
        Set<String> friends = prefsRepo.getFriends();
        if (friends == null || friends.isEmpty()) {
            Toast.makeText(requireContext(), "No friends added. Add friends first.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> options = new ArrayList<>();
        options.add("All Friends");
        options.addAll(friends);
        String[] optionsArray = options.toArray(new String[0]);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Friend's Blogs")
                .setItems(optionsArray, (dialog, which) -> {
                    if (which == 0) {
                        selectedFriendHandle = null;
                    } else {
                        selectedFriendHandle = optionsArray[which];
                    }
                    displayedCount = PAGE_SIZE;
                    rebuildFriendSubChips();
                    applyFilters();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
