package com.example.myapplication.fragment.biblegames.fourpiconeword;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.myapplication.R;
import com.example.myapplication.fragment.biblegames.GamesFragment;

public class NextActivity extends AppCompatActivity {


    AppCompatButton nextButton;
    int userId = 1;
    private LinearLayout answerBoxesLayout;
    private MediaPlayer mediaPlayer;
    private boolean isSoundOn = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fourpiconeword_next); // Ensure the correct layout is used



        mediaPlayer = MediaPlayer.create(this, R.raw.conragtseffect);
        mediaPlayer.setLooping(true);
        Log.d("BibleActivity", "RecyclerView and Adapter setup complete.");
        answerBoxesLayout = findViewById(R.id.answerBoxes);

        // Retrieve the correct answer from the Intent
        String correctAnswer = getIntent().getStringExtra("CORRECT_ANSWER");

        if (correctAnswer == null) {
            Log.e("NextActivity", "Correct answer is null");
            correctAnswer = ""; // Initialize to avoid NullPointerException
        }

        // Display the correct answer in the answer boxes
        displayCorrectAnswer(correctAnswer);


        // Initialize arrowback ImageView

        nextButton = findViewById(R.id.nextButton);

        // Set the click listener for arrowback

        // Set the click listener for the nextButton
        // Handle back navigation
        nextButton.setOnClickListener(v -> {
            // Navigate back to FourPicOneword activity
            Intent intent = new Intent(NextActivity.this, FourPicOneword.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish(); // Optionally finish the current activity
        });

        mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the music when the activity is not in the foreground
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the music when the activity comes back to the foreground
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }
    private void displayCorrectAnswer(String correctAnswer) {
        int answerLength = correctAnswer.length();
        TextView[] answerBoxes = new TextView[answerLength];

        for (int i = 0; i < answerLength; i++) {
            TextView answerBox = new TextView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    150, 150); // Set width and height in pixels

            answerBox.setLayoutParams(layoutParams);
            answerBox.setTextSize(24);
            answerBox.setGravity(Gravity.CENTER);
            answerBox.setBackgroundResource(R.drawable.rounded_box);
            answerBox.setTextColor(getResources().getColor(android.R.color.white));
            answerBox.setText(String.valueOf(correctAnswer.charAt(i)));
            ((LinearLayout.LayoutParams) answerBox.getLayoutParams()).setMargins(5, 0, 5, 0);

            answerBoxes[i] = answerBox;
            answerBoxesLayout.addView(answerBox);
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("BibleActivity", "Navigating back to FragmentHome");
    }


}
