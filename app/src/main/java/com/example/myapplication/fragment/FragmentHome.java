package com.example.myapplication.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.fragment.biblegames.GamesFragment;
import com.example.myapplication.fragment.biblestories.BibleFragment;
import com.example.myapplication.fragment.notification.ModelNotification;
import com.example.myapplication.fragment.notification.NotificationAdapter;
import java.util.ArrayList;
import java.util.List;
import android.media.MediaPlayer;

public class FragmentHome extends Fragment {
    TextView clickStories, userNameTextView;
    TextView clickGames;
    VideoView videoView;
    ImageView imageView, imageright, userAvatarImageView;
    ImageButton notif;

    private MediaPlayer mediaPlayer;
    private AppDatabase db;
    private UserDao userDao;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imageView = view.findViewById(R.id.cloudgif);
        imageright = view.findViewById(R.id.cloudgifright);
        userNameTextView = view.findViewById(R.id.name);
        userAvatarImageView = view.findViewById(R.id.avatar);
        notif = view.findViewById(R.id.notif); // Initialize your notification button

        // Initialize database and DAO
        db = AppDatabase.getDatabase(getContext());
        userDao = db.userDao();

        // Load user data
        loadUserData();

        // Glide GIF loading
        Glide.with(this)
                .asGif()
                .load(R.raw.cloud)
                .into(imageView);
        Glide.with(this)
                .asGif()
                .load(R.raw.cloud)
                .into(imageright);

        // Set up click listeners
        clickStories = view.findViewById(R.id.clickStories);
        clickGames = view.findViewById(R.id.clickGames);

        clickStories.setOnClickListener(v -> {
            playClickSound(); // Play sound effect
            navigateToBibleActivity();
        });

        clickGames.setOnClickListener(v -> {
            playClickSound(); // Play sound effect
            navigateToBiblegamesActivity();
        });

        // Set up notification button click listener
        notif.setOnClickListener(v -> {
            playClickSound(); // Play sound effect
            showNotificationDialog();
        });

        return view;
    }

    private void playClickSound() {
        // Release any previously playing sound to prevent overlapping
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        // Initialize MediaPlayer with the sound file
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.clickpop); // Ensure your sound file is in res/raw
        mediaPlayer.setOnCompletionListener(mp -> mp.release()); // Release after playing
        mediaPlayer.start(); // Play the sound
    }

    private void showNotificationDialog() {
        // Create a dialog
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.notifcation); // Set the custom layout

        // Initialize close button
        ImageButton closeButton = dialog.findViewById(R.id.close);
        closeButton.setOnClickListener(v -> dialog.dismiss()); // Close the dialog

        // Initialize RecyclerView
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclep);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get the list of notifications
        List<ModelNotification> notifications = getNotificationList(); // Update the variable name to match the new return type

        // Set up the adapter with the list of notifications
        NotificationAdapter adapter = new NotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);

        // Update the visibility of "No Message" TextView
        TextView noMessageTextView = dialog.findViewById(R.id.nomessage);
        if (notifications.isEmpty()) { // Check if the list is empty
            noMessageTextView.setVisibility(View.VISIBLE); // Show "No Message"
            recyclerView.setVisibility(View.GONE); // Hide RecyclerView
        } else {
            noMessageTextView.setVisibility(View.GONE); // Hide "No Message"
            recyclerView.setVisibility(View.VISIBLE); // Show RecyclerView
        }

        dialog.show(); // Show the dialog
    }

    private List<ModelNotification> getNotificationList() {
        List<ModelNotification> notifications = new ArrayList<>();

        // Replace with actual data fetching logic if needed
        notifications.add(new ModelNotification("Notification 1: New update available!", "2024-10-22", null));
        notifications.add(new ModelNotification("Notification 2: Your profile has been updated.", "2024-10-21", null));
        notifications.add(new ModelNotification("Notification 3: You have a new message.", "2024-10-20", null));

        return notifications; // Return the list of notifications
    }

    private void loadUserData() {
        AsyncTask.execute(() -> {
            User user = userDao.getFirstUser();
            if (user != null) {
                requireActivity().runOnUiThread(() -> {
                    String greetingMessage = "Hello, " + user.getChildName();
                    userNameTextView.setText(greetingMessage);

                    Glide.with(requireContext())
                            .load(user.getAvatarResourceId())
                            .into(userAvatarImageView);
                });
            }
        });
    }

    private void navigateToBibleActivity() {
        Intent intent = new Intent(getActivity(), BibleFragment.class);
        startActivity(intent);
        Log.d("FragmentHome", "Navigating to BibleActivity");
    }

    private void navigateToBiblegamesActivity() {
        Intent intent = new Intent(getActivity(), GamesFragment.class);
        startActivity(intent);
        Log.d("FragmentHome", "Navigating to Biblegames");
    }
}
