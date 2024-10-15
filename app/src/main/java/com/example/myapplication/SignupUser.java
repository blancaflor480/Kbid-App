package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
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
    private EditText inputEmail, inputPassword;
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

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
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
                        addUserToFirestore(user, password);
                    } else {
                        // If sign in fails
                        hideLoader();
                        Toast.makeText(SignupUser.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addUserToFirestore(FirebaseUser user, String password) {
        if (user == null) {
            Toast.makeText(this, "User is null. Cannot add to Firestore.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SignupUser.this, "Failed to add user to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        loader.setAnimation(R.raw.registeredsucess);
        loader.setVisibility(View.VISIBLE);
        Toast.makeText(SignupUser.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
        hideLoader();
        navigateToHome();
    }

    private void showNoInternet() {
        noInternet.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        buttonSignup.setVisibility(View.VISIBLE);
        Toast.makeText(SignupUser.this, "No internet connection. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
    }

    private void navigateToHome() {
        Intent intent = new Intent(SignupUser.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
