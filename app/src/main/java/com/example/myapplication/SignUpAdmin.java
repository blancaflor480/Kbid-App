package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpAdmin extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputRole;
    private Button buttonSignup, buttonCreate;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_admin);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputRole = findViewById(R.id.inputRole); // Assuming there's an EditText for role input
        buttonSignup = findViewById(R.id.buttonSignup);
        buttonCreate = findViewById(R.id.buttonCreate);

        buttonSignup.setOnClickListener(v -> registerUser());
        buttonCreate.setOnClickListener(v -> startActivity(new Intent(SignUpAdmin.this, LoginUser.class)));
    }

    private void registerUser() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String role = inputRole.getText().toString().trim();

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

        if (TextUtils.isEmpty(role)) {
            Toast.makeText(getApplicationContext(), "Enter role (Admin or SuperAdmin)!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SignupUser", "createUserWithEmail:success");
                        FirebaseUser user = auth.getCurrentUser();
                        addUserToFirestore(user, password, role);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("SignupUser", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpAdmin.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addUserToFirestore(FirebaseUser user, String password, String role) {
        if (user == null) {
            Toast.makeText(this, "User is null. Cannot add to Firestore.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user map
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("uid", user.getUid());
        userData.put("password", password); // Store password (not recommended for production)
        userData.put("role", role); // Add role to user data

        // Determine collection based on role
        String collection = "user";
        if (role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("SuperAdmin")) {
            collection = "admin";
        }

        // Add user data to Firestore
        firestore.collection(collection).document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SignupUser", "User added to Firestore successfully.");
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    Log.w("SignupUser", "Error adding user to Firestore", e);
                    Toast.makeText(SignUpAdmin.this, "Failed to add user to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToHome() {
        // Navigate to the home screen or another activity
        Intent intent = new Intent(SignUpAdmin.this, AdminDashboard.class);
        startActivity(intent);
        finish(); // Close this activity
    }
}
