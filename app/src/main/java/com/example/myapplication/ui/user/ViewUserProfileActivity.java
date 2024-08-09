package com.example.myapplication.ui.user;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class ViewUserProfileActivity extends AppCompatActivity {

    private TextView firstNameTextView, middleNameTextView, lastNameTextView, birthdayTextView, genderTextView, roleTextView, emailTextView;
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get user ID and role from intent
        String userId = getIntent().getStringExtra("USER_ID");
        String userRole = getIntent().getStringExtra("USER_ROLE");

        // Debug logging
        Log.d("ViewUserProfileActivity", "USER_ID: " + userId);
        Log.d("ViewUserProfileActivity", "USER_ROLE: " + userRole);

        // Validate data
        if (userId == null || userRole == null) {
            Toast.makeText(this, "User ID or Role is missing", Toast.LENGTH_SHORT).show();
            finish(); // Close activity
            return;
        }

        // Fetch user details from Firestore
        fetchUserProfile(userId, userRole);
    }

    private void fetchUserProfile(String userId, String userRole) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String collectionName = "user"; // Default collection name

        if (userRole != null && (userRole.equalsIgnoreCase("Admin") || userRole.equalsIgnoreCase("SuperAdmin"))) {
            collectionName = "admin";
        }

        db.collection(collectionName)
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            ModelUser user = documentSnapshot.toObject(ModelUser.class);
                            if (user != null) {
                                showUserProfileDialog(ViewUserProfileActivity.this, user);
                            } else {
                                Toast.makeText(ViewUserProfileActivity.this, "User data conversion failed", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ViewUserProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ViewUserProfileActivity.this, "Error loading user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showUserProfileDialog(Context context, ModelUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_view_user, null);
        builder.setView(dialogView);

        // Initialize views
        profileImageView = dialogView.findViewById(R.id.profileImageView);
        firstNameTextView = dialogView.findViewById(R.id.firstNameTextView);
        middleNameTextView = dialogView.findViewById(R.id.middleNameTextView);
        lastNameTextView = dialogView.findViewById(R.id.lastNameTextView);
        birthdayTextView = dialogView.findViewById(R.id.birthdayTextView);
        genderTextView = dialogView.findViewById(R.id.genderTextView);
        roleTextView = dialogView.findViewById(R.id.roleTextView);
        emailTextView = dialogView.findViewById(R.id.emailTextView);

        // Populate user data
        firstNameTextView.setText("First Name: " + user.getFirstname());
        middleNameTextView.setText("Middle Name: " + user.getMiddlename());
        lastNameTextView.setText("Last Name: " + user.getLastname());
        birthdayTextView.setText("Birthday: " + user.getBirthday());
        genderTextView.setText("Gender: " + user.getGender());
        roleTextView.setText("Role: " + user.getRole());
        emailTextView.setText("Email: " + user.getEmail());

        // Load profile image
        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            Glide.with(context).load(user.getImageUrl()).into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.user); // Replace with your default image
        }

        // Show the dialog
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
