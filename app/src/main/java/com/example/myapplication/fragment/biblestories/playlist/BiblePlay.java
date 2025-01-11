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
    private Handler storyHandler;
    private List<String> descriptionSentences;
    private int currentSlideIndex = 0;
    private AppCompatButton nextButton, previousButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_strories);
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
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        String title = getIntent().getStringExtra("title");
        String verse = getIntent().getStringExtra("verse");
        audioUrl = getIntent().getStringExtra("audioUrl");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String description = getIntent().getStringExtra("description");
        titleView.setText(title);
        verseView.setText(verse);
        setThumbnail(imageUrl);
        descriptionSentences = splitDescriptionIntoSentences(description);
        updateStoryView();
        nextButton.setOnClickListener(v -> showNextSlide());
        previousButton.setOnClickListener(v -> showPreviousSlide());
        storyHandler = new Handler();
        arrowback.setOnClickListener(v -> onBackPressed());
        playButton.setOnClickListener(v -> togglePlayPause(true));
        pauseButton.setOnClickListener(v -> togglePlayPause(false));
        repeatButton.setOnClickListener(v -> toggleRepeatMode());
        restartButton.setOnClickListener(v -> restartStory());
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        repeatButton.setBackgroundTintList(getResources().getColorStateList(R.color.greenlightning));
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
                            mediaPlayer.setDataSource(this, Uri.parse(audioUrl));
                        }
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        startAudioSynchronization();
                        mediaPlayer.setOnCompletionListener(mp -> {
                            stopAudioSynchronization();
                            playButton.setVisibility(View.VISIBLE);
                            pauseButton.setVisibility(View.GONE);
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
            String fileUrlWithoutQuery = audioUrl.split("\\?")[0];
            String fileName = fileUrlWithoutQuery.substring(fileUrlWithoutQuery.lastIndexOf('/') + 1);
            fileName = URLDecoder.decode(fileName, "UTF-8");
            fileName = fileName.replace("/", "_");
            File storageDir = new File(getFilesDir(), "audio_stories/stories/audio");
            if (!storageDir.mkdirs() && !storageDir.exists()) {
                Log.e("AudioFile", "Failed to create directory: " + storageDir.getAbsolutePath());
                return null;
            }
            File audioFile = new File(storageDir, fileName);
            Log.d("AudioFile", "Searching for local audio file: " + audioFile.getAbsolutePath());

            return audioFile.exists() ? audioFile : null;
        } catch (Exception e) {
            Log.e("AudioFile", "Error finding local audio file", e);
            return null;
        }
    }
    private List<String> splitDescriptionIntoSentences(String description) {
        List<String> sentences = new ArrayList<>();
        if (description != null && !description.isEmpty()) {
            String[] parts = description.split("\\.\\s+");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    sentences.add(part.trim() + ".");
                }
            }
        }
        return sentences;
    }

    private void showNextSlide() {
        if (!descriptionSentences.isEmpty()) {
            currentSlideIndex = (currentSlideIndex + 1) % descriptionSentences.size();
            updateStoryView();
        }
    }

    private void showPreviousSlide() {
        if (!descriptionSentences.isEmpty()) {
            currentSlideIndex = (currentSlideIndex - 1 + descriptionSentences.size()) % descriptionSentences.size(); // Loop back to end
            updateStoryView();
        }
    }

    private void updateStoryView() {
        if (!descriptionSentences.isEmpty()) {
            storyView.setText(descriptionSentences.get(currentSlideIndex));
        } else {
            storyView.setText("No description available.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        storyHandler.removeCallbacksAndMessages(null);
    }

    private void unlockNextStory(String currentStoryId) {
        AppDatabase db = AppDatabase.getDatabase(this);
        BibleDao bibleDao = db.bibleDao();
        StoryAchievementDao storyAchievementDao = db.storyAchievementDao();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Log.d("UnlockNextStory", "Fetching current story with ID: " + currentStoryId);

            ModelBible currentStory = bibleDao.getStoryById(currentStoryId);
            if (currentStory != null) {
                Log.d("UnlockNextStory", "Current story fetched: " + currentStory.getId() +
                        ", Count: " + currentStory.getCount() +
                        ", isCompleted: " + currentStory.getIsCompleted());

                currentStory.setIsCompleted("completed");
                bibleDao.update(currentStory);
                Log.d("UnlockNextStory", "Current story marked as completed: " + currentStory.getId());

                StoryAchievementModel achievement = storyAchievementDao.getAchievementByStoryId(currentStoryId);
                if (achievement != null) {
                    achievement.setIsCompleted("completed");
                    storyAchievementDao.update(achievement);
                    Log.d("UnlockNextStory", "Achievement updated for story ID: " + currentStoryId);
                } else {
                    Log.d("UnlockNextStory", "No achievement found for story ID: " + currentStoryId);
                }

                int nextCount = currentStory.getCount() + 1; // Increment the count to find the next story
                ModelBible nextStory = bibleDao.getStoryByCount(nextCount);

                if (nextStory != null) {
                    Log.d("UnlockNextStory", "Next story fetched: " + nextStory.getId() +
                            ", Count: " + nextStory.getCount() +
                            ", isCompleted: " + nextStory.getIsCompleted());

                    if ("locked".equalsIgnoreCase(nextStory.getIsCompleted())) {
                        nextStory.setIsCompleted("unlocked");
                        bibleDao.update(nextStory);
                        Log.d("UnlockNextStory", "Next story unlocked: " + nextStory.getId());

                        runOnUiThread(() -> {
                            Toast.makeText(BiblePlay.this, "Next story unlocked!", Toast.LENGTH_SHORT).show();
                            showFinishReadingDialog();
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
            runOnUiThread(() -> Toast.makeText(BiblePlay.this, "Current story marked as completed!", Toast.LENGTH_SHORT).show());
        });
    }
    private void showFinishReadingDialog() {
        Dialog finishReadingDialog = new Dialog(this);
        finishReadingDialog.setContentView(R.layout.finish_reading);
        finishReadingDialog.setCancelable(false);
        AppCompatButton doneButton = finishReadingDialog.findViewById(R.id.button_done);
        if (doneButton != null) {
            doneButton.setOnClickListener(v -> finishReadingDialog.dismiss());
        }
        finishReadingDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfStoryIsFavorite();
    }
    private void checkIfStoryIsFavorite() {
        String storyId = getCurrentStoryId();
        AppDatabase db = AppDatabase.getDatabase(this);
        FavoriteDao favoriteDao = db.FavoriteDao();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Modelfavoritelist existingFavorite = favoriteDao.getFavoriteByStoryIdAndUserId(storyId);

            runOnUiThread(() -> {
                if (existingFavorite != null) {
                    addplaylist.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
                } else {
                    addplaylist.setBackgroundTintList(getResources().getColorStateList(R.color.orangelight));
                }
            });
        });
    }

    private void addStoryToFavorites() {
        String storyId = getCurrentStoryId();
        Log.d("BiblePlay", "Current Story ID: " + storyId); // Debugging log
        String storyTitle = titleView.getText().toString();
        AppDatabase db = AppDatabase.getDatabase(this);
        FavoriteDao favoriteDao = db.FavoriteDao();
        BibleDao bibleDao = db.bibleDao();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ModelBible story = bibleDao.getStoryById(storyId);
            Log.d("BiblePlay", "Story Retrieved: " + (story != null));
            if (story != null) {
                Modelfavoritelist existingFavorite = favoriteDao.getFavoriteByStoryIdAndUserId(storyId);
                if (existingFavorite != null) {
                    favoriteDao.delete(existingFavorite);
                    runOnUiThread(() -> {
                        addplaylist.setBackgroundTintList(getResources().getColorStateList(R.color.orangelight)); // Change to green
                        Toast.makeText(BiblePlay.this, "Removed from Favorites: " + storyTitle, Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Modelfavoritelist favorite = new Modelfavoritelist();
                    favorite.setStoryId(storyId);
                    favorite.setUserId(getCurrentUserId());
                    favorite.setTitle(storyTitle);
                    try {
                        favoriteDao.insert(favorite);
                        runOnUiThread(() -> {
                            addplaylist.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    updateStoryText(currentPosition);
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void updateStoryText(int currentPosition) {
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