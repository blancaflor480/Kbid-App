package com.example.myapplication.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.CustomTarget;
import com.example.myapplication.AvatarSelectionActivity;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;
import com.example.myapplication.fragment.biblegames.GamesFragment;
import com.example.myapplication.fragment.biblestories.BibleFragment;
import com.example.myapplication.fragment.notification.ModelNotification;
import com.example.myapplication.fragment.notification.NotificationAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.target.Target;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.media.MediaPlayer;

public class FragmentHome extends Fragment {
    TextView userNameTextView;
    CardView clickStories, clickGames;
    VideoView videoView;
    ImageView imageView, imageright, userAvatarImageView, sungif,rainbowgif,star1,star2,star3;
    ImageButton notif;
    EditText userAgeEditText,userNameEditText;
    private View editProfileOverlay;
    private ImageView changeInfoButton;

    private ExecutorService executor;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    private MediaPlayer mediaPlayer;
    private AppDatabase db;
    private UserDao userDao;
    private BreakIterator googleEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sungif = view.findViewById(R.id.sungif);
        rainbowgif = view.findViewById(R.id.rainbowgif);

        star1 = view.findViewById(R.id.stars1);
        star2 = view.findViewById(R.id.stars2);
        star3 = view.findViewById(R.id.stars3);

        // imageright = view.findViewById(R.id.cloudgifright);
        userNameTextView = view.findViewById(R.id.name);
        userAvatarImageView = view.findViewById(R.id.avatar);
        notif = view.findViewById(R.id.notif); // Initialize your notification button
        changeInfoButton = view.findViewById(R.id.changeinfo);

        // Show the dialog when clicking on changeinfo
        changeInfoButton.setOnClickListener(v -> showEditProfileDialog());

        // Initialize edit profile overlay
        editProfileOverlay = view.findViewById(R.id.editprofile);

        // Toggle edit profile overlay on avatar click
        userAvatarImageView.setOnClickListener(v -> {
            if (editProfileOverlay.getVisibility() == View.GONE) {
                editProfileOverlay.setVisibility(View.VISIBLE);
            } else {
                editProfileOverlay.setVisibility(View.GONE);
            }
        });

        // Initialize database and DAO
        db = AppDatabase.getDatabase(getContext());
        userDao = db.userDao();

        // Load user data
        loadUserData();

        // Glide GIF loading
        Glide.with(this)
                .asGif()
                .load(R.raw.sun_animate)
                .into(sungif);
        Glide.with(this)
                .asGif()
                .load(R.raw.cloud1)
                .into(rainbowgif);

        // Load static images for stars initially
        Glide.with(this).load(R.raw.star).into(star1);
        Glide.with(this).load(R.raw.star1).into(star2);
        Glide.with(this).load(R.raw.star2).into(star3);
        setupStarClickListeners();


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

    private void setupStarClickListeners() {
        setupStarAnimation(star1, R.raw.star1gif, R.raw.star);
        setupStarAnimation(star2, R.raw.star2gif, R.raw.star1);
        setupStarAnimation(star3, R.raw.star3gif, R.raw.star2);
    }
    // Inside your setupStarAnimation method
    private void setupStarAnimation(ImageView starView, int gifResource, int staticImageResource) {
        starView.setOnClickListener(v -> {
            Glide.with(this)
                    .asGif()
                    .load(gifResource)
                    .into(new CustomTarget<GifDrawable>() {
                        @Override
                        public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                            starView.setImageDrawable(resource);
                            resource.setLoopCount(1);
                            resource.start();
                            resource.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                                @Override
                                public void onAnimationEnd(Drawable drawable) {
                                    Glide.with(FragmentHome.this).load(staticImageResource).into(starView);
                                }
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            starView.setImageDrawable(placeholder);
                        }
                    });
        });
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
                    String greetingMessage = "Hi, " + user.getChildName();
                    userNameTextView.setText(greetingMessage);

                    /*Glide.with(requireContext())
                            .load(user.getAvatarResourceId())
                            .into(userAvatarImageView);*/
                });
            }
        });
    }

    private void showEditProfileDialog() {
        // Initialize GoogleSignInOptions and GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_profile, null);
        builder.setView(dialogView);

        ImageView avatarImageView = dialogView.findViewById(R.id.avatar);
        EditText editName = dialogView.findViewById(R.id.Editname);
        EditText editAge = dialogView.findViewById(R.id.Editage);
        EditText googleEditText = dialogView.findViewById(R.id.google);
        AppCompatButton connectButton = dialogView.findViewById(R.id.connect);
        AppCompatButton saveButton = dialogView.findViewById(R.id.save);
        ImageButton closeButton = dialogView.findViewById(R.id.close);
        ImageButton changeProfileButton = dialogView.findViewById(R.id.changepf);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            User user = userDao.getFirstUser();
            requireActivity().runOnUiThread(() -> {
                if (user != null) {
                    editName.setText(user.getChildName());
                    editAge.setText(String.valueOf(user.getChildBirthday()));
                    googleEditText.setText(user.getEmail());

                    // Set visibility of connectButton based on email value
                    if ("Null".equals(user.getEmail())) {
                        connectButton.setVisibility(View.VISIBLE);
                    } else {
                        connectButton.setVisibility(View.GONE);
                    }

                    Glide.with(requireContext())
                            .load(user.getAvatarResourceId())
                            .into(avatarImageView);
                }
            });
        });

        changeProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AvatarSelectionActivity.class);
            startActivityForResult(intent, 1);
        });

        connectButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        AlertDialog dialog = builder.create();
        closeButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v1 -> {
            String newName = editName.getText().toString();

            executor.execute(() -> {
                User user = userDao.getFirstUser();
                if (user != null) {
                    user.setChildName(newName);
                    userDao.updateUser(user);
                    requireActivity().runOnUiThread(() -> {
                        userNameTextView.setText(newName);
                        Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    });
                }
            });

            dialog.dismiss();
        });

        dialog.show();
    }

    // Handle Google Sign-In result
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // Process the result of Google Sign-In
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String email = account.getEmail();
                googleEditText.setText(email);

                // Save the email in SQLite and Firebase
                executor.execute(() -> {
                    User user = userDao.getFirstUser();
                    if (user != null) {
                        user.setEmail(email);
                        userDao.updateUser(user);

                        // Update Firebase with new email
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection("user").document(user.getUid())
                                .update("email", email)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(requireContext(), "Google account connected!", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Firebase update failed", Toast.LENGTH_SHORT).show()
                                );
                    }
                });
            }
        } catch (ApiException e) {
            Toast.makeText(requireContext(), "Google Sign-In failed", Toast.LENGTH_SHORT).show();
        }
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
