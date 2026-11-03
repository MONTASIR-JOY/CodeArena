package com.kuet.codearena;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

/**
 * Custom Application class performing initial application setup and safe Firebase initialization.
 */
public class CodeArenaApp extends Application {
    private static final String TAG = "CodeArenaApp";
    private static boolean firebaseAvailable = false;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeFirebaseSafely();
    }

    private void initializeFirebaseSafely() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
            firebaseAvailable = FirebaseApp.getInstance() != null;
            Log.d(TAG, "Firebase initialized successfully. Available: " + firebaseAvailable);
        } catch (Exception e) {
            Log.w(TAG, "Firebase initialization skipped or unconfigured: " + e.getMessage());
            firebaseAvailable = false;
        }
    }

    public static boolean isFirebaseAvailable() {
        return firebaseAvailable;
    }
}
