package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText inputEmail;
    private TextInputEditText inputPassword;
    private Button buttonSignin;
    private Button buttonCreate;
    private TextInputLayout passwordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_user);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonSignin = findViewById(R.id.buttonSignin);
        buttonCreate = findViewById(R.id.buttonCreate);
        passwordLayout = findViewById(R.id.passwordLayout);

        buttonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    inputEmail.setError("Email is required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    inputPassword.setError("Password is required.");
                    return;
                }

                if (password.length() < 6) {
                    inputPassword.setError("Password must be >= 6 characters");
                    return;
                }

                // Authenticate the user
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginUser.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                        // Navigate to the home activity
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    } else {
                        Toast.makeText(LoginUser.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        buttonCreate.setOnClickListener(v -> {
            // Navigate to the registration activity
            //startActivity(new Intent(getApplicationContext(), RegisterUser.class));
        });
    }
}
