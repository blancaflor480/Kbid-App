package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboard extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView userNameTextView, userRoleTextView;
    private Button editProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userNameTextView = findViewById(R.id.userName);
        userRoleTextView = findViewById(R.id.userRole);
        editProfileButton = findViewById(R.id.editProfileButton);

        if (mAuth.getCurrentUser() != null) {
            String userEmail = mAuth.getCurrentUser().getEmail();
            if (userEmail != null) {
                fetchUserDetails(userEmail);
            } else {
                Log.d(TAG, "No email found for the current user.");
                redirectToLogin();
            }
        } else {
            // No user is signed in, redirect to login
            redirectToLogin();
        }
    }

    private void fetchUserDetails(String email) {
        // Use the email of the signed-in user as the document ID
        DocumentReference docRef = db.collection("admin").document(email);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "Document found: " + document.getId());
                    String firstName = document.getString("firstname");
                    String lastName = document.getString("lastname");
                    String role = document.getString("role");

                    String fullName = (firstName != null ? firstName : "N/A") + " " + (lastName != null ? lastName : "N/A");
                    userNameTextView.setText(fullName);
                    userRoleTextView.setText(role != null ? role : "N/A");

                    if (role != null && (role.equalsIgnoreCase("SuperAdmin") || role.equalsIgnoreCase("Admin"))) {
                        Log.d(TAG, "User allowed: " + fullName + " with role " + role);
                        // Proceed with the activity as the user has the appropriate role
                    } else {
                        Log.d(TAG, "Access denied for user: " + fullName + " with role " + role);
                        redirectToLogin();
                    }
                } else {
                    Log.d(TAG, "No such document with ID: " + email);
                    redirectToLogin();
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                redirectToLogin();
            }
        });
    }

    private void redirectToLogin() {
        Toast.makeText(AdminDashboard.this, "Access denied. Please log in with appropriate credentials.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AdminDashboard.this, LoginUser.class);
        startActivity(intent);
        finish();
    }
}
