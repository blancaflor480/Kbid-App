package com.example.myapplication.fragment.biblestories.playlist;

import android.app.Dialog;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;

import android.speech.SpeechRecognizer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;


import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.database.BibleDao;
import com.example.myapplication.database.BibleDatabaseHelper;
import com.example.myapplication.database.achievement.StoryAchievementDao;
import com.example.myapplication.database.favorite.FavoriteDao;
import com.example.myapplication.fragment.achievement.StoryAchievementModel;
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

                        File localFile = getLocalAudioFile(audioUrl);
                        if (localFile != null && localFile.exists()) {
                            mediaPlayer.setDataSource(localFile.getAbsolutePath());
                        } else {
                            // If no local file, try online URL
                            mediaPlayer.setDataSource(this, Uri.parse(audioUrl));
                        }


                   //   mediaPlayer.setDataSource(this, Uri.parse(audioUrl));
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


    private File getLocalAudioFile(String audioUrl) {
        try {
            // Remove the query part of the URL (if any)
            String fileUrlWithoutQuery = audioUrl.split("\\?")[0];

            // Decode the file name from the URL
            String fileName = fileUrlWithoutQuery.substring(fileUrlWithoutQuery.lastIndexOf('/') + 1);
            fileName = URLDecoder.decode(fileName, "UTF-8"); // Decode URL-encoded characters

            // Make sure the file name doesn't have slashes (replace with underscores)
            fileName = fileName.replace("/", "_");

            // Create the storage directory matching the download method
            File storageDir = new File(getFilesDir(), "audio_stories/stories/audio");

            // Ensure all parent directories are created
            if (!storageDir.mkdirs() && !storageDir.exists()) {
                Log.e("AudioFile", "Failed to create directory: " + storageDir.getAbsolutePath());
                return null;
            }

            // Create the file in the storage directory
            File audioFile = new File(storageDir, fileName);

            // Log the file path for debugging
            Log.d("AudioFile", "Searching for local audio file: " + audioFile.getAbsolutePath());

            return audioFile.exists() ? audioFile : null;
        } catch (Exception e) {
            Log.e("AudioFile", "Error finding local audio file", e);
            return null;
        }
    }

    private void unlockNextStory(String currentStoryId) {
        // Access the database
        AppDatabase db = AppDatabase.getDatabase(this);
        BibleDao bibleDao = db.bibleDao();
        StoryAchievementDao storyAchievementDao = db.storyAchievementDao(); // Assuming you have an AchievementDao interface

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

                // Update the achievement for the completed story
                StoryAchievementModel achievement = storyAchievementDao.getAchievementByStoryId(currentStoryId);
                if (achievement != null) {
                    achievement.setIsCompleted("completed");
                    storyAchievementDao.update(achievement);
                    Log.d("UnlockNextStory", "Achievement updated for story ID: " + currentStoryId);
                } else {
                    Log.d("UnlockNextStory", "No achievement found for story ID: " + currentStoryId);
                }

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
                        runOnUiThread(() -> {
                            Toast.makeText(BiblePlay.this, "Next story unlocked!", Toast.LENGTH_SHORT).show();
                            showFinishReadingDialog(); // Show the dialog when next story is unlocked
                        });
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

    private void showFinishReadingDialog() {
        // Create a dialog to show the finish_reading layout
        Dialog finishReadingDialog = new Dialog(this);
        finishReadingDialog.setContentView(R.layout.finish_reading);
        finishReadingDialog.setCancelable(false); // Prevent the dialog from being dismissed by clicking outside

        // Set up the "Done" button to dismiss the dialog
        AppCompatButton doneButton = finishReadingDialog.findViewById(R.id.button_done);
        if (doneButton != null) {
            doneButton.setOnClickListener(v -> finishReadingDialog.dismiss());
        }

        // Show the dialog
        finishReadingDialog.show();
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
        return getIntent().getStringExtra("id");
    }

    private String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }
    private void startAudioSynchronization() {
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
