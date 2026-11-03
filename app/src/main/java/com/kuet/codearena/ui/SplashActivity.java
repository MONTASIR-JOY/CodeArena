package com.kuet.codearena.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.kuet.codearena.databinding.ActivitySplashBinding;
import com.kuet.codearena.util.AuthManager;

/**
 * Splash screen displaying app branding and routing user to login or main dashboard.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AuthManager auth = AuthManager.getInstance(this);
            Intent nextIntent;
            if (auth.isSignedIn()) {
                nextIntent = new Intent(this, MainActivity.class);
            } else {
                nextIntent = new Intent(this, LoginActivity.class);
            }
            startActivity(nextIntent);
            finish();
        }, 600);
    }
}
