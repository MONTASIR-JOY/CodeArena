package com.codearena.ui.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.codearena.R;
import com.codearena.data.model.CfUser;
import com.codearena.data.repository.CodeforcesRepository;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentSetupBinding;
import com.codearena.util.RankColors;

public class SetupFragment extends Fragment {

    private FragmentSetupBinding binding;
    private CodeforcesRepository cfRepo;
    private UserPreferencesRepository prefsRepo;
    private CfUser verifiedUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSetupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cfRepo = new CodeforcesRepository(requireContext());
        prefsRepo = new UserPreferencesRepository(requireContext());

        binding.btnValidate.setOnClickListener(v -> validateHandle());
        binding.btnConfirm.setOnClickListener(v -> confirmAndProceed());
    }

    private void validateHandle() {
        String handle = binding.etHandle.getText() != null ?
                binding.etHandle.getText().toString().trim() : "";
        if (handle.isEmpty()) {
            binding.cardError.setVisibility(View.VISIBLE);
            binding.tvErrorMessage.setText("Please enter a Codeforces handle");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.cardError.setVisibility(View.GONE);
        binding.cardProfilePreview.setVisibility(View.GONE);

        cfRepo.validateAndFetchUser(handle, new CodeforcesRepository.Callback<CfUser>() {
            @Override
            public void onSuccess(CfUser user) {
                if (!isAdded()) return;
                verifiedUser = user;
                requireActivity().runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.cardProfilePreview.setVisibility(View.VISIBLE);

                    int rankColor = RankColors.getColorForRating(user.getRating());
                    binding.tvPreviewHandle.setText(user.getHandle());
                    binding.tvPreviewHandle.setTextColor(rankColor);
                    binding.tvPreviewRank.setText(RankColors.getTitleForRating(user.getRating()));
                    binding.tvPreviewRank.setTextColor(rankColor);
                    binding.tvPreviewRating.setText(String.valueOf(user.getRating() != null ? user.getRating() : 0));
                    binding.tvPreviewMaxRating.setText(String.valueOf(user.getMaxRating() != null ? user.getMaxRating() : 0));
                });
            }

            @Override
            public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.cardError.setVisibility(View.VISIBLE);
                    binding.tvErrorMessage.setText(error.getMessage());
                });
            }
        });
    }

    private void confirmAndProceed() {
        if (verifiedUser != null) {
            prefsRepo.saveHandle(verifiedUser.getHandle());
            Navigation.findNavController(requireView()).navigate(R.id.nav_home);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

