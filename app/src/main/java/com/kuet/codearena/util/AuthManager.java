package com.kuet.codearena.util;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kuet.codearena.CodeArenaApp;

/**
 * Manager coordinating Firebase Authentication, Google Sign-In clients, and guest fallback states.
 */
public class AuthManager {
    private static final String TAG = "AuthManager";
    private static volatile AuthManager INSTANCE;

    private final Context context;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private boolean configured = false;

    private AuthManager(Context context) {
        this.context = context.getApplicationContext();
        init();
    }

    public static AuthManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AuthManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AuthManager(context);
                }
            }
        }
        return INSTANCE;
    }

    private void init() {
        if (!CodeArenaApp.isFirebaseAvailable()) {
            configured = false;
            return;
        }

        try {
            firebaseAuth = FirebaseAuth.getInstance();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(context, gso);
            configured = true;
        } catch (Exception e) {
            Log.w(TAG, "Firebase Auth not configured or using placeholder credentials: " + e.getMessage());
            configured = false;
        }
    }

    public boolean isConfigured() {
        return configured && firebaseAuth != null;
    }

    public boolean isSignedIn() {
        return isConfigured() && firebaseAuth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        if (isConfigured()) {
            return firebaseAuth.getCurrentUser();
        }
        return null;
    }

    public String getUserDisplayName() {
        FirebaseUser user = getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            return user.getDisplayName();
        }
        return "Competitive Programmer";
    }

    public String getUserEmail() {
        FirebaseUser user = getCurrentUser();
        if (user != null && user.getEmail() != null) {
            return user.getEmail();
        }
        return "guest@codearena.local";
    }

    public String getUserPhotoUrl() {
        FirebaseUser user = getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            return user.getPhotoUrl().toString();
        }
        return null;
    }

    public String getUserId() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return "guest_user";
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }

    public void signOut() {
        if (isConfigured()) {
            try {
                firebaseAuth.signOut();
                if (googleSignInClient != null) {
                    googleSignInClient.signOut();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during signOut", e);
            }
        }
    }
}
