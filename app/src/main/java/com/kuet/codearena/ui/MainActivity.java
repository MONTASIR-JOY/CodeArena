package com.kuet.codearena.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.kuet.codearena.R;
import com.kuet.codearena.data.repository.SyncRepository;
import com.kuet.codearena.databinding.ActivityMainBinding;

/**
 * Main activity hosting bottom navigation, runtime notification permissions, and top-level tab fragments.
 */
public class MainActivity extends AppCompatActivity {
    private static final String KEY_SELECTED_TAB = "key_selected_tab";
    private static final int PERMISSION_REQ_NOTIFICATIONS = 101;

    private ActivityMainBinding binding;
    private int currentTabId = R.id.navigation_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestNotificationPermissionIfNeeded();

        binding.btnToolbarProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        binding.bottomNav.setOnItemSelectedListener(item -> {
            currentTabId = item.getItemId();
            return switchTab(currentTabId);
        });

        if (savedInstanceState != null) {
            currentTabId = savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.navigation_home);
            binding.bottomNav.setSelectedItemId(currentTabId);
        } else {
            switchTab(R.id.navigation_home);
        }

        // Trigger background solve sync on app open & schedule daily sync
        SyncRepository.getInstance(this).syncAllPlatformsDirectly();
        com.kuet.codearena.util.AlarmScheduler.scheduleDailySyncAlarm(this);
    }

    private boolean switchTab(int tabId) {
        Fragment selectedFragment;
        if (tabId == R.id.navigation_contests) {
            selectedFragment = new ContestsFragment();
            binding.mainToolbar.setTitle("Codeforces Contests");
        } else if (tabId == R.id.navigation_calendar) {
            selectedFragment = new CalendarFragment();
            binding.mainToolbar.setTitle("Contest Calendar");
        } else if (tabId == R.id.navigation_practice) {
            selectedFragment = new PracticeFragment();
            binding.mainToolbar.setTitle("Tag Mastery & Ladder");
        } else if (tabId == R.id.navigation_leaderboard) {
            selectedFragment = new LeaderboardFragment();
            binding.mainToolbar.setTitle("Global Leaderboards");
        } else {
            selectedFragment = new HomeFragment();
            binding.mainToolbar.setTitle(R.string.app_name);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, selectedFragment)
                .commit();
        return true;
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQ_NOTIFICATIONS
                );
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, currentTabId);
    }
}

