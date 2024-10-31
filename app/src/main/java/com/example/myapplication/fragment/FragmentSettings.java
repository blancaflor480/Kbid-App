package com.example.myapplication.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

    private AppDatabase db;
    private UserDao userDao;
    private Map<String, String> questionsAndAnswers;

    // Reference to the no account layout
    private View noAccountLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the questions and answers
        questionsAndAnswers = new HashMap<>();
        questionsAndAnswers.put("Who is the son of God?", "Jesus");
        questionsAndAnswers.put("Who led the Israelites out of Egypt?", "Moses");
        questionsAndAnswers.put("Who was thrown into the lion's den?", "Daniel");
        questionsAndAnswers.put("Who was swallowed by a big fish?", "Jonah");
        questionsAndAnswers.put("Who was the strongest man in the Bible?", "Samson");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        userNameTextView = view.findViewById(R.id.name);

        userAvatarImageView = view.findViewById(R.id.avatar);
        // Initialize database and DAO
        db = AppDatabase.getDatabase(getContext());
        userDao = db.userDao();

        // Find ToggleButtons
        toggleSound = view.findViewById(R.id.togglesound);
        toggleProgress = view.findViewById(R.id.toggleprogress);
        toggleAnnounce = view.findViewById(R.id.toggleannounce);

        // Find the no account layout
        noAccountLayout = view.findViewById(R.id.noaccount);

        // Set listeners for each toggle button
        setUpToggleButton(toggleSound);
        setUpToggleButton(toggleProgress);
        setUpToggleButton(toggleAnnounce);

        // Find the exit TextView
        CardView exitTextView = view.findViewById(R.id.exit);
        // Set click listener for exit TextView
        exitTextView.setOnClickListener(v -> showExitConfirmationDialog());

        ImageButton changeInfoView = view.findViewById(R.id.changeinfo); // Add reference to changeinfo
        changeInfoView.setOnClickListener(v -> showEditProfileDialog());


        // Set up the sign-in button listener
        Button signInButton = view.findViewById(R.id.signin);
        signInButton.setOnClickListener(v -> showLoginDialog());
        Button signUpButton = view.findViewById(R.id.signup);  // Find the button in your layout
        signUpButton.setOnClickListener(v -> showSignUpDialog());  // Set click listener to show the sign-up dialog

        // Trigger the dialog when settings are accessed



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
                    String greetingMessage = user.getChildName(); // Create greeting message
                    userNameTextView.setText(greetingMessage); // Display greeting message

                    // Load avatar using Glide
                    Glide.with(requireContext()) // Use requireContext() for Glide
                            .load(user.getAvatarResourceId()) // Load avatar using Glide
                            .into(userAvatarImageView);

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
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate the custom layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_profile, null);

        builder.setView(dialogView); // Set the custom layout

        // Find the EditText fields, close button, and other views in the dialog layout
        ImageView avatarImageView = dialogView.findViewById(R.id.avatar); // This is the avatar in the dialog
        EditText editName = dialogView.findViewById(R.id.Editname);
        EditText editAge = dialogView.findViewById(R.id.Editage);
        EditText google = dialogView.findViewById(R.id.google);

        Button saveButton = dialogView.findViewById(R.id.save);
        ImageButton closeButton = dialogView.findViewById(R.id.close); // Close button
        ImageButton changeProfileButton = dialogView.findViewById(R.id.changepf);

        // Load current user data (from the database) using ExecutorService
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            User user = userDao.getFirstUser(); // Get the first user from the database
            requireActivity().runOnUiThread(() -> {
                if (user != null) {
                    // Set the current data in the EditText fields
                    editName.setText(user.getChildName());
                    editAge.setText(String.valueOf(user.getChildAge())); // Convert int age to String
                    google.setText(user.getEmail());
                    // Load avatar using Glide
                    Glide.with(requireContext()) // Use requireContext() for Glide
                            .load(user.getAvatarResourceId()) // Load avatar using user's resource id
                            .into(avatarImageView); // Set the avatar in the dialog's ImageView
                }
            });
        });

        changeProfileButton.setOnClickListener(v -> {
            // Start AvatarSelectionActivity
            Intent intent = new Intent(getActivity(), AvatarSelectionActivity.class);
            startActivityForResult(intent, 1); // Use a request code of 1
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Set up the close button click listener
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Set up the save button click listener
        saveButton.setOnClickListener(v1 -> {
            // Get the updated name and age from the EditText fields
            String newName = editName.getText().toString();
            String newAgeStr = editAge.getText().toString();

            // Convert the age to an integer
            int newAge;
            try {
                newAge = Integer.parseInt(newAgeStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid age format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save the updated data to the database using ExecutorService
            executor.execute(() -> {
                User user = userDao.getFirstUser();
                if (user != null) {
                    user.setChildName(newName);  // Update the child's name
                    user.setChildAge(String.valueOf(newAge));    // Update the child's age
                    userDao.updateUser(user);    // Update the user in the database

                    // Update the UI after saving
                    requireActivity().runOnUiThread(() -> {
                        userNameTextView.setText(newName); // Update the displayed name in the fragment
                        Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    });
                }
            });

            // Dismiss the dialog
            dialog.dismiss();
        });

        dialog.show(); // Show the dialog
    }

    //Login
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


    @Override
    public void onResume() {
        super.onResume();
        // Only show the dialog when this fragment is visible
        showAskYourParentDialog();
    }
    private void showAskYourParentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate the custom layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_askyourparent, null);
        builder.setView(dialogView);

        // Find the views in the dialog
        EditText parentAnswerEditText = dialogView.findViewById(R.id.Editname);
        Button submitButton = dialogView.findViewById(R.id.submit);
        Button cancelButton = dialogView.findViewById(R.id.cancel);
        TextView questionTextView = dialogView.findViewById(R.id.question);

        // Create the dialog instance
        AlertDialog dialog = builder.create();

        // Get the Vibrator service
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        // Randomly select a question
        List<String> questions = new ArrayList<>(questionsAndAnswers.keySet());
        String randomQuestion = questions.get(new Random().nextInt(questions.size()));
        questionTextView.setText(randomQuestion);

        // Set click listener for the submit button
        submitButton.setOnClickListener(v -> {
            String answer = parentAnswerEditText.getText().toString().trim();
            String correctAnswer = questionsAndAnswers.get(randomQuestion);

            // Check if the answer is correct
            if (answer.equalsIgnoreCase(correctAnswer)) {
                // Dismiss the dialog if the correct answer is provided
                dialog.dismiss();
                Toast.makeText(requireContext(), "Correct! Access granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Show an error message if the answer is wrong
                parentAnswerEditText.setBackgroundResource(R.drawable.red_outline);
                parentAnswerEditText.setText("");

                // Vibrate the device for 1000 milliseconds
                if (vibrator != null) {
                    vibrator.vibrate(1000);
                }
            }
        });

        // Set click listener for the cancel button
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss(); // Close the dialog
            // Navigate to HomeActivity
            Intent intent = new Intent(requireContext(), HomeActivity.class);
            startActivity(intent);
            requireActivity().finish(); // Optional: close the current activity if needed
        });

        // Disable dismissing the dialog by clicking outside or pressing the back button
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Show the dialog
        dialog.show();
    }


}
