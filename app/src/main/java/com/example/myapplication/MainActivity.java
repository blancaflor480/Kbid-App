package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.Notification.DevotionalNotificationScheduler;
import com.example.myapplication.Notification.NotificationReceiver;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private LottieAnimationView loader;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestNotificationPermission();
        DevotionalNotificationScheduler.checkDevotionalForToday(this);
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
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can schedule notifications
                DevotionalNotificationScheduler.scheduleNotificationCheck(this);
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Notification permission is required for daily devotionals", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkUserRecord() {
        new Thread(() -> {
            User user = userDao.getFirstUser();
            runOnUiThread(() -> {
                hideLoader();

                if (user != null) {
                    // User record exists
                    if (user.getChildBirthday() == null || user.getChildBirthday().isEmpty()) {
                        // Check if user is guest (no email and controlid)
                        if ((user.getEmail() == null || user.getEmail().isEmpty()) &&
                                (user.getControlid() == null || user.getControlid().isEmpty())) {
                            // Guest user with no birthday - go directly to home
                            navigateToHome();
                        } else {
                            // Registered user with no birthday - go to birthday input
                            navigateToChildBirthdayActivity();
                        }
                    } else {
                        // Birthday exists, check avatar
                        if (user.getAvatarName() == null || user.getAvatarName().isEmpty()) {
                            navigateToAvatarActivity();
                        } else {
                            navigateToHome();
                        }
                    }
                } else {
                    // No user record found
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
