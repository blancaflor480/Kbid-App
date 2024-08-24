package com.example.myapplication.fragment.biblegames.fourpiconeword;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.drawable.ColorDrawable;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FourPicOneword extends AppCompatActivity {
    ImageView arrowback;

    // Answer boxes container
    private LinearLayout answerBoxesLayout;
    private TextView[] answerBoxes;
    private int currentAnswerIndex = 0;
    private boolean isAnswerIncorrect = false; // Track if the answer was incorrect

    // Keyboard buttons
    private Button[] keyboardButtons;

    // Correct answer
    private String correctAnswer = "JESUS"; // Example correct answer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_fourpiconeword); // Ensure the correct layout is used

        Log.d("BibleActivity", "FourPicOneWord Activity started.");

        // Initialize arrowback ImageView
        arrowback = findViewById(R.id.arrowback);

        // Set the click listener for arrowback
        arrowback.setOnClickListener(v -> onBackPressed());

        // Initialize answer boxes container
        answerBoxesLayout = findViewById(R.id.answerBoxes);

        // Dynamically create answer boxes based on the correct answer length
        setupAnswerBoxes();

        // Initialize keyboard buttons
        keyboardButtons = new Button[]{
                findViewById(R.id.keyboardButton1),
                findViewById(R.id.keyboardButton2),
                findViewById(R.id.keyboardButton3),
                findViewById(R.id.keyboardButton4),
                findViewById(R.id.keyboardButton5),
                findViewById(R.id.keyboardButton6),
                findViewById(R.id.keyboardButton7),
                findViewById(R.id.keyboardButton8),
        };

        // Generate random letters and set up the keyboard
        setupKeyboard();

        // Set up listener for delete button
        ImageButton deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> onDeleteButtonClick());

        // Set up listener for shuffle button
        ImageButton shuffleButton = findViewById(R.id.shuffle);
        shuffleButton.setOnClickListener(v -> shuffleKeyboard());
    }

    // Inside the onCreate method, replace the setup for answerBoxes
    private void setupAnswerBoxes() {
        int answerLength = correctAnswer.length();
        answerBoxes = new TextView[answerLength];

        for (int i = 0; i < answerLength; i++) {
            TextView answerBox = new TextView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    150, 150); // Set width and height in pixels

            answerBox.setLayoutParams(layoutParams);
            answerBox.setTextSize(24);
            answerBox.setGravity(Gravity.CENTER); // Center the text
            answerBox.setBackgroundResource(R.drawable.rounded_box);
            answerBox.setTextColor(getResources().getColor(android.R.color.white));
            answerBox.setTag(null); // Store the reference to the corresponding button
            answerBox.setText(""); // Ensure text is empty initially
            ((LinearLayout.LayoutParams) answerBox.getLayoutParams()).setMargins(5, 0, 5, 0);

            answerBoxes[i] = answerBox;
            answerBoxesLayout.addView(answerBox);
        }
    }

    // Inside onKeyboardButtonClick
    private void onKeyboardButtonClick(View view) {
        Button clickedButton = (Button) view;
        String letter = clickedButton.getText().toString();

        // Add the letter to the current answer box
        if (currentAnswerIndex < answerBoxes.length) {
            answerBoxes[currentAnswerIndex].setText(letter);

            // Attempt to extract the background color
            Drawable background = clickedButton.getBackground();
            int originalBackgroundColor = Color.WHITE; // Default color if extraction fails

            if (background instanceof ColorDrawable) {
                originalBackgroundColor = ((ColorDrawable) background).getColor();
            } else if (background instanceof RippleDrawable) {
                // Extract color from RippleDrawable's background layer
                RippleDrawable rippleDrawable = (RippleDrawable) background;
                Drawable rippleBackground = rippleDrawable.findDrawableByLayerId(android.R.id.background);
                if (rippleBackground instanceof ColorDrawable) {
                    originalBackgroundColor = ((ColorDrawable) rippleBackground).getColor();
                }
            }

            clickedButton.setTag(originalBackgroundColor); // Store the original color in tag
            answerBoxes[currentAnswerIndex].setTag(clickedButton); // Store the reference of the button
            currentAnswerIndex++;

            // Disable the clicked button and change its background to darker gray
            clickedButton.setEnabled(false);
            clickedButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }

        // Check if all answer boxes are filled
        if (currentAnswerIndex == answerBoxes.length) {
            validateAnswer(); // Automatically validate the answer
        }
    }

    // Inside onDeleteButtonClick method
    private void onDeleteButtonClick() {
        if (currentAnswerIndex > 0) {
            currentAnswerIndex--;

            // Get the associated button from the tag
            Button associatedButton = (Button) answerBoxes[currentAnswerIndex].getTag();
            if (associatedButton != null) {
                // Re-enable the corresponding keyboard button
                associatedButton.setEnabled(true);

                // Restore the original background color from the tag
                int originalBackgroundColor = (int) associatedButton.getTag();
                associatedButton.setBackgroundColor(originalBackgroundColor);
            }

            // Clear the last filled answer box and its tag
            answerBoxes[currentAnswerIndex].setText("");
            answerBoxes[currentAnswerIndex].setTag(null);

            // Reset all answer box colors if it was an incorrect attempt
            if (isAnswerIncorrect) {
                resetAnswerBoxColors();
            }
        }
    }

    // Reset the background color of all answer boxes to default
    private void resetAnswerBoxColors() {
        for (TextView answerBox : answerBoxes) {
            answerBox.setBackgroundResource(R.drawable.rounded_box); // Default background
        }
        isAnswerIncorrect = false; // Reset the incorrect answer flag
    }

    // Shuffle the keyboard letters
    private void shuffleKeyboard() {
        setupKeyboard(); // Regenerate and shuffle letters
    }

    // Generate random letters including the correct answer letters and set up the keyboard
    private void setupKeyboard() {
        List<String> letters = new ArrayList<>();

        // Add letters of the correct answer
        for (char letter : correctAnswer.toCharArray()) {
            letters.add(String.valueOf(letter));
        }

        // Add random letters to fill up the keyboard
        while (letters.size() < keyboardButtons.length) {
            char randomLetter = (char) ('A' + (int) (Math.random() * 26));  // Random letter from A-Z
            letters.add(String.valueOf(randomLetter));
        }

        // Shuffle the letters
        Collections.shuffle(letters);

        // Assign letters to keyboard buttons
        for (int i = 0; i < keyboardButtons.length; i++) {
            keyboardButtons[i].setText(letters.get(i));
            keyboardButtons[i].setEnabled(true);
            keyboardButtons[i].setOnClickListener(this::onKeyboardButtonClick); // Set the click listener for each button
        }
    }

    // Validate the final answer
    private void validateAnswer() {
        StringBuilder userAnswer = new StringBuilder();
        for (TextView answerBox : answerBoxes) {
            userAnswer.append(answerBox.getText().toString());
        }

        if (userAnswer.toString().equals(correctAnswer)) {
            // Correct answer - fill the answer boxes and navigate to next activity
            Log.d("BibleActivity", "Correct Answer!");
            for (int i = 0; i < answerBoxes.length; i++) {
                answerBoxes[i].setText(String.valueOf(correctAnswer.charAt(i))); // Show the correct answer
            }
            // Optionally navigate to the next screen here
            Intent intent = new Intent(FourPicOneword.this, NextActivity.class);
            intent.putExtra("CORRECT_ANSWER", correctAnswer);
            startActivity(intent);
        } else {
            // Incorrect answer - handle accordingly (e.g., show an error message)
            Log.d("BibleActivity", "Incorrect Answer!");
            for (TextView answerBox : answerBoxes) {
                answerBox.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light)); // Change background to red
            }
            isAnswerIncorrect = true; // Set the incorrect answer flag
            // Optionally show a message to the user
            // Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("BibleActivity", "Navigating back to FragmentHome");
    }
}
