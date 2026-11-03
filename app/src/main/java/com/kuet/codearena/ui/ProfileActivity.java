package com.kuet.codearena.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.kuet.codearena.data.remote.ApiClient;
import com.kuet.codearena.data.remote.model.CfApiResponse;
import com.kuet.codearena.data.remote.model.CfUser;
import com.kuet.codearena.data.repository.SyncRepository;
import com.kuet.codearena.databinding.ActivityProfileBinding;
import com.kuet.codearena.util.AppExecutors;
import com.kuet.codearena.util.AuthManager;
import com.kuet.codearena.util.PreferenceHelper;

import java.util.List;
import java.util.Locale;

import retrofit2.Response;

/**
 * Activity presenting user identity, platform handle linking, verification checks, and practice preferences.
 */
public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private AuthManager authManager;
    private PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = AuthManager.getInstance(this);
        preferenceHelper = new PreferenceHelper(this);

        setupViews();
        loadUserData();
    }

    private void setupViews() {
        binding.toolbarProfile.setNavigationOnClickListener(v -> finish());

        binding.btnSignOut.setOnClickListener(v -> {
            authManager.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        binding.sliderDailyTarget.addOnChangeListener((slider, value, fromUser) -> {
            int target = (int) value;
            binding.tvDailyTargetLabel.setText(String.format(Locale.getDefault(), "Target: %d problems / day", target));
            preferenceHelper.setDailyTarget(target);
        });

        binding.btnVerifyCf.setOnClickListener(v -> {
            String handle = binding.etCfHandle.getText() != null ? binding.etCfHandle.getText().toString().trim() : "";
            if (!handle.isEmpty()) {
                verifyCodeforcesHandle(handle);
            } else {
                binding.etCfHandle.setError("Enter a handle");
            }
        });

        binding.btnVerifyAtCoder.setOnClickListener(v -> {
            String handle = binding.etAtCoderHandle.getText() != null ? binding.etAtCoderHandle.getText().toString().trim() : "";
            if (!handle.isEmpty()) {
                verifyAtCoderHandle(handle);
            } else {
                binding.etAtCoderHandle.setError("Enter AtCoder username");
            }
        });

        binding.btnVerifyLeetCode.setOnClickListener(v -> {
            String handle = binding.etLeetCodeHandle.getText() != null ? binding.etLeetCodeHandle.getText().toString().trim() : "";
            if (!handle.isEmpty()) {
                verifyLeetCodeHandle(handle);
            } else {
                binding.etLeetCodeHandle.setError("Enter LeetCode username");
            }
        });

        binding.btnSaveHandles.setOnClickListener(v -> {
            String cf = binding.etCfHandle.getText() != null ? binding.etCfHandle.getText().toString().trim() : "";
            String at = binding.etAtCoderHandle.getText() != null ? binding.etAtCoderHandle.getText().toString().trim() : "";
            String lc = binding.etLeetCodeHandle.getText() != null ? binding.etLeetCodeHandle.getText().toString().trim() : "";

            preferenceHelper.setCfHandle(cf);
            preferenceHelper.setAtCoderHandle(at);
            preferenceHelper.setLeetCodeHandle(lc);

            Snackbar.make(binding.profileCoordinator, "Handles saved. Initiating background solve sync...", Snackbar.LENGTH_SHORT).show();
            SyncRepository.getInstance(this).syncAllPlatformsDirectly();
        });
    }

    private void loadUserData() {
        binding.tvProfileName.setText(authManager.getUserDisplayName());
        binding.tvProfileEmail.setText(authManager.getUserEmail());

        binding.etCfHandle.setText(preferenceHelper.getCfHandle());
        binding.etAtCoderHandle.setText(preferenceHelper.getAtCoderHandle());
        binding.etLeetCodeHandle.setText(preferenceHelper.getLeetCodeHandle());

        int currentTarget = preferenceHelper.getDailyTarget();
        binding.sliderDailyTarget.setValue((float) currentTarget);
        binding.tvDailyTargetLabel.setText(String.format(Locale.getDefault(), "Target: %d problems / day", currentTarget));
    }

    private void verifyCodeforcesHandle(String handle) {
        binding.btnVerifyCf.setEnabled(false);
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                Response<CfApiResponse<List<CfUser>>> response =
                        ApiClient.getCodeforcesApi().getUserInfo(handle).execute();

                AppExecutors.getInstance().mainThread().execute(() -> {
                    binding.btnVerifyCf.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                        List<CfUser> list = response.body().getResult();
                        if (list != null && !list.isEmpty()) {
                            CfUser user = list.get(0);
                            String rank = user.getRank() != null ? user.getRank() : "unrated";
                            int rating = user.getRating() != null ? user.getRating() : 0;
                            Snackbar.make(binding.profileCoordinator,
                                    "Verified @" + user.getHandle() + " (" + rank + ", " + rating + ")",
                                    Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        Snackbar.make(binding.profileCoordinator, "User @" + handle + " not found on Codeforces", Snackbar.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                AppExecutors.getInstance().mainThread().execute(() -> {
                    binding.btnVerifyCf.setEnabled(true);
                    Snackbar.make(binding.profileCoordinator, "Verification error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void verifyAtCoderHandle(String handle) {
        binding.btnVerifyAtCoder.setEnabled(false);
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                com.kuet.codearena.data.remote.AtCoderPlatformSource source = new com.kuet.codearena.data.remote.AtCoderPlatformSource();
                List<com.kuet.codearena.data.db.entity.SolveEntity> list = source.fetchUserAcceptedSubmissions(handle);
                AppExecutors.getInstance().mainThread().execute(() -> {
                    binding.btnVerifyAtCoder.setEnabled(true);
                    Snackbar.make(binding.profileCoordinator,
                            "Verified AtCoder @" + handle + " (" + list.size() + " AC submissions found)",
                            Snackbar.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                AppExecutors.getInstance().mainThread().execute(() -> {
                    binding.btnVerifyAtCoder.setEnabled(true);
                    Snackbar.make(binding.profileCoordinator, "AtCoder verify failed: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void verifyLeetCodeHandle(String handle) {
        binding.btnVerifyLeetCode.setEnabled(false);
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                com.kuet.codearena.data.remote.LeetCodePlatformSource source = new com.kuet.codearena.data.remote.LeetCodePlatformSource();
                List<com.kuet.codearena.data.db.entity.SolveEntity> list = source.fetchUserAcceptedSubmissions(handle);
                AppExecutors.getInstance().mainThread().execute(() -> {
                    binding.btnVerifyLeetCode.setEnabled(true);
                    Snackbar.make(binding.profileCoordinator,
                            "Verified LeetCode @" + handle + " (" + list.size() + " recent AC submissions found)",
                            Snackbar.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                AppExecutors.getInstance().mainThread().execute(() -> {
                    binding.btnVerifyLeetCode.setEnabled(true);
                    Snackbar.make(binding.profileCoordinator, "LeetCode verify failed: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                });
            }
        });
    }
}

