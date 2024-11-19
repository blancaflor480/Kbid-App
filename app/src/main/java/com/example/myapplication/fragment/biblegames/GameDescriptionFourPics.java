package com.example.myapplication.fragment.biblegames;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.myapplication.R;
import com.example.myapplication.fragment.biblegames.fourpiconeword.FourPicOneword;

public class GameDescriptionFourPics extends AppCompatActivity {
    ImageView arrowback;
    AppCompatButton howtoplay;
    TextView play, level;
    int userId = 1;
    MediaPlayer clickSound; // MediaPlayer for sound effects

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.askplayfourpics);

        Log.d("BibleActivity", "RecyclerView and Adapter setup complete.");

        // Initialize views
        arrowback = findViewById(R.id.arrowback);
        play = findViewById(R.id.play);
        howtoplay = findViewById(R.id.howtoplay);
        level = findViewById(R.id.level);

        // Initialize the MediaPlayer for click sound
        clickSound = MediaPlayer.create(this, R.raw.clickpop);

        // Set the click listener for the back arrow
        arrowback.setOnClickListener(v -> {
            playClickSound(); // Play sound effect
            onBackPressed();
        });

        // Set the click listener for play button
        play.setOnClickListener(v -> {
            playClickSound(); // Play sound effect
            Intent intent = new Intent(GameDescriptionFourPics.this, FourPicOneword.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        // Set the click listener for the "How to Play" button
        howtoplay.setOnClickListener(v -> {
            playClickSound(); // Play sound effect
            showHowToPlayDialog();
        });
    }

    private void showHowToPlayDialog() {
        // Define instructions, images, and dot indicators
        String[] instructions = {
                "Four pictures will be displayed on the screen. Each picture represents a hint that leads to one common word related to the Bible.",
                "Use the letters below to form the word that connects the images.",
                "Complete as many levels as you can to test your knowledge!"
        };
        int[] instructionImages = {R.raw.instruction1, R.raw.instruction2, R.raw.instruction3};
        int[] dotIndicators = {R.id.dot1, R.id.dot2, R.id.dot3};

        // Track the current step
        final int[] currentIndex = {0};

        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_howtoplay, null);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Get views from the layout
        ImageView instructionImage = dialogView.findViewById(R.id.instruction);
        TextView instructionText = dialogView.findViewById(R.id.textinstruction);
        ImageButton nextButton = dialogView.findViewById(R.id.next);
        ImageButton previousButton = dialogView.findViewById(R.id.previous);
        AppCompatButton playButton = dialogView.findViewById(R.id.play);

        // Set initial content
        instructionImage.setImageResource(instructionImages[currentIndex[0]]);
        instructionText.setText(instructions[currentIndex[0]]);
        updateDots(dotIndicators, currentIndex[0], dialogView);

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Next button functionality
        nextButton.setOnClickListener(v -> {
            if (currentIndex[0] < instructions.length - 1) {
                currentIndex[0]++;
                updateContent(instructionImage, instructionText, instructions, instructionImages, currentIndex[0]);
                updateDots(dotIndicators, currentIndex[0], dialogView);
            }
        });

        // Previous button functionality
        previousButton.setOnClickListener(v -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                updateContent(instructionImage, instructionText, instructions, instructionImages, currentIndex[0]);
                updateDots(dotIndicators, currentIndex[0], dialogView);
            }
        });

        // "Okay" button functionality
        playButton.setOnClickListener(v -> {
            dialog.dismiss();
            playClickSound(); // Play sound effect when dismissing the dialog
        });
    }

    // Helper function to play the click sound
    private void playClickSound() {
        if (clickSound != null) {
            clickSound.start(); // Play the sound
        }
    }

    // Helper function to update image and text with animation
    private void updateContent(ImageView imageView, TextView textView, String[] texts, int[] images, int index) {
        imageView.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
        textView.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
        imageView.setImageResource(images[index]);
        textView.setText(texts[index]);
    }

    // Helper function to update dot indicators
    private void updateDots(int[] dotIndicators, int currentIndex, View dialogView) {
        for (int i = 0; i < dotIndicators.length; i++) {
            ImageView dot = dialogView.findViewById(dotIndicators[i]);
            if (i == currentIndex) {
                dot.setImageResource(R.drawable.dot_active); // Active dot drawable
            } else {
                dot.setImageResource(R.drawable.dot_inactive); // Inactive dot drawable
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("BibleActivity", "Navigating back to FragmentHome");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clickSound != null) {
            clickSound.release(); // Release the MediaPlayer resources
            clickSound = null;
        }
    }
}
