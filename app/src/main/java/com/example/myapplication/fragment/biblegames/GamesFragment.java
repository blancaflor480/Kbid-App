package com.example.myapplication.fragment.biblegames;

import android.content.Intent;
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
    TextView playQuiz, playPics;  // Declare the ImageView for playQuiz

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_games); // Ensure the correct layout is used

        Log.d("BibleActivity", "RecyclerView and Adapter setup complete.");

        // Initialize arrowback ImageView
        arrowback = findViewById(R.id.arrowback);

        // Initialize playQuiz ImageView
        playQuiz = findViewById(R.id.playquiz);  // Ensure you have an ImageView or Button with this ID in your XML
        playPics = findViewById(R.id.playpics);  // Ensure you have an ImageView or Button with this ID in your XML

        // Set the click listener for arrowback
        arrowback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();  // This will handle back navigation
            }
        });

        // Set the click listener for playQuiz
        playQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to QuizGame activity
                Intent intent = new Intent(GamesFragment.this, QuizGame.class);
                startActivity(intent);
            }
        });

        // Set the click listener for playQuiz
        playPics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to QuizGame activity
                Intent intent = new Intent(GamesFragment.this, FourPicOneword.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("BibleActivity", "Navigating back to FragmentHome");
    }
}
