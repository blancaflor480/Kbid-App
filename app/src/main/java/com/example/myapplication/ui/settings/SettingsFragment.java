package com.example.myapplication.ui.settings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ValueAnimator;
import android.widget.LinearLayout;
import androidx.cardview.widget.CardView;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.myapplication.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {
    private CardView editProfileCard;
    private CardView changePasswordCard;
    private LinearLayout editProfileContent;
    private LinearLayout changePasswordContent;
    private boolean isEditProfileExpanded = false;
    private boolean isChangePasswordExpanded = false;

    private EditText etEmail, etFirstName, etMiddleName, etLastName, etBirthday;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Spinner spinnerGender;
    private Button btnSaveChanges, btnUpdatePassword;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;
    private String userRole;
    private Activity view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_admin, container, false);

        initializeFirebase();
        initializeViews(view);
        setupSpinner();
        setupDatePicker();
        loadUserData();
        setupClickListeners(view);  // Pass the view here

        return view;
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
    }

    private void initializeViews(View view) {
        // Initialize CardViews and LinearLayouts
        editProfileCard = view.findViewById(R.id.card_edit_profile);
        changePasswordCard = view.findViewById(R.id.card_change_password);
        editProfileContent = view.findViewById(R.id.layout_edit_profile_content);
        changePasswordContent = view.findViewById(R.id.layout_change_password_content);

        // Initialize EditTexts
        etEmail = view.findViewById(R.id.etEmail);
        etFirstName = view.findViewById(R.id.etFirstName);
        etMiddleName = view.findViewById(R.id.etMiddleName);
        etLastName = view.findViewById(R.id.etLastName);
        etBirthday = view.findViewById(R.id.etBirthday);
        spinnerGender = view.findViewById(R.id.spinnerGender);

        // Initialize password fields
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        // Set email as disabled
        etEmail.setEnabled(false);

        // Set initial visibility
        editProfileContent.setVisibility(View.GONE);
        changePasswordContent.setVisibility(View.GONE);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.gender_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etBirthday.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                        etBirthday.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void loadUserData() {
        db.collection("admin")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userRole = documentSnapshot.getString("role");
                        etEmail.setText(documentSnapshot.getString("email"));
                        etFirstName.setText(documentSnapshot.getString("firstname"));
                        etMiddleName.setText(documentSnapshot.getString("middlename"));
                        etLastName.setText(documentSnapshot.getString("lastname"));
                        etBirthday.setText(documentSnapshot.getString("birthday"));

                        // Set gender spinner selection
                        String gender = documentSnapshot.getString("gender");
                        ArrayAdapter adapter = (ArrayAdapter) spinnerGender.getAdapter();
                        int position = adapter.getPosition(gender);
                        spinnerGender.setSelection(position);

                        // Enable editing only for SuperAdmin and Teacher roles
                        boolean canEdit = "SuperAdmin".equals(userRole) || "Teacher".equals(userRole);
                        setFieldsEditable(canEdit);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void setFieldsEditable(boolean editable) {
        etFirstName.setEnabled(editable);
        etMiddleName.setEnabled(editable);
        etLastName.setEnabled(editable);
        etBirthday.setEnabled(editable);
        spinnerGender.setEnabled(editable);
    }

    private void setupClickListeners(View view) {  // Add View parameter
        editProfileCard.setOnClickListener(v -> toggleEditProfile());
        changePasswordCard.setOnClickListener(v -> toggleChangePassword());

        // Initialize the buttons
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        btnUpdatePassword = view.findViewById(R.id.btnUpdatePassword);

        // Set click listeners for buttons
        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
        btnUpdatePassword.setOnClickListener(v -> updatePassword());
    }

    private void saveProfileChanges() {
        if (!("SuperAdmin".equals(userRole) || "Teacher".equals(userRole))) {
            Toast.makeText(requireContext(), "You don't have permission to edit", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create and show the confirmation dialog
        Dialog confirmDialog = new Dialog(requireContext());
        confirmDialog.setContentView(R.layout.confirmation_editprof);
        confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize dialog buttons
        AppCompatButton btnYes = confirmDialog.findViewById(R.id.btnYes);
        AppCompatButton btnNo = confirmDialog.findViewById(R.id.btnNo);

        // Set click listener for No button
        btnNo.setOnClickListener(v -> {
            confirmDialog.dismiss();
        });

        // Set click listener for Yes button
        btnYes.setOnClickListener(v -> {
            // Create updates map
            Map<String, Object> updates = new HashMap<>();
            updates.put("firstname", etFirstName.getText().toString());
            updates.put("middlename", etMiddleName.getText().toString());
            updates.put("lastname", etLastName.getText().toString());
            updates.put("birthday", etBirthday.getText().toString());
            updates.put("gender", spinnerGender.getSelectedItem().toString());

            // Update Firestore
            db.collection("admin")
                    .document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        confirmDialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                        confirmDialog.dismiss();
                    });
        });

        // Show the dialog
        confirmDialog.show();
    }

    private void updatePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the confirmation dialog
        Dialog confirmDialog = new Dialog(requireContext());
        confirmDialog.setContentView(R.layout.confirmation_editprof);
        confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize the views from custom layout
        Button buttonYes = confirmDialog.findViewById(R.id.btnYes);
        Button buttonNo = confirmDialog.findViewById(R.id.btnNo);
        TextView messageText = confirmDialog.findViewById(R.id.dialogMessage);
        TextView title = confirmDialog.findViewById(R.id.dialogTitle);

        // Set the confirmation message
        title.setText("Confirmation to change password");
        messageText.setText("Are you sure you want to update your password?");

        // Handle Yes button click
        buttonYes.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                // First, reauthenticate the user
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

                user.reauthenticate(credential)
                        .addOnCompleteListener(reauthTask -> {
                            if (reauthTask.isSuccessful()) {
                                // Reauthentication successful, now change the password
                                user.updatePassword(newPassword)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                                // Clear the password fields
                                                etCurrentPassword.setText("");
                                                etNewPassword.setText("");
                                                etConfirmPassword.setText("");
                                                confirmDialog.dismiss();
                                            } else {
                                                Toast.makeText(requireContext(), "Failed to update password: " +
                                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Authentication failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(requireContext(), "No user is currently signed in", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle No button click
        buttonNo.setOnClickListener(v -> {
            confirmDialog.dismiss();
        });

        // Show the dialog
        confirmDialog.show();
    }

    // Your existing toggle and animation methods remain the same
    private void toggleEditProfile() {
        isEditProfileExpanded = !isEditProfileExpanded;
        animateDropdown(editProfileContent, isEditProfileExpanded);

        if (isEditProfileExpanded && isChangePasswordExpanded) {
            isChangePasswordExpanded = false;
            animateDropdown(changePasswordContent, false);
        }
    }

    private void toggleChangePassword() {
        isChangePasswordExpanded = !isChangePasswordExpanded;
        animateDropdown(changePasswordContent, isChangePasswordExpanded);

        if (isChangePasswordExpanded && isEditProfileExpanded) {
            isEditProfileExpanded = false;
            animateDropdown(editProfileContent, false);
        }
    }

    private void animateDropdown(final View view, boolean expand) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        if (expand) {
            view.setVisibility(View.VISIBLE);
            ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
            animator.addUpdateListener(animation -> {
                view.getLayoutParams().height = (int) animation.getAnimatedValue();
                view.requestLayout();
            });
            animator.setDuration(300);
            animator.start();
        } else {
            ValueAnimator animator = ValueAnimator.ofInt(view.getHeight(), 0);
            animator.addUpdateListener(animation -> {
                view.getLayoutParams().height = (int) animation.getAnimatedValue();
                view.requestLayout();
            });
            animator.setDuration(300);
            animator.start();
            animator.addUpdateListener(animation -> {
                if ((int) animation.getAnimatedValue() == 0) {
                    view.setVisibility(View.GONE);
                }
            });
        }
    }
}