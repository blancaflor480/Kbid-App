package com.example.myapplication.fragment.biblestories.playlist;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.database.BibleDatabaseHelper;
import com.example.myapplication.database.favorite.FavoriteDao;
import com.example.myapplication.fragment.biblestories.ModelBible;
import com.example.myapplication.fragment.biblestories.favoritelist.Modelfavoritelist;
import com.example.myapplication.database.AppDatabase;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BiblePlay extends AppCompatActivity {

    private TextView titleView, verseView, storyView;
    private ImageView thumbnail, arrowback;
    private TextToSpeech textToSpeech;
    private String[] sentences;
    private int currentSentenceIndex = 0;
    private boolean isPlaying = false; // Track play/pause state
    private boolean isRepeatEnabled = false; // Track repeat state
    private ImageButton playButton, pauseButton, repeatButton, restartButton, nextButton, previousButton;
    private int lastSpokenSentenceIndex = -1;
    private LinearLayout addplaylist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_strories);

        // Initialize views
        titleView = findViewById(R.id.title);
        verseView = findViewById(R.id.verse);
        repeatButton = findViewById(R.id.buttonrepeat);
        restartButton = findViewById(R.id.buttonrestart); // Reference to the restart button
        storyView = findViewById(R.id.textstory);
        thumbnail = findViewById(R.id.thumbnail);
        arrowback = findViewById(R.id.arrowback);
        playButton = findViewById(R.id.buttonplay); // Get reference to the play button
        pauseButton = findViewById(R.id.buttonpause); // Get reference to the pause button
        nextButton = findViewById(R.id.buttonnext); // Reference to the next button
        previousButton = findViewById(R.id.buttonprevious); // Reference to the previous button
        addplaylist = findViewById(R.id.addplaylist);




        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                textToSpeech.setSpeechRate(0.75f); // Set speech rate to 75% for slower narration
                textToSpeech.setPitch(1.2f); // Adjust pitch for expressiveness

                // Set UtteranceProgressListener
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        runOnUiThread(() -> {
                            playButton.setEnabled(true); // Enable play button during narration
                            pauseButton.setEnabled(true); // Enable pause button during narration
                        });
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> {
                            currentSentenceIndex++;
                            if (currentSentenceIndex < sentences.length) {
                                fadeOutText(storyView, () -> readNextSentence()); // Read the next sentence
                            } else {
                                if (isRepeatEnabled) {
                                    // Repeat the story from the beginning
                                    currentSentenceIndex = 0;
                                    readNextSentence();
                                } else {
                                    // End of narration handling
                                    isPlaying = false; // Reset playing state
                                    playButton.setVisibility(View.VISIBLE); // Show play button
                                    pauseButton.setVisibility(View.GONE); // Hide pause button
                                    currentSentenceIndex = 0;
                                    lastSpokenSentenceIndex = -1; // Reset for next time
                                    playButton.setEnabled(true);
                                    loadNextStory(); // Automatically load the next story
// Enable play button again
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> playButton.setEnabled(true)); // Enable play button on error
                    }
                });
            }
        });

        // Get data from intent
        String title = getIntent().getStringExtra("title");
        String verse = getIntent().getStringExtra("verse");
        String description = getIntent().getStringExtra("description");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Set data to views
        titleView.setText(title);
        verseView.setText(verse);
        setThumbnail(imageUrl);

        // Initialize the story text with only the first sentence
        sentences = description.split("\\. "); // Split text into sentences
        if (sentences.length > 0) {
            storyView.setText(sentences[0]); // Set only the first sentence initially
        }

        // Set click listeners for play, pause, repeat, restart, next, and previous buttons
        arrowback.setOnClickListener(v -> onBackPressed());
        playButton.setOnClickListener(v -> togglePlayPause(true));
        pauseButton.setOnClickListener(v -> togglePlayPause(false));
        repeatButton.setOnClickListener(v -> toggleRepeatMode());
        restartButton.setOnClickListener(v -> restartStory()); // Set the restart button click listener
        nextButton.setOnClickListener(v -> goToNextSentence()); // Next button listener
        previousButton.setOnClickListener(v -> goToPreviousSentence()); // Previous button listener

        // Initially, show play button and hide pause button
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        repeatButton.setBackgroundTintList(getResources().getColorStateList(R.color.greenlightning)); // Set initial color
    }

    private void setThumbnail(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.image) // Default image while loading
                    .error(R.drawable.image) // Default image in case of error
                    .into(thumbnail);
        } else {
            thumbnail.setImageResource(R.drawable.image);
        }
    }

    private void togglePlayPause(boolean play) {
        if (play) {
            // Resume narration from the last spoken sentence
            if (lastSpokenSentenceIndex >= 0 && lastSpokenSentenceIndex < sentences.length) {
                currentSentenceIndex = lastSpokenSentenceIndex;
                readNextSentence();
            } else {
                if (currentSentenceIndex < sentences.length) {
                    readNextSentence();
                } else {
                    currentSentenceIndex = 0;
                    readNextSentence();
                }
            }
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        } else {
            // Pause the narration
            textToSpeech.stop(); // Stop speech temporarily
            lastSpokenSentenceIndex = currentSentenceIndex; // Store the last spoken sentence index
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        }
        isPlaying = play;
    }

    private void toggleRepeatMode() {
        isRepeatEnabled = !isRepeatEnabled; // Toggle the repeat state

        if (isRepeatEnabled) {
            // Change the background color to green when repeat is enabled
            repeatButton.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
        } else {
            // Change the background color back to gray when repeat is disabled
            repeatButton.setBackgroundTintList(getResources().getColorStateList(R.color.greenlightning));
        }
    }

    private void restartStory() {
        // Reset the story to the beginning
        currentSentenceIndex = 0;
        lastSpokenSentenceIndex = -1; // Reset the last spoken index
        storyView.setText(sentences[0]); // Display the first sentence
        togglePlayPause(true); // Start playing from the beginning
    }

    private void goToNextSentence() {
        if (currentSentenceIndex < sentences.length - 1) {
            currentSentenceIndex++;
            storyView.setText(sentences[currentSentenceIndex]);
            textToSpeech.speak(sentences[currentSentenceIndex], TextToSpeech.QUEUE_FLUSH, null, "SentenceID");
            fadeInText(storyView);
        }
    }

    private void goToPreviousSentence() {
        if (currentSentenceIndex > 0) {
            currentSentenceIndex--;
            storyView.setText(sentences[currentSentenceIndex]);
            textToSpeech.speak(sentences[currentSentenceIndex], TextToSpeech.QUEUE_FLUSH, null, "SentenceID");
            fadeInText(storyView);
        }
    }

    private void readNextSentence() {
        if (currentSentenceIndex < sentences.length) {
            String currentSentence = sentences[currentSentenceIndex];
            storyView.setText(currentSentence);
            textToSpeech.speak(currentSentence, TextToSpeech.QUEUE_FLUSH, null, "SentenceID");
            fadeInText(storyView);
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        } else {
            // End of narration handling
            isPlaying = false;
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            currentSentenceIndex = 0;
            playButton.setEnabled(true);
        }
    }


    private void loadNextStory() {
        // Assuming you have a way to get the current story ID, you should define it
        int currentStoryId = getCurrentStoryId(); // Implement this method to retrieve the current story ID

        BibleDatabaseHelper dbHelper = new BibleDatabaseHelper(this);
        ModelBible nextStory = dbHelper.getNextStory(currentStoryId);

        if (nextStory != null) {
            // Update UI with the new story details
            titleView.setText(nextStory.getTitle());
            verseView.setText(nextStory.getVerse());
            sentences = nextStory.getDescription().split("\\. ");
            storyView.setText(sentences[0]);
            currentSentenceIndex = 0; // Reset index for the new story
            readNextSentence(); // Start reading the next story
        } else {
            // No more stories
            // Optionally, handle the end of stories (e.g., show a message)
        }
    }

    // Example method to get the current story ID, you might already have this implemented
    private int getCurrentStoryId() {
        // Implement logic to return the current story ID
        // For example, you might want to keep track of it in an instance variable or pass it around
        return 0; // Replace with actual logic
    }



    private void fadeInText(TextView textView) {
        textView.setAlpha(0f);
        textView.animate().alpha(1f).setDuration(1000);
    }

    private void fadeOutText(TextView textView, Runnable endAction) {
        textView.animate().alpha(0f).setDuration(1000).withEndAction(endAction);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        lastSpokenSentenceIndex = -1;
        super.onDestroy();
    }
}
