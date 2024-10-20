package com.example.myapplication.fragment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;
import com.bumptech.glide.Glide;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.fragment.biblegames.GamesFragment;
import com.example.myapplication.fragment.biblestories.BibleFragment;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentHome extends Fragment {
    TextView clickStories,userNameTextView;
    TextView clickGames;
    VideoView videoView;
    ImageView imageView, imageright, userAvatarImageView;
    // For displaying user name
      // For displaying user avatar

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

        // Initialize database and DAO
        db = AppDatabase.getDatabase(getContext());
        userDao = db.userDao();

        // Load user data
        loadUserData();

        // Glide GIF loading (already implemented in your original code)
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

        clickStories.setOnClickListener(v -> navigateToBibleActivity());
        clickGames.setOnClickListener(v -> navigateToBiblegamesActivity());

        return view;
    }

    private void loadUserData() {
        AsyncTask.execute(() -> {
            User user = userDao.getFirstUser(); // Get the first user from the database
            if (user != null) {
                // Update UI on the main thread
                requireActivity().runOnUiThread(() -> {
                    // Set greeting message with the user's name
                    String greetingMessage = "Hello, " + user.getChildName(); // Create greeting message
                    userNameTextView.setText(greetingMessage); // Display greeting message

                    // Load avatar using Glide
                    Glide.with(requireContext()) // Use requireContext() for Glide
                            .load(user.getAvatarResourceId()) // Load avatar using Glide
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