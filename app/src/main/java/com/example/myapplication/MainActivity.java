package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Find the button by its ID
        Button getStartedButton = findViewById(R.id.button);
        TextView button_login = findViewById(R.id.button_login);

        // Set an OnClickListener on the button
        getStartedButton.setOnClickListener(v -> {
            // Create an Intent to start ChildNameActivity
            Intent intent = new Intent(MainActivity.this, ChildNameActivity.class);
            startActivity(intent);
        });

        button_login.setOnClickListener(v -> {
            // Check if a user is currently signed in
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // User is signed in, check their role
                checkUserRole(currentUser.getUid());
            } else {
                // No user is signed in, redirect to LoginUser
                Intent intent = new Intent(MainActivity.this, LoginUser.class);
                startActivity(intent);
            }
        });
    }

    private void checkUserRole(String userId) {
        firestore.collection("user").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // User is a regular user, redirect to HomeActivity
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
                                                Toast.makeText(MainActivity.this, "No access rights.", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(MainActivity.this, LoginUser.class);
                                                startActivity(intent);
                                            }
                                        } else {
                                            // Error checking admin collection
                                            Toast.makeText(MainActivity.this, "Error checking admin access.", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(MainActivity.this, LoginUser.class);
                                            startActivity(intent);
                                        }
                                    });
                        }
                    } else {
                        // Error checking user collection
                        Toast.makeText(MainActivity.this, "Error checking user access.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, LoginUser.class);
                        startActivity(intent);
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close this activity
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(MainActivity.this, SideNavigationAdmin.class);
        startActivity(intent);
        finish(); // Close this activity
    }
}
