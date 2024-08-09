package com.example.myapplication.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditUserProfileActivity extends Fragment {

    private ImageView profileImageView;
    private EditText firstNameEditText, middleNameEditText, lastNameEditText, birthdayEditText, emailEditText;
    private Spinner genderSpinner, roleSpinner;
    private Button saveButton;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_user, container, false);

        // Initialize views
        profileImageView = view.findViewById(R.id.ivProfileImage);
        firstNameEditText = view.findViewById(R.id.etFirstName);
        middleNameEditText = view.findViewById(R.id.etMiddleName);
        lastNameEditText = view.findViewById(R.id.etLastName);
        birthdayEditText = view.findViewById(R.id.etBirthday);
        emailEditText = view.findViewById(R.id.etEmail);
        genderSpinner = view.findViewById(R.id.spinnerGender);
        roleSpinner = view.findViewById(R.id.spinnerRole);
        saveButton = view.findViewById(R.id.btnSave);

        // Retrieve userId from arguments
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        // Load the user data
        loadUserData();

        // Set save button action
        saveButton.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void loadUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String firstName = documentSnapshot.getString("firstName");
                String middleName = documentSnapshot.getString("middleName");
                String lastName = documentSnapshot.getString("lastName");
                String email = documentSnapshot.getString("email");
                String role = documentSnapshot.getString("role");
                String birthday = documentSnapshot.getString("birthday");
                String gender = documentSnapshot.getString("gender");
                String imageUrl = documentSnapshot.getString("imageUrl");

                // Set the data to views
                firstNameEditText.setText(firstName);
                middleNameEditText.setText(middleName);
                lastNameEditText.setText(lastName);
                emailEditText.setText(email);
                birthdayEditText.setText(birthday);

                // Assuming gender and role are set via Spinner, you may need to set their values accordingly
                // For example, find the appropriate index in the spinner that matches the fetched value
                // genderSpinner.setSelection(genderAdapter.getPosition(gender));
                // roleSpinner.setSelection(roleAdapter.getPosition(role));

                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.user)
                        .error(R.drawable.error_outline)
                        .into(profileImageView);
            }
        }).addOnFailureListener(e -> {
            // Handle the error
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveUserData() {
        String firstName = firstNameEditText.getText().toString().trim();
        String middleName = middleNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String birthday = birthdayEditText.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        String role = roleSpinner.getSelectedItem().toString();

        if (firstName.isEmpty() || email.isEmpty() || role.isEmpty() || birthday.isEmpty() || gender.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId);

        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName);
        user.put("middleName", middleName);
        user.put("lastName", lastName);
        user.put("email", email);
        user.put("role", role);
        user.put("birthday", birthday);
        user.put("gender", gender);

        docRef.update(user).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "User profile updated", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            // Handle the error
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
        });
    }
}
