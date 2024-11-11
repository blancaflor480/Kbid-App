package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private LottieAnimationView loader;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize the loader
        loader = findViewById(R.id.loader);

        // Initialize UserDao
        userDao = AppDatabase.getDatabase(this).userDao();

        // Find the buttons by their IDs
        Button getStartedButton = findViewById(R.id.button);
        TextView button_login = findViewById(R.id.button_login);

        // Set an OnClickListener on the "Get Started" button
        getStartedButton.setOnClickListener(v -> {
            // Show the loader
            Log.d("MainActivity", "Show loader");
            showLoader();

            // Check if there is a user record in SQLite
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                checkUserRecord();
            }, 500);  // Short delay (500 milliseconds)
        });

        // Set an OnClickListener on the "Have an Account Login" button
        button_login.setOnClickListener(v -> {
            // Show the loader
            Log.d("MainActivity", "Show loader");
            showLoader();

            // Check if a user is currently signed in
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // User is signed in, check their role
                checkUserRole(currentUser.getUid());
            } else {
                // No user is signed in, redirect to LoginUser
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent intent = new Intent(MainActivity.this, LoginUser.class);
                    startActivity(intent);
                    hideLoader();  // Hide loader before navigation
                }, 500);  // Short delay (500 milliseconds)
            }
        });
    }


    private void checkUserRecord() {
        // Fetch the first user record from the database
        new Thread(() -> {
            User user = userDao.getFirstUser(); // Get the first user from the database
            runOnUiThread(() -> {
                hideLoader();  // Hide loader before navigation

                if (user != null) {
                    // User record found, check for childBirthday
                    if (user.getChildBirthday() == null || user.getChildBirthday().isEmpty()) {
                        // If childBirthday is blank, navigate to ChildBirthdayActivity
                        navigateToChildBirthdayActivity();
                    } else {
                        // If childBirthday is valid, check for avatarName
                        if (user.getAvatarName() == null || user.getAvatarName().isEmpty()) {
                            // If avatarName is blank, navigate to AvatarActivity
                            navigateToAvatarActivity();
                        } else {
                            // If both childBirthday and avatarName are valid, navigate to HomeActivity
                            navigateToHome();
                        }
                    }
                } else {
                    // No user record found, navigate to ChildNameActivity
                    navigateToChildNameActivity();
                }
            });
        }).start();
    }





    private void navigateToChildNameActivity() {
        Intent intent = new Intent(MainActivity.this, ChildNameActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish(); // Close this activity
    }
    private void navigateToChildBirthdayActivity() {
        Intent intent = new Intent(MainActivity.this, ChildAgeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish(); // Close this activity
    }

    private void navigateToAvatarActivity() {
        Intent intent = new Intent(MainActivity.this, AvatarActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish(); // Close this activity
    }

    private void checkUserRole(String userId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Check if the user's email is verified
        if (currentUser != null && !currentUser.isEmailVerified()) {
            handleAccessError("Please verify your email before logging in.");
            return;
        }

        // Fetch user details from Firestore
        firestore.collection("user").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            navigateToHome();
                        } else {
                            // Check if the user is an admin
                            firestore.collection("admin").document(userId).get()
                                    .addOnCompleteListener(adminTask -> {
                                        if (adminTask.isSuccessful()) {
                                            DocumentSnapshot adminDocument = adminTask.getResult();
                                            if (adminDocument.exists()) {
                                                // User is an admin, redirect to SideNavigationAdmin
                                                navigateToAdminDashboard();
                                            } else {
                                                // User is neither admin nor regular user
                                                handleAccessError("No access rights.");
                                            }
                                        } else {
                                            handleAccessError("Error checking admin access.");
                                        }
                                    });
                        }
                    } else {
                        handleAccessError("Error checking user access.");
                    }
                });
    }

    private void handleAccessError(String errorMessage) {
        Log.d("MainActivity", "Hide loader on error");
        hideLoader();  // Hide loader on error
        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, LoginUser.class);
        startActivity(intent);
    }

    private void navigateToHome() {
        Log.d("MainActivity", "Hide loader and navigate to HomeActivity");
        hideLoader();  // Hide the loader before navigating
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
        startActivity(intent);
        finish(); // Close this activity
    }

    private void navigateToAdminDashboard() {
        Log.d("MainActivity", "Hide loader and navigate to SideNavigationAdmin");
        hideLoader();  // Hide the loader before navigating
        Intent intent = new Intent(MainActivity.this, SideNavigationAdmin.class);
        startActivity(intent);
        finish(); // Close this activity
    }

    private void showLoader() {
        Log.d("MainActivity", "Showing loader");
        loader.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        Log.d("MainActivity", "Hiding loader");
        loader.setVisibility(View.GONE);
    }
}
