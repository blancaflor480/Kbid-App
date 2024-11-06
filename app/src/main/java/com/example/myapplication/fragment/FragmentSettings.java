package com.example.myapplication.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.bumptech.glide.Glide;
import com.example.myapplication.AvatarSelectionActivity;
import com.example.myapplication.HomeActivity;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FragmentSettings extends Fragment {

    ImageView userAvatarImageView;
    TextView clickStories, userNameTextView;
    ToggleButton toggleSound, toggleProgress, toggleAnnounce;
    EditText userAgeEditText,userNameEditText;
    private ExecutorService executor;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private AppDatabase db;
    private UserDao userDao;
    private Map<String, String> questionsAndAnswers;

    // Reference to the no account layout
    private View noAccountLayout;
    private View AppCompatButton;
    private BreakIterator googleEditText;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);


        // Initialize database and DAO
        db = AppDatabase.getDatabase(getContext());
        userDao = db.userDao();

        // Find ToggleButtons
        toggleSound = view.findViewById(R.id.togglesound);
       // toggleAnnounce = view.findViewById(R.id.toggleannounce);

        // Find the no account layout
        noAccountLayout = view.findViewById(R.id.noaccount);

        // Set listeners for each toggle button
        setUpToggleButton(toggleSound);
        //setUpToggleButton(toggleAnnounce);

        // Find the exit TextView
        CardView exitTextView = view.findViewById(R.id.exit);
        // Set click listener for exit TextView
        exitTextView.setOnClickListener(v -> showExitConfirmationDialog());

       // ImageButton changeInfoView = view.findViewById(R.id.changeinfo); // Add reference to changeinfo
       // changeInfoView.setOnClickListener(v -> showEditProfileDialog());


        // Set up the sign-in button listener
        Button signInButton = view.findViewById(R.id.signin);
        signInButton.setOnClickListener(v -> showLoginDialog());
        Button signUpButton = view.findViewById(R.id.signup);  // Find the button in your layout
        signUpButton.setOnClickListener(v -> showSignUpDialog());  // Set click listener to show the sign-up dialog



        // Load user data
        loadUserData();

        return view;
    }

    private void loadUserData() {
        AsyncTask.execute(() -> {
            User user = userDao.getFirstUser(); // Get the first user from the database
            requireActivity().runOnUiThread(() -> {
                if (user != null) {
                    // User exists, update UI
                    // Check user's email status
                    if ("No Bind".equals(user.getEmail())) {
                        // If email is "No Bind", show no account layout
                        noAccountLayout.setVisibility(View.VISIBLE);
                    } else {
                        // Hide no account layout for other email statuses
                        noAccountLayout.setVisibility(View.GONE);
                    }
                } else {
                    // No user exists, show no account layout
                    noAccountLayout.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void setUpToggleButton(ToggleButton toggleButton) {
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Toggle is ON, set green background with rounded corners
                toggleButton.setBackgroundResource(R.drawable.toggle_on);
                toggleButton.setTextColor(Color.WHITE);
            } else {
                // Toggle is OFF, set red background with rounded corners
                toggleButton.setBackgroundResource(R.drawable.toggle_off);
                toggleButton.setTextColor(Color.WHITE);
            }
        });
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate the custom layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.confirmation_exit, null);

        builder.setView(dialogView); // Set the custom layout

        // Find the buttons in the custom layout
        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);

        // Create the dialog instance
        AlertDialog dialog = builder.create();

        // Set click listeners
        btnYes.setOnClickListener(v -> {
            dialog.dismiss(); // Dismiss the dialog before finishing the activity
            requireActivity().finish(); // Close the app
        });

        btnNo.setOnClickListener(v -> {
            dialog.dismiss(); // Dismiss the dialog
        });

        dialog.show(); // Show the dialog
    }

//Login
    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate the custom layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_loginuser, null);
        builder.setView(dialogView);

        // Find views in the dialog
        EditText emailEditText = dialogView.findViewById(R.id.email);
        EditText passwordEditText = dialogView.findViewById(R.id.password);
        Button loginButton = dialogView.findViewById(R.id.btnlogin);  // Assuming you have this button in your XML
        ImageButton closeButton = dialogView.findViewById(R.id.close);

        // Create the dialog instance
        AlertDialog dialog = builder.create();

        // Set click listeners
        closeButton.setOnClickListener(v -> dialog.dismiss());

        loginButton.setOnClickListener(v -> {
            // Get email and password input
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // Check if inputs are valid
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else {
                // Perform login
                performLogin(email, password, dialog);
            }
        });

        // Show the dialog
        dialog.show();
    }

    // Method to perform login
    private void performLogin(String email, String password, AlertDialog dialog) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User signed in successfully
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Check if email is verified
                            if (user.isEmailVerified()) {
                                // Proceed to the main app
                                Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                                // Optional: Redirect to main activity
                                // startActivity(new Intent(getContext(), MainActivity.class));
                            } else {
                                // Email is not verified
                                Toast.makeText(getContext(), "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        // Sign-in failed
                        Toast.makeText(getContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to show sign up dialog
    private void showSignUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate the custom sign-up layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_registeruser, null);
        builder.setView(dialogView);

        // Find views in the dialog
        EditText emailEditText = dialogView.findViewById(R.id.email);
        EditText passwordEditText = dialogView.findViewById(R.id.password);
        EditText confirmPasswordEditText = dialogView.findViewById(R.id.confirmpassword);
        Button signUpButton = dialogView.findViewById(R.id.btnsignup);
        ImageButton closeButton = dialogView.findViewById(R.id.close);

        // Create the dialog instance
        AlertDialog dialog = builder.create();

        // Set click listener for close button
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Set click listener for the sign-up button
        signUpButton.setOnClickListener(v -> {
            // Get user inputs
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            // Validate inputs
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create the user with Firebase Authentication
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Send email verification
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "Verification email sent!", Toast.LENGTH_SHORT).show();

                                            // Insert user into Firestore with the password after successful sign-up
                                            addUserToFirestore(firebaseUser, password);

                                        } else {
                                            Toast.makeText(getContext(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Sign-up failed
                            Toast.makeText(getContext(), "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Show the dialog
        dialog.show();
    }

    private void addUserToFirestore(FirebaseUser firebaseUser, String password) {
        // Prepare user data
        String email = firebaseUser.getEmail();
        String uid = firebaseUser.getUid(); // Get the UID from Firebase Authentication

        // Create a user object to insert in Firestore
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", password); // Add the password to Firestore
        user.put("isEmailVerified", false); // Initially false, until the user verifies their email
        user.put("role", "user"); // Set default role as "user"
        user.put("uid", uid); // Insert the UID

        // Insert user data into Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(uid) // Use the UID as the document ID
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
                    // Optional: Redirect user to login or home screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to register user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
