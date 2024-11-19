package com.example.myapplication.fragment.biblestories.playlist;

import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import java.util.ArrayList;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;

import android.speech.SpeechRecognizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.database.BibleDao;
import com.example.myapplication.database.BibleDatabaseHelper;
import com.example.myapplication.database.favorite.FavoriteDao;
import com.example.myapplication.fragment.biblestories.ModelBible;
import com.example.myapplication.fragment.biblestories.favoritelist.Modelfavoritelist;
import com.example.myapplication.database.AppDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class BiblePlay extends AppCompatActivity {

    private TextView titleView, verseView, storyView;
    private ImageView thumbnail, arrowback;
    private boolean isPlaying = false;
    private boolean isRepeatEnabled = false;
    private ImageButton playButton, pauseButton, repeatButton, restartButton;
    private MediaPlayer mediaPlayer;
    private String audioUrl;
    private SpeechRecognizer speechRecognizer;
    private Handler handler;
    private LinearLayout addplaylist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_strories);

        // Initialize views
        titleView = findViewById(R.id.title);
        verseView = findViewById(R.id.verse);
        repeatButton = findViewById(R.id.buttonrepeat);
        restartButton = findViewById(R.id.buttonrestart);
        storyView = findViewById(R.id.textstory);
        thumbnail = findViewById(R.id.thumbnail);
        arrowback = findViewById(R.id.arrowback);
        playButton = findViewById(R.id.buttonplay);
        pauseButton = findViewById(R.id.buttonpause);
        addplaylist = findViewById(R.id.addplaylist);
        // Get data from intent
        String title = getIntent().getStringExtra("title");
        String verse = getIntent().getStringExtra("verse");
        audioUrl = getIntent().getStringExtra("audioUrl");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Set data to views
        titleView.setText(title);
        verseView.setText(verse);
        setThumbnail(imageUrl);

        // Set click listeners
        arrowback.setOnClickListener(v -> onBackPressed());
        playButton.setOnClickListener(v -> togglePlayPause(true));
        pauseButton.setOnClickListener(v -> togglePlayPause(false));
        repeatButton.setOnClickListener(v -> toggleRepeatMode());
        restartButton.setOnClickListener(v -> restartStory());

        // Initially, show play button and hide pause button
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        repeatButton.setBackgroundTintList(getResources().getColorStateList(R.color.greenlightning));
// Add this inside onCreate() after initializing addplaylist
        addplaylist.setOnClickListener(v -> addStoryToFavorites());
        handler = new Handler();
    }

    private void togglePlayPause(boolean play) {
        if (audioUrl != null && !audioUrl.isEmpty()) {
            if (play) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(this, Uri.parse(audioUrl));
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        // Start subtitle synchronization
                        startAudioSynchronization();

                        // Listener to unlock the next story when the audio completes
                        mediaPlayer.setOnCompletionListener(mp -> {
                            stopAudioSynchronization();
                            playButton.setVisibility(View.VISIBLE);
                            pauseButton.setVisibility(View.GONE);

                            // Unlock the next story
                            unlockNextStory(getCurrentStoryId());
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mediaPlayer.start();
                    startAudioSynchronization();
                }

                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
            } else {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    stopAudioSynchronization();
                }

                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
            }

            isPlaying = play;
        }
    }


    private void unlockNextStory(String currentStoryId) {
        // Access the database
        AppDatabase db = AppDatabase.getDatabase(this);
        BibleDao bibleDao = db.bibleDao();

        // Use an executor to perform the database operations in the background
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Log.d("UnlockNextStory", "Fetching current story with ID: " + currentStoryId);

            // Fetch the current story by ID
            ModelBible currentStory = bibleDao.getStoryById(currentStoryId);
            if (currentStory != null) {
                Log.d("UnlockNextStory", "Current story fetched: " + currentStory.getId() +
                        ", Count: " + currentStory.getCount() +
                        ", isCompleted: " + currentStory.getIsCompleted());

                // Mark the current story as "completed"
                currentStory.setIsCompleted("completed");
                bibleDao.update(currentStory);
                Log.d("UnlockNextStory", "Current story marked as completed: " + currentStory.getId());

                // Fetch the next story based on the count column
                int nextCount = currentStory.getCount() + 1; // Increment the count to find the next story
                ModelBible nextStory = bibleDao.getStoryByCount(nextCount);

                if (nextStory != null) {
                    Log.d("UnlockNextStory", "Next story fetched: " + nextStory.getId() +
                            ", Count: " + nextStory.getCount() +
                            ", isCompleted: " + nextStory.getIsCompleted());

                    if ("locked".equalsIgnoreCase(nextStory.getIsCompleted())) {
                        // Unlock the next story
                        nextStory.setIsCompleted("unlocked");
                        bibleDao.update(nextStory);
                        Log.d("UnlockNextStory", "Next story unlocked: " + nextStory.getId());

                        // Notify the user
                        runOnUiThread(() -> Toast.makeText(BiblePlay.this, "Next story unlocked!", Toast.LENGTH_SHORT).show());
                    } else {
                        Log.d("UnlockNextStory", "Next story is already unlocked or completed: " + nextStory.getId());
                    }
                } else {
                    Log.d("UnlockNextStory", "No story found with count: " + nextCount);
                }
            } else {
                Log.e("UnlockNextStory", "Current story not found with ID: " + currentStoryId);
            }

            // Notify the user about the current story update
            runOnUiThread(() -> Toast.makeText(BiblePlay.this, "Current story marked as completed!", Toast.LENGTH_SHORT).show());
        });
    }




    @Override
    protected void onResume() {
        super.onResume();
        checkIfStoryIsFavorite();
    }
    private void checkIfStoryIsFavorite() {
        String storyId = getCurrentStoryId(); // Get the current story ID as a string
        AppDatabase db = AppDatabase.getDatabase(this);
        FavoriteDao favoriteDao = db.FavoriteDao();

        // Use an executor to perform the database operation in the background
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Check if the favorite exists for the current user
            Modelfavoritelist existingFavorite = favoriteDao.getFavoriteByStoryIdAndUserId(storyId);

            runOnUiThread(() -> {
                if (existingFavorite != null) {
                    // If favorite exists, set button color to gray
                    addplaylist.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
                } else {
                    // If favorite doesn't exist, set button color to green
                    addplaylist.setBackgroundTintList(getResources().getColorStateList(R.color.orangelight));
                }
            });
        });
    }

    private void addStoryToFavorites() {
        String storyId = getCurrentStoryId(); // Get the current story ID as a string
        Log.d("BiblePlay", "Current Story ID: " + storyId); // Debugging log

        String storyTitle = titleView.getText().toString(); // Get story title from UI

        AppDatabase db = AppDatabase.getDatabase(this);
        FavoriteDao favoriteDao = db.FavoriteDao();
        BibleDao bibleDao = db.bibleDao(); // DAO for the stories

        // Use an executor to perform the database operation in the background
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Query to check if the story exists in the Bible table
            ModelBible story = bibleDao.getStoryById(storyId); // Use the storyId as a string
            Log.d("BiblePlay", "Story Retrieved: " + (story != null)); // Debugging log

            if (story != null) {
                // Check if the favorite already exists for the current user
                Modelfavoritelist existingFavorite = favoriteDao.getFavoriteByStoryIdAndUserId(storyId);

                if (existingFavorite != null) {
                    // If favorite already exists, remove it from favorites
                    favoriteDao.delete(existingFavorite); // Delete operation should be in background
                    runOnUiThread(() -> {
                        addplaylist.setBackgroundTintList(getResources().getColorStateList(R.color.orangelight)); // Change to green
                        Toast.makeText(BiblePlay.this, "Removed from Favorites: " + storyTitle, Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // If favorite doesn't exist, insert it
                    Modelfavoritelist favorite = new Modelfavoritelist();
                    favorite.setStoryId(storyId); // Set the existing storyId
                    favorite.setUserId(getCurrentUserId()); // Set the current user ID
                    favorite.setTitle(storyTitle); // Set the story title

                    try {
                        favoriteDao.insert(favorite); // Insert operation should be in background
                        runOnUiThread(() -> {
                            addplaylist.setBackgroundTintList(getResources().getColorStateList(R.color.gray)); // Change to gray
                            Toast.makeText(BiblePlay.this, "Added to Favorites: " + storyTitle, Toast.LENGTH_SHORT).show();
                        });
                    } catch (SQLiteConstraintException e) {
                        Log.e("BiblePlay", "Foreign key constraint failed: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(BiblePlay.this, "Failed to add to favorites.", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(BiblePlay.this, "Story does not exist.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }




    // Example method to get the current story ID, you might already have this implemented
    private String getCurrentStoryId() {
        // Implement logic to return the current story ID
        // For example, you might want to keep track of it in an instance variable or pass it around
        //return getIntent().getStringExtra("id");
        return getIntent().getStringExtra("id");
    }


    private String getCurrentUserId() {
        // Retrieve the current user ID from Firebase or shared preferences, etc.
        // Assuming you are using Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }
    private void startAudioSynchronization() {
        // Stop any ongoing recognition before starting a new one
        stopAudioSynchronization();

        // Using a handler to periodically fetch the current position and simulate subtitle sync
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    updateStoryText(currentPosition);
                    handler.postDelayed(this, 1000); // Update every 1 second
                }
            }
        }, 1000);
    }

    private void updateStoryText(int currentPosition) {
        // Logic to update the storyView text based on the current position of the audio
        // This is a placeholder implementation and should be updated with actual subtitle logic
        storyView.setText("Playing at: " + currentPosition + " ms");
    }

    private void stopAudioSynchronization() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void restartStory() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
            togglePlayPause(true); // Restart audio
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopAudioSynchronization();
        super.onDestroy();
    }

    private void setThumbnail(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.image)
                    .error(R.drawable.image)
                    .into(thumbnail);
        } else {
            thumbnail.setImageResource(R.drawable.image);
        }
    }

    private void toggleRepeatMode() {
        isRepeatEnabled = !isRepeatEnabled;

        if (isRepeatEnabled) {
            repeatButton.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
            Toast.makeText(this, "Repeat mode enabled", Toast.LENGTH_SHORT).show();
        } else {
            repeatButton.setBackgroundTintList(getResources().getColorStateList(R.color.greenlightning));
            Toast.makeText(this, "Repeat mode disabled", Toast.LENGTH_SHORT).show();
        }

        if (mediaPlayer != null) {
            mediaPlayer.setLooping(isRepeatEnabled);
        }
    }

}
