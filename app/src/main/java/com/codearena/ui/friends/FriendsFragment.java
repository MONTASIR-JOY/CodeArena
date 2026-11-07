package com.codearena.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.codearena.data.model.CfUser;
import com.codearena.data.repository.CodeforcesRepository;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentFriendsBinding;
import com.codearena.ui.adapter.FriendAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FriendsFragment extends Fragment {

    private FragmentFriendsBinding binding;
    private CodeforcesRepository cfRepo;
    private UserPreferencesRepository prefsRepo;
    private FriendAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cfRepo = new CodeforcesRepository(requireContext());
        prefsRepo = new UserPreferencesRepository(requireContext());

        adapter = new FriendAdapter(user -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Remove Friend")
                    .setMessage("Remove '" + user.getHandle() + "' from friends?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        prefsRepo.removeFriend(user.getHandle());
                        loadFriends();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.rvFriends.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFriends.setAdapter(adapter);

        binding.btnAddFriend.setOnClickListener(v -> {
            String handle = binding.etFriendHandle.getText() != null ?
                    binding.etFriendHandle.getText().toString().trim() : "";
            if (!handle.isEmpty()) {
                prefsRepo.addFriend(handle);
                binding.etFriendHandle.setText("");
                loadFriends();
            }
        });

        loadFriends();
    }

    private void loadFriends() {
        Set<String> friendHandles = prefsRepo.getFriends();
        if (friendHandles == null || friendHandles.isEmpty()) {
            adapter.setFriends(new ArrayList<>(), 0);
            return;
        }

        binding.progressFriends.setVisibility(View.VISIBLE);
        binding.tvFriendsError.setVisibility(View.GONE);

        cfRepo.fetchUsers(new ArrayList<>(friendHandles), new CodeforcesRepository.Callback<List<CfUser>>() {
            @Override
            public void onSuccess(List<CfUser> users) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    binding.progressFriends.setVisibility(View.GONE);
                    Collections.sort(users, (a, b) -> Integer.compare(
                            b.getRating() != null ? b.getRating() : 0,
                            a.getRating() != null ? a.getRating() : 0
                    ));
                    adapter.setFriends(users, 1500);
                });
            }

            @Override
            public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    binding.progressFriends.setVisibility(View.GONE);
                    binding.tvFriendsError.setVisibility(View.VISIBLE);
                    binding.tvFriendsError.setText("Error loading friends: " + error.getMessage());
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

