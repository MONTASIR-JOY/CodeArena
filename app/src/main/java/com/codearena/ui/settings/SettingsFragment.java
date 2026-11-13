package com.codearena.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.codearena.R;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentSettingsBinding;
import com.codearena.util.ThemeManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private UserPreferencesRepository prefsRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefsRepo = new UserPreferencesRepository(requireContext());

        setupAuthUI();
        setupHandleSetting();
        setupTargetSlider();
        setupSwitches();
        setupThemeToggle();
        setupNavigationShortcuts();
    }

    private void setupAuthUI() {
        String email = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        if (email != null && !email.isEmpty()) {
            binding.tvUserEmail.setText(email);
        } else {
            binding.tvUserEmail.setText("Not signed in");
        }

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent intent = new Intent(requireContext(), com.codearena.ui.auth.LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        });
    }

    private void setupHandleSetting() {
        updateAllHandlesUI();

        binding.btnChangeHandle.setOnClickListener(v ->
                showHandleDialog("Codeforces", "e.g. tourist, ecnerwala", prefsRepo.getHandle(), newHandle -> {
                    prefsRepo.saveHandle(newHandle);
                    updateAllHandlesUI();
                }));

        binding.btnChangeHandleLeetCode.setOnClickListener(v ->
                showHandleDialog("LeetCode", "e.g. leetcode_user", prefsRepo.getLeetCodeHandle(), newHandle -> {
                    prefsRepo.saveLeetCodeHandle(newHandle);
                    updateAllHandlesUI();
                }));

        binding.btnChangeHandleCodeChef.setOnClickListener(v ->
                showHandleDialog("CodeChef", "e.g. codechef_handle", prefsRepo.getCodeChefHandle(), newHandle -> {
                    prefsRepo.saveCodeChefHandle(newHandle);
                    updateAllHandlesUI();
                }));

        binding.btnChangeHandleAtCoder.setOnClickListener(v ->
                showHandleDialog("AtCoder", "e.g. chokudai", prefsRepo.getAtCoderHandle(), newHandle -> {
                    prefsRepo.saveAtCoderHandle(newHandle);
                    updateAllHandlesUI();
                }));

        binding.btnChangeHandleHackerRank.setOnClickListener(v ->
                showHandleDialog("HackerRank", "e.g. hackerrank_id", prefsRepo.getHackerRankHandle(), newHandle -> {
                    prefsRepo.saveHackerRankHandle(newHandle);
                    updateAllHandlesUI();
                }));

        binding.btnChangeHandleCses.setOnClickListener(v ->
                showHandleDialog("CSES", "e.g. user ID or handle", prefsRepo.getCsesHandle(), newHandle -> {
                    prefsRepo.saveCsesHandle(newHandle);
                    updateAllHandlesUI();
                }));
    }

    private void updateAllHandlesUI() {
        String cf = prefsRepo.getHandle();
        binding.tvSettingsHandle.setText(cf.isEmpty() ? "Not configured" : cf);

        String lc = prefsRepo.getLeetCodeHandle();
        binding.tvSettingsHandleLeetCode.setText(lc.isEmpty() ? "Not configured" : lc);

        String cc = prefsRepo.getCodeChefHandle();
        binding.tvSettingsHandleCodeChef.setText(cc.isEmpty() ? "Not configured" : cc);

        String ac = prefsRepo.getAtCoderHandle();
        binding.tvSettingsHandleAtCoder.setText(ac.isEmpty() ? "Not configured" : ac);

        String hr = prefsRepo.getHackerRankHandle();
        binding.tvSettingsHandleHackerRank.setText(hr.isEmpty() ? "Not configured" : hr);

        String cses = prefsRepo.getCsesHandle();
        binding.tvSettingsHandleCses.setText(cses.isEmpty() ? "Not configured" : cses);
    }

    private interface OnHandleSavedListener {
        void onSaved(String newHandle);
    }

    private void showHandleDialog(String platform, String hint, String currentHandle, OnHandleSavedListener listener) {
        View dialogContent = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_single_text_input, null, false);
        TextInputLayout inputLayout = dialogContent.findViewById(R.id.tilDialogInput);
        TextInputEditText input = dialogContent.findViewById(R.id.etDialogInput);
        inputLayout.setHint(hint);
        input.setText(currentHandle != null ? currentHandle : "");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(platform + " Handle")
                .setMessage("Enter your official " + platform + " handle / username:")
                .setView(dialogContent)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newHandle = input.getText() != null ? input.getText().toString().trim() : "";
                    listener.onSaved(newHandle);
                    Toast.makeText(requireContext(), platform + " handle saved!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupTargetSlider() {
        int current = prefsRepo.getDailyTarget();
        binding.sliderDailyTarget.setValue(Math.max(1, Math.min(10, current)));
        binding.tvDailyTarget.setText(current + " problems / day");

        binding.sliderDailyTarget.addOnChangeListener((slider, value, fromUser) -> {
            int target = (int) value;
            binding.tvDailyTarget.setText(target + " problems / day");
            prefsRepo.saveDailyTarget(target);
        });
    }

    private void setupSwitches() {
        binding.switchNotifications.setChecked(prefsRepo.isNotificationsEnabled());
        binding.switchNotifications.setOnCheckedChangeListener((btn, checked) ->
                prefsRepo.setNotificationsEnabled(checked));

        binding.switchMilestones.setChecked(prefsRepo.isMilestoneAlertsEnabled());
        binding.switchMilestones.setOnCheckedChangeListener((btn, checked) ->
                prefsRepo.setMilestoneAlertsEnabled(checked));

        binding.switchFastReview.setChecked(prefsRepo.isFastReviewDebugMode());
        binding.switchFastReview.setOnCheckedChangeListener((btn, checked) ->
                prefsRepo.setFastReviewDebugMode(checked));
    }

    private void setupThemeToggle() {
        String theme = prefsRepo.getTheme();
        if ("light".equalsIgnoreCase(theme)) {
            binding.toggleTheme.check(R.id.btnThemeLight);
        } else if ("dark".equalsIgnoreCase(theme)) {
            binding.toggleTheme.check(R.id.btnThemeDark);
        } else {
            binding.toggleTheme.check(R.id.btnThemeSystem);
        }

        binding.toggleTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String newTheme = "system";
            if (checkedId == R.id.btnThemeLight) newTheme = "light";
            else if (checkedId == R.id.btnThemeDark) newTheme = "dark";
            prefsRepo.saveTheme(newTheme);
            ThemeManager.apply(newTheme);
        });
    }

    private void setupNavigationShortcuts() {
        binding.btnOpenMastery.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.masteryFragment));

        binding.btnOpenHistory.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.historyFragment));

        binding.btnOpenFriends.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.friendsFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
