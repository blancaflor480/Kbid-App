package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupUser extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private EditText inputEmail, inputPassword,inputConfirmPassword;
    private Button buttonSignup, buttonCreate;
    private RelativeLayout googlesignIn;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private LottieAnimationView loader, noInternet;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_user);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonSignup = findViewById(R.id.buttonSignup);
        buttonCreate = findViewById(R.id.buttonCreate);
        googlesignIn = findViewById(R.id.googlesignIn);
        loader = findViewById(R.id.loader);
        noInternet = findViewById(R.id.nointernet);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Use your web client ID from Firebase console
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googlesignIn.setOnClickListener(view -> signInWithGoogle());
        buttonSignup.setOnClickListener(v -> registerUser());
        buttonCreate.setOnClickListener(v -> startActivity(new Intent(SignupUser.this, LoginUser.class)));
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign-In failed
                Log.w("SignupUser", "Google sign in failed", e);
                Toast.makeText(SignupUser.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = auth.getCurrentUser();
                        Log.d("SignupUser", "signInWithCredential:success");

                        // Check if the user already exists in Firestore
                        firestore.collection("user").document(user.getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        // User already exists
                                        Toast.makeText(SignupUser.this, "Account already exists. Please log in.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // New user, proceed with adding to Firestore
                                        addUserToFirestore(user, null);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("SignupUser", "Error checking Firestore: " + e.getMessage());
                                    Toast.makeText(SignupUser.this, "Error checking account. Please try again.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // If sign in fails
                        Log.w("SignupUser", "signInWithCredential:failure", task.getException());
                        Toast.makeText(SignupUser.this, "Google Sign-In failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String confirmPassword = inputConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            showValidationDialog("Enter email address!");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showValidationDialog("Enter password!");
            return;
        }

        if (password.length() < 6) {
            showValidationDialog("Password too short, enter minimum 6 characters!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showValidationDialog("Passwords do not match. Please re-enter.");
            return;
        }

        if (!isConnected()) {
            showNoInternet();
            return;
        }

        // Show loader and disable button
        showLoader();

        // Create user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = auth.getCurrentUser();
                        sendVerificationEmail(user);  // Send verification email
                    } else {
                        // If sign in fails
                        hideLoader();
                        showValidationDialog("Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupUser.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            addUserToFirestore(user, null);
                        } else {
                            showValidationDialog("Failed to send verification email.");
                            hideLoader();
                        }
                    });
        }
    }

    private void addUserToFirestore(FirebaseUser user, String password) {
        if (user == null) {
            showValidationDialog("User is null. Cannot add to Firestore.");
            hideLoader();
            return;
        }

        // Create a new user map
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("uid", user.getUid());
        if (password != null) {
            userData.put("password", password); // Only store password for manual sign-up
        }

        // Add user data to Firestore
        firestore.collection("user").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SignupUser", "User added to Firestore successfully.");
                    showSuccessMessage();
                })
                .addOnFailureListener(e -> {
                    Log.w("SignupUser", "Error adding user to Firestore", e);
                    hideLoader();
                    showValidationDialog("Failed to add user to Firestore: " + e.getMessage());
                });
    }

    private void showLoader() {
        buttonSignup.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        noInternet.setVisibility(View.GONE);
    }

    private void hideLoader() {
        loader.setVisibility(View.GONE);
        buttonSignup.setVisibility(View.VISIBLE);
    }

    private void showSuccessMessage() {
        // Show loader animation for successful registration
        loader.setAnimation(R.raw.registeredsucess);
        loader.setVisibility(View.VISIBLE);
        Toast.makeText(SignupUser.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
        hideLoader(); // Assuming you want to hide this loader after a short time

        // Check if the current user is verified
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified()) {
                navigateToChildname(); // Navigate if email is verified
            } else {
                // Show a message if email is not verified
                Toast.makeText(this, "Please verify your email to proceed.", Toast.LENGTH_LONG).show();

                // Set the content view to loading verification layout
                setContentView(R.layout.loading_verification); // Your loading verification layout XML
                // Show loading animation
                loader.setAnimation(R.raw.openemail); // Replace with your loading animation
                loader.setVisibility(View.VISIBLE); // Make the loader visible

                // Optionally, you can add a delay or a listener to check for email verification
                // For example, after a few seconds, you might want to check if the email is verified again
                new Handler().postDelayed(() -> {
                    // Recheck email verification
                    FirebaseUser recheckedUser = auth.getCurrentUser();
                    if (recheckedUser != null && recheckedUser.isEmailVerified()) {
                        navigateToChildname(); // Navigate if email is verified
                    } else {
                        // Optionally, show a message or stay on the current screen
                        Toast.makeText(this, "Still waiting for email verification...", Toast.LENGTH_LONG).show();
                    }
                }, 1000); // Adjust delay as necessary
            }
        }
    }

    private void showValidationDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupUser.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_authenticateaccount, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView messageTextView = dialogView.findViewById(R.id.message);
        messageTextView.setText(message);

        Button buttonOk = dialogView.findViewById(R.id.submit);
        buttonOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }




    @Override
    protected void onResume() {
        super.onResume();
        checkEmailVerification();
    }

    private void checkEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        // Email is verified, navigate to the next activity
                        navigateToChildname();

                    } else {
                        // Optionally inform the user
                        Toast.makeText(this, "Email not verified yet.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void showNoInternet() {
        noInternet.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        buttonSignup.setVisibility(View.GONE);
    }

    private void navigateToChildname() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(SignupUser.this, ChildNameActivity.class);
            intent.putExtra("USER_EMAIL", user.getEmail()); // Pass the verified email
            startActivity(intent);
            finish();
        }
    }
}
