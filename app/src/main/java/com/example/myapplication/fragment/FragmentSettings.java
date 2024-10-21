package com.example.myapplication.fragment;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.bumptech.glide.Glide;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.R;

public class FragmentSettings extends Fragment {

    ImageView userAvatarImageView;
    TextView clickStories, userNameTextView;
    ToggleButton toggleSound, toggleProgress, toggleAnnounce;

    private AppDatabase db;
    private UserDao userDao;

    // Reference to the no account layout
    private View noAccountLayout;

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

}
