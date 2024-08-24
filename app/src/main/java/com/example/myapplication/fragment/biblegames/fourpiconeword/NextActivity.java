package com.example.myapplication.fragment.biblegames.fourpiconeword;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class NextActivity extends AppCompatActivity {

    ImageView arrowback;
    private LinearLayout answerBoxesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fourpiconeword_next); // Ensure the correct layout is used

        Log.d("BibleActivity", "RecyclerView and Adapter setup complete.");
        answerBoxesLayout = findViewById(R.id.answerBoxes);

        // Retrieve the correct answer from the Intent
        String correctAnswer = getIntent().getStringExtra("CORRECT_ANSWER");

        // Display the correct answer in the answer boxes
        displayCorrectAnswer(correctAnswer);
        // Initialize arrowback ImageView
        arrowback = findViewById(R.id.arrowback);


        // Set the click listener for arrowback
        arrowback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();  // This will handle back navigation
            }
        });


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
