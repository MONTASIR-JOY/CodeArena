package com.kuet.codearena.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kuet.codearena.databinding.ActivityLoginBinding;
import com.kuet.codearena.util.AuthManager;

/**
 * Activity managing Google Sign-In with Firebase Authentication and guest offline mode entry.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private ActivityLoginBinding binding;
    private AuthManager authManager;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                } else {
                    Snackbar.make(binding.getRoot(), "Google Sign-In cancelled or failed", Snackbar.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = AuthManager.getInstance(this);

        setupAuthOptions();
    }

    private void setupAuthOptions() {
        if (!authManager.isConfigured()) {
            // Firebase is not configured or google-services.json is missing/placeholder
            binding.btnGoogleSignIn.setVisibility(View.GONE);
            binding.tvCloudDisabledNotice.setVisibility(View.VISIBLE);
        } else {
            binding.btnGoogleSignIn.setVisibility(View.VISIBLE);
            binding.tvCloudDisabledNotice.setVisibility(View.GONE);

            binding.btnGoogleSignIn.setOnClickListener(v -> {
                if (authManager.getGoogleSignInClient() != null) {
                    Intent signInIntent = authManager.getGoogleSignInClient().getSignInIntent();
                    googleSignInLauncher.launch(signInIntent);
                } else {
                    Snackbar.make(binding.getRoot(), "Google Sign-In is unavailable", Snackbar.LENGTH_LONG).show();
                }
            });
        }

        binding.btnContinueGuest.setOnClickListener(v -> navigateToMain());
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null && authManager.getFirebaseAuth() != null) {
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                authManager.getFirebaseAuth().signInWithCredential(credential)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInWithCredential:success");
                                navigateToMain();
                            } else {
                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                                Snackbar.make(binding.getRoot(), "Firebase Authentication failed", Snackbar.LENGTH_LONG).show();
                            }
                        });
            } else {
                // Local sign in fallback
                navigateToMain();
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Snackbar.make(binding.getRoot(), "Sign in failed (" + e.getStatusCode() + ")", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
