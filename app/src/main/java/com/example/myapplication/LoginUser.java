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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginUser extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button buttonSignin, buttonCreate;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private LottieAnimationView loader, noInternet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_user);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonSignin = findViewById(R.id.buttonSignin);
        buttonCreate = findViewById(R.id.buttonCreate);
        loader = findViewById(R.id.loader);
        noInternet = findViewById(R.id.nointernet);

        buttonSignin.setOnClickListener(v -> loginUser());
        buttonCreate.setOnClickListener(v -> startActivity(new Intent(LoginUser.this, SignupUser.class)));
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void loginUser() {
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

        if (!isConnected()) {
            showNoInternet();
            return;
        }

        // Show loader and hide the button when login is initiated
        showLoader();

        // Authenticate user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, get the current user
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            checkUserInFirestore(user.getUid());
                        }
                    } else {
                        // If sign in fails, hide loader and show message
                        hideLoader();
                        Toast.makeText(LoginUser.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserInFirestore(String userId) {
        // Check if the user exists in the 'user' collection
        firestore.collection("user").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            navigateToHome();
                        } else {
                            // Check if the user exists in the 'admin' collection
                            firestore.collection("admin").document(userId).get()
                                    .addOnCompleteListener(adminTask -> {
                                        if (adminTask.isSuccessful()) {
                                            DocumentSnapshot adminDocument = adminTask.getResult();
                                            if (adminDocument.exists()) {
                                                String role = adminDocument.getString("role");
                                                if (role != null && (role.equals("Admin") || role.equals("SuperAdmin"))) {
                                                    navigateToAdminDashboard();
                                                } else {
                                                    showNoAccessMessage();
                                                }
                                            } else {
                                                showNoAccessMessage();
                                            }
                                        } else {
                                            Log.e("LoginUser", "Error checking admin collection", adminTask.getException());
                                        }
                                        hideLoader();  // Hide loader once done
                                    });
                        }
                    } else {
                        Log.e("LoginUser", "Error checking user collection", task.getException());
                        hideLoader();  // Hide loader on error
                    }
                });
    }

    private void navigateToHome() {
        hideLoader();  // Hide the loader when navigating
        Intent intent = new Intent(LoginUser.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close this activity
    }

    private void navigateToAdminDashboard() {
        hideLoader();  // Hide the loader when navigating
        Intent intent = new Intent(LoginUser.this, SideNavigationAdmin.class);
        startActivity(intent);
        finish(); // Close this activity
    }

    private void showNoAccessMessage() {
        hideLoader();  // Hide loader
        Toast.makeText(LoginUser.this, "No access rights.", Toast.LENGTH_SHORT).show();
    }

    private void showLoader() {
        loader.setVisibility(View.VISIBLE);
        buttonSignin.setVisibility(View.GONE); // Hide the button when loader is visible

        // Optionally, apply a blur effect or other UI changes if needed
    }

    private void hideLoader() {
        loader.setVisibility(View.GONE);
        buttonSignin.setVisibility(View.VISIBLE); // Show the button again if loader is hidden
        noInternet.setVisibility(View.GONE);
    }

    private void showNoInternet() {
        noInternet.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        buttonSignin.setVisibility(View.VISIBLE); // Ensure button is visible if there's no internet
    }
}
