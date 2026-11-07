package com.codearena;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.ActivityMainBinding;
import com.codearena.util.ThemeManager;

/**
 * Main application container hosting the NavHostFragment and BottomNavigationView.
 */
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(new UserPreferencesRepository(this).getTheme());
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Request runtime notification permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Handle bottom nav tab clicks
            binding.bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    navigateToHome();
                    return true;
                }

                if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == id) {
                    return true;
                }

                NavOptions options = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(R.id.nav_home, false)
                        .build();
                try {
                    navController.navigate(id, null, options);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });

            // Handle tab reselection (e.g. user is in a child screen of Home like Mastery or Daily Challenge,
            // and clicks the already-selected Home tab)
            binding.bottomNavigation.setOnItemReselectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    navigateToHome();
                } else {
                    if (navController.getCurrentDestination() != null &&
                        navController.getCurrentDestination().getId() != id) {
                        navController.popBackStack(id, false);
                    }
                }
            });

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                if (id == R.id.liveContestFragment || id == R.id.setupFragment || id == R.id.onboardingFragment) {
                    binding.bottomNavigation.setVisibility(View.GONE);
                } else {
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                }

                MenuItem menuItem = binding.bottomNavigation.getMenu().findItem(id);
                if (menuItem != null && !menuItem.isChecked()) {
                    menuItem.setChecked(true);
                }
            });
        }
    }

    private void navigateToHome() {
        if (navController == null) return;
        if (navController.getCurrentDestination() != null &&
            navController.getCurrentDestination().getId() == R.id.nav_home) {
            return;
        }
        boolean popped = navController.popBackStack(R.id.nav_home, false);
        if (!popped) {
            NavOptions options = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                    .build();
            try {
                navController.navigate(R.id.nav_home, null, options);
            } catch (Exception ignored) {}
        }
    }
}

