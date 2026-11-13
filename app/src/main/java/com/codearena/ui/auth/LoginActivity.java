package com.codearena.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.codearena.MainActivity;
import com.codearena.R;
import com.codearena.data.repository.FirestoreSyncRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        com.google.firebase.auth.FirebaseUser existingUser = auth.getCurrentUser();

        EditText emailEt = findViewById(R.id.etEmail);
        EditText passEt = findViewById(R.id.etPassword);
        Button signInBtn = findViewById(R.id.btnSignIn);
        Button registerBtn = findViewById(R.id.btnRegister);
        Button googleBtn = findViewById(R.id.btnGoogleSignIn);
        TextView forgotTv = findViewById(R.id.tvForgotPassword);

        signInBtn.setOnClickListener(v -> {
            String email = emailEt.getText().toString().trim();
            String pwd = passEt.getText().toString();
            if (email.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            com.google.firebase.auth.FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                continueWithVerifiedEmail(user, email);
                            } else {
                                Toast.makeText(this, "Could not load the signed-in account.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String msg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Login failed: " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        registerBtn.setOnClickListener(v -> {
            String email = emailEt.getText().toString().trim();
            String pwd = passEt.getText().toString();
            if (email.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            com.google.firebase.auth.FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification().addOnCompleteListener(this, verifyTask -> {
                                    if (!verifyTask.isSuccessful()) {
                                        String msg = verifyTask.getException() != null
                                                ? verifyTask.getException().getMessage()
                                                : "Unknown error";
                                        Toast.makeText(this, "Account created, but verification email failed: " + msg,
                                                Toast.LENGTH_LONG).show();
                                    }
                                    showVerificationDialog(user, email);
                                });
                            } else {
                                Toast.makeText(this, "Account created, but could not load the account. Please sign in.",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String msg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Register failed: " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        forgotTv.setOnClickListener(v -> {
            View dialogContent = getLayoutInflater()
                    .inflate(R.layout.dialog_single_text_input, null, false);
            TextInputLayout inputLayout = dialogContent.findViewById(R.id.tilDialogInput);
            TextInputEditText input = dialogContent.findViewById(R.id.etDialogInput);
            inputLayout.setHint("Email address");
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Reset password")
                    .setMessage("Enter the email address linked to your account.")
                    .setView(dialogContent)
                    .setPositiveButton("Send", (dialog, which) -> {
                        String email = input.getText() != null ? input.getText().toString().trim() : "";
                        if (!email.isEmpty()) {
                            auth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(this, "Reset email sent to " + email, Toast.LENGTH_SHORT).show();
                                        } else {
                                            String msg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                            Toast.makeText(this, "Error: " + msg, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Configure Google Sign-In
        String webClientId = getString(R.string.default_web_client_id);
        GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail();

        if (webClientId != null && !webClientId.isEmpty() && !webClientId.startsWith("YOUR_WEB_CLIENT_ID")) {
            gsoBuilder.requestIdToken(webClientId);
        }

        googleSignInClient = GoogleSignIn.getClient(this, gsoBuilder.build());

        googleBtn.setOnClickListener(v -> {
            String clientId = getString(R.string.default_web_client_id);
            if (clientId.startsWith("YOUR_WEB_CLIENT_ID")) {
                Toast.makeText(this, "Web Client ID not set in strings.xml", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Please add default_web_client_id from Firebase Console to strings.xml");
                return;
            }
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        });

        if (existingUser != null) {
            if (isEmailPasswordUser(existingUser)) {
                existingUser.reload().addOnCompleteListener(this, reloadTask -> {
                    com.google.firebase.auth.FirebaseUser currentUser = auth.getCurrentUser();
                    if (currentUser == null) return;
                    if (reloadTask.isSuccessful() && currentUser.isEmailVerified()) {
                        FirestoreSyncRepository.pullFromFirestore(this, success -> goToMain());
                    } else {
                        auth.signOut();
                        Toast.makeText(this, "Please verify your email, then sign in.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                FirestoreSyncRepository.pullFromFirestore(this, success -> goToMain());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Log.e(TAG, "Google Sign-In failed: No ID Token returned");
                    Toast.makeText(this, "Google Sign-In: No ID token returned", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                Log.e(TAG, "Google Sign-In failed code: " + e.getStatusCode(), e);
                if (e.getStatusCode() == 10) {
                    Toast.makeText(this, "Developer Error (10): Add SHA-1 to Firebase Console!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Google Sign-In failed (" + e.getStatusCode() + ")", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirestoreSyncRepository.pullFromFirestore(this, success -> goToMain());
                    } else {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Firebase Auth failed: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean isEmailPasswordUser(com.google.firebase.auth.FirebaseUser user) {
        for (com.google.firebase.auth.UserInfo provider : user.getProviderData()) {
            if ("password".equals(provider.getProviderId())) return true;
        }
        return false;
    }

    private void continueWithVerifiedEmail(com.google.firebase.auth.FirebaseUser user, String email) {
        user.reload().addOnCompleteListener(this, reloadTask -> {
            if (reloadTask.isSuccessful() && user.isEmailVerified()) {
                FirestoreSyncRepository.pullFromFirestore(this, success -> goToMain());
            } else {
                showVerificationDialog(user, email);
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showVerificationDialog(com.google.firebase.auth.FirebaseUser user, String email) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_email_verification, null);
        TextView subtitleTv = dialogView.findViewById(R.id.tvVerificationSubtitle);
        subtitleTv.setText("Check the inbox or spam folder for " + email + ", open the verification link, then tap “I've Verified”.");

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialogView.findViewById(R.id.btnVerified).setOnClickListener(v -> {
            user.reload().addOnCompleteListener(this, reloadTask -> {
                if (reloadTask.isSuccessful() && user.isEmailVerified()) {
                    Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    FirestoreSyncRepository.pullFromFirestore(this, success -> goToMain());
                } else if (!reloadTask.isSuccessful()) {
                    Toast.makeText(this, "Could not check verification. Check your connection and try again.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Email not verified yet. Please check your inbox.", Toast.LENGTH_LONG).show();
                }
            });
        });

        dialogView.findViewById(R.id.btnResend).setOnClickListener(v -> {
            user.sendEmailVerification().addOnCompleteListener(resendTask -> {
                if (resendTask.isSuccessful()) {
                    Toast.makeText(this, "Verification link resent to " + email, Toast.LENGTH_SHORT).show();
                } else {
                    String msg = resendTask.getException() != null
                            ? resendTask.getException().getMessage()
                            : "Unknown error";
                    Toast.makeText(this, "Failed to resend email: " + msg, Toast.LENGTH_LONG).show();
                }
            });
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            auth.signOut();
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }
}
