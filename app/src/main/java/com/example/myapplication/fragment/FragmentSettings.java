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
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.myapplication.database.userdb.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.bumptech.glide.Glide;
import com.example.myapplication.AvatarSelectionActivity;
import com.example.myapplication.HomeActivity;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.UserDao;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class FragmentSettings extends Fragment {

    private ImageView userAvatarImageView;
    private TextView userNameTextView;
    private ToggleButton toggleSound;
    private EditText userAgeEditText, userNameEditText;
    private CardView ratingFeedbackCardView;
    private View noAccountLayout;
    private ExecutorService executor;
    private AppDatabase db;
    private UserDao userDao;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize database and DAO
        db = AppDatabase.getDatabase(requireContext());
        userDao = db.userDao();

        // Find and initialize UI components
        toggleSound = view.findViewById(R.id.togglesound);
        noAccountLayout = view.findViewById(R.id.noaccount);
        ratingFeedbackCardView = view.findViewById(R.id.ratingfeedback);

        // Set listeners for UI components
        setUpToggleButton(toggleSound);

        ratingFeedbackCardView.setOnClickListener(v -> showRatingFeedbackDialog());

        CardView exitButton = view.findViewById(R.id.exit);
        exitButton.setOnClickListener(v -> showExitConfirmationDialog());

        return view;
    }

    private void setUpToggleButton(ToggleButton toggleButton) {
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update the button appearance
            if (isChecked) {
                toggleButton.setBackgroundResource(R.drawable.toggle_on);
                toggleButton.setTextColor(Color.WHITE);
            } else {
                toggleButton.setBackgroundResource(R.drawable.toggle_off);
                toggleButton.setTextColor(Color.WHITE);
            }

            // Access HomeActivity and control the music
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).toggleMusic(isChecked); // Start music if on, pause if off
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

        // Create the dialog instance
        AlertDialog dialog = builder.create();

        // Set click listeners
        btnYes.setOnClickListener(v -> {
            dialog.dismiss(); // Dismiss the dialog before finishing the activity
            requireActivity().finish(); // Close the app
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show(); // Show the dialog
    }

    private String getCurrentUserEmail() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getEmail() : null;
    }

    private void showRatingFeedbackDialog() {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate the custom layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.ratingfeedback, null);

        // Set the custom layout to the dialog
        builder.setView(dialogView);

        // Get references to the views in the custom layout
        ImageButton closeButton = dialogView.findViewById(R.id.close);
        AppCompatButton sendFeedbackButton = dialogView.findViewById(R.id.sendfeedback);
        EditText commentEditText = dialogView.findViewById(R.id.comment);

        // Rating stars ImageViews
        ImageView rating1 = dialogView.findViewById(R.id.rating1);
        ImageView rating2 = dialogView.findViewById(R.id.rating2);
        ImageView rating3 = dialogView.findViewById(R.id.rating3);
        ImageView rating4 = dialogView.findViewById(R.id.rating4);
        ImageView rating5 = dialogView.findViewById(R.id.rating5);

        // Variable to track selected rating (1-5)
        int[] currentRating = {0}; // Using array to update within the lambda

        // Set OnClickListeners for rating stars
        rating1.setOnClickListener(v -> {
            currentRating[0] = 1;
            updateStars(rating1, rating2, rating3, rating4, rating5, 1);
        });
        rating2.setOnClickListener(v -> {
            currentRating[0] = 2;
            updateStars(rating1, rating2, rating3, rating4, rating5, 2);
        });
        rating3.setOnClickListener(v -> {
            currentRating[0] = 3;
            updateStars(rating1, rating2, rating3, rating4, rating5, 3);
        });
        rating4.setOnClickListener(v -> {
            currentRating[0] = 4;
            updateStars(rating1, rating2, rating3, rating4, rating5, 4);
        });
        rating5.setOnClickListener(v -> {
            currentRating[0] = 5;
            updateStars(rating1, rating2, rating3, rating4, rating5, 5);
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Set click listener for the close button
        closeButton.setOnClickListener(v -> dialog.dismiss());


        // Set click listener for the send feedback button
        sendFeedbackButton.setOnClickListener(v -> {
            String comment = commentEditText.getText().toString().trim();
            String email = getCurrentUserEmail();  // Get the current user's email

            // Check if the rating is greater than 0, and if email is not null
            if (currentRating[0] > 0 && email != null) {
                // Get the Firestore instance
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Create a new feedback document
                CollectionReference ratingFeedbackRef = db.collection("rating_feedback");
                DocumentReference newFeedbackRef = ratingFeedbackRef.document(); // Auto-generates a new document ID

                // Create a map with the feedback data
                Map<String, Object> feedbackData = new HashMap<>();
                feedbackData.put("rating", currentRating[0]);
                feedbackData.put("comment", comment.isEmpty() ? "" : comment);  // If no comment, send an empty string
                feedbackData.put("timestamp", FieldValue.serverTimestamp()); // Add timestamp
                feedbackData.put("email", (email));  // Use the email fetched from SQLite

                // Save the data to Firestore
                newFeedbackRef.set(feedbackData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(requireContext(), "Feedback submitted!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); // Optionally close the dialog after submission
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Error submitting feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(requireContext(), "Please select a rating!", Toast.LENGTH_SHORT).show();
            }
        });


        dialog.show();
    }

    // Method to update the star images based on rating
    private void updateStars(ImageView rating1, ImageView rating2, ImageView rating3, ImageView rating4, ImageView rating5, int rating) {
        // Reset all stars to blank
        rating1.setImageResource(R.drawable.rating_blank);
        rating2.setImageResource(R.drawable.rating_blank);
        rating3.setImageResource(R.drawable.rating_blank);
        rating4.setImageResource(R.drawable.rating_blank);
        rating5.setImageResource(R.drawable.rating_blank);

        // Set filled stars up to the selected rating
        if (rating >= 1) rating1.setImageResource(R.drawable.rating);
        if (rating >= 2) rating2.setImageResource(R.drawable.rating);
        if (rating >= 3) rating3.setImageResource(R.drawable.rating);
        if (rating >= 4) rating4.setImageResource(R.drawable.rating);
        if (rating >= 5) rating5.setImageResource(R.drawable.rating);
    }

}
