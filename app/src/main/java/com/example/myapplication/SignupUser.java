package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupUser extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button buttonSignup, buttonCreate;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private LottieAnimationView loader, noInternet;

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
        loader = findViewById(R.id.loader);
        noInternet = findViewById(R.id.nointernet);

        buttonSignup.setOnClickListener(v -> registerUser());
        buttonCreate.setOnClickListener(v -> startActivity(new Intent(SignupUser.this, LoginUser.class)));
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
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
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SignupUser", "createUserWithEmail:success");
                        FirebaseUser user = auth.getCurrentUser();
                        addUserToFirestore(user, password);
                    } else {
                        // If sign in fails, hide loader and reset button
                        Log.w("SignupUser", "createUserWithEmail:failure", task.getException());
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
        userData.put("password", password); // Store password (not recommended for production)

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

    private void navigateToHome() {
        Intent intent = new Intent(SignupUser.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close this activity
    }

    private void showLoader() {
        buttonSignup.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        noInternet.setVisibility(View.GONE); // Ensure no internet animation is hidden
    }

    private void hideLoader() {
        loader.setVisibility(View.GONE);
        buttonSignup.setVisibility(View.VISIBLE);
    }

    private void showSuccessMessage() {
        loader.setAnimation(R.raw.registeredsucess); // Set the success animation
        loader.setVisibility(View.VISIBLE);
        Toast.makeText(SignupUser.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
        hideLoader();
        navigateToHome();
    }

    private void showNoInternet() {
        noInternet.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE); // Hide loader if there's no internet
        buttonSignup.setVisibility(View.VISIBLE); // Ensure button is visible if there's no internet
        Toast.makeText(SignupUser.this, "No internet connection. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
    }
}
