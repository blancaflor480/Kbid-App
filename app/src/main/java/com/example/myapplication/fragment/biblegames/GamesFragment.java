package com.example.myapplication.fragment.biblegames;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.fragment.biblegames.fourpiconeword.FourPicOneword;
import com.example.myapplication.fragment.biblegames.quizgame.QuizGame;  // Import the QuizGame activity

public class GamesFragment extends AppCompatActivity {

    ImageView arrowback;
    TextView playQuiz, playPics;
    int userId = 1; // Declare the ImageView for playQuiz
    String email;
    MediaPlayer clickSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_games); // Ensure the correct layout is used

        Log.d("BibleActivity", "RecyclerView and Adapter setup complete.");

        // Initialize views
        arrowback = findViewById(R.id.arrowback);
        playPics = findViewById(R.id.playpics);  // Ensure you have an ImageView or Button with this ID in your XML

        // Initialize the MediaPlayer for click sound
        clickSound = MediaPlayer.create(this, R.raw.clickpop);

        // Set the click listener for arrowback
        arrowback.setOnClickListener(v -> {
            playClickSound(); // Play sound effect
            onBackPressed(); // Handle back press
        });

        // Set the click listener for playPics
        playPics.setOnClickListener(v -> {
            // Navigate to GameDescriptionFourPics activity
            Intent intent = new Intent(GamesFragment.this, GameDescriptionFourPics.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
        });
    }

    // Function to play the click sound
    private void playClickSound() {
        if (clickSound != null) {
            clickSound.start(); // Play the sound
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clickSound != null) {
            clickSound.release(); // Release the MediaPlayer resources
            clickSound = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("BibleActivity", "Navigating back to FragmentHome");
    }
}
