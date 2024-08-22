package com.example.myapplication.fragment.biblegames.quizgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class ScoreActivity extends AppCompatActivity {

    private TextView scoreText;
    private Button okayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_score);

        // Get the score and total questions from the Intent
        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 5);

        // Calculate the percentage score
        double percentage = ((double) score / totalQuestions) * 100;

        // Initialize views
        scoreText = findViewById(R.id.score);
        okayButton = findViewById(R.id.okaybtn);

        // Display the score
        scoreText.setText(String.valueOf(score));

        // Determine if the user passed or failed
        if (percentage >= 50) {
            okayButton.setText("Done");
        } else {
            okayButton.setText("Try Again");
        }

        okayButton.setOnClickListener(v -> {
            if (percentage >= 50) {
                finish(); // Finish the activity if the user passed
            } else {
                // Restart the QuizGame activity if the user didn't pass
                Intent intent = new Intent(ScoreActivity.this, QuizGame.class);
                startActivity(intent);
                finish(); // Close the ScoreActivity to prevent the user from going back
            }
        });
    }
}
