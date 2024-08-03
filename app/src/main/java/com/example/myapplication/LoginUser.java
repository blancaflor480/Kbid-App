package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginUser extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button buttonSignin, buttonCreate;
    private TextView button_admin;
    private TextView buttonHome;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @SuppressLint("MissingInflatedId")
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


        buttonSignin.setOnClickListener(v -> loginUser());
        buttonCreate.setOnClickListener(v -> startActivity(new Intent(LoginUser.this, SignupUser.class)));
        button_admin = findViewById(R.id.button_admin);
        button_admin.setOnClickListener(v -> startActivity(new Intent(LoginUser.this, SignUpAdmin.class)));
      
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

        // Authenticate user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, get the current user
                        Log.d("LoginUser", "signInWithEmail:success");
                        FirebaseUser user = auth.getCurrentUser();

                        // Check if user exists in Firestore
                        checkUserInFirestore(user.getUid());

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LoginUser", "signInWithEmail:failure", task.getException());
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
                            Log.d("LoginUser", "User document exists.");
                            // User exists in 'user' collection, proceed
                            navigateToHome();
                        } else {
                            // Check if the user exists in the 'admin' collection
                            firestore.collection("admin").document(userId).get()
                                    .addOnCompleteListener(adminTask -> {
                                        if (adminTask.isSuccessful()) {
                                            DocumentSnapshot adminDocument = adminTask.getResult();
                                            if (adminDocument.exists()) {
                                                Log.d("LoginUser", "Admin document exists.");
                                                // Check role in 'admin' collection
                                                String role = adminDocument.getString("role");
                                                if (role != null && (role.equals("Admin") || role.equals("SuperAdmin"))) {
                                                    navigateToAdminDashboard();
                                                } else {
                                                    Log.d("LoginUser", "No access rights.");
                                                    Toast.makeText(LoginUser.this, "No access rights.", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                // User does not exist in either collection
                                                Log.d("LoginUser", "User does not exist in either collection.");
                                                Toast.makeText(LoginUser.this, "No access rights.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Log.d("LoginUser", "Error checking admin collection.", adminTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.d("LoginUser", "Error checking user collection.", task.getException());
                    }
                });
    }

    private void navigateToHome() {
        // Navigate to the home screen or another activity
        Intent intent = new Intent(LoginUser.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close this activity
    }

    private void navigateToAdminDashboard() {
        // Navigate to the admin dashboard
        Intent intent = new Intent(LoginUser.this, MainSidebarAdmin.class);
        startActivity(intent);
        finish(); // Close this activity
    }
}
