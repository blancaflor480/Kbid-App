package com.example.myapplication.fragment.biblegames.fourpiconeword;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWord;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWordDao;
import com.example.myapplication.database.gamesdb.DataFetcher;
import com.example.myapplication.database.gamesdb.Games;
import com.example.myapplication.database.gamesdb.GamesDao;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class FourPicOneword extends AppCompatActivity {
    private static final String TAG = "FourPicOneword";
    private ImageView arrowback;
    private LinearLayout answerBoxesLayout;
    private TextView[] answerBoxes;
    private int currentAnswerIndex = 0;
    private boolean isAnswerIncorrect = false;
    private Button[] keyboardButtons;
    private String correctAnswer;
    private GamesDao gamesDao;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_fourpiconeword);

        // Initialize database
        AppDatabase db = AppDatabase.getDatabase(this);
        gamesDao = db.gamesDao();
        DataFetcher dataFetcher = new DataFetcher(gamesDao);

        // Retrieve the userId from Intent
        userId = getIntent().getIntExtra("USER_ID", -1);


        Log.d(TAG, "FourPicOneWord Activity started.");

        // Initialize arrowback ImageView
        arrowback = findViewById(R.id.arrowback);
        arrowback.setOnClickListener(v -> onBackPressed());

        // Initialize answer boxes container
        answerBoxesLayout = findViewById(R.id.answerBoxes);

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

        // Fetch games from Firestore and update SQLite
        dataFetcher.fetchGamesFromFirestore();

        // Observe the database changes after a delay to ensure data is inserted
        new Handler().postDelayed(this::fetchGameData, 2000); // Delay to allow Firestore fetch to complete
        // Set up listener for delete button
        ImageButton deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> onDeleteButtonClick());

        // Set up listener for shuffle button
        ImageButton shuffleButton = findViewById(R.id.shuffle);
        shuffleButton.setOnClickListener(v -> shuffleKeyboard());
    }

    private void fetchGameData() {
        int userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId == -1) {
            Log.e(TAG, "User ID is invalid.");
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(this);
        LiveData<FourPicsOneWord> currentLevelData = db.fourPicsOneWordDao().getCurrentLevel(userId);

        currentLevelData.observe(this, fourPicsOneWord -> {
            if (fourPicsOneWord != null) {
                int currentLevel = fourPicsOneWord.getCurrentLevel();

                LiveData<List<Games>> liveData = db.gamesDao().getGamesByLevel(currentLevel);
                liveData.observe(this, gamesList -> {
                    if (gamesList != null && !gamesList.isEmpty()) {
                        Games game = gamesList.get(0);
                        correctAnswer = game.getAnswer();

                        // Log fetched answer
                        Log.d(TAG, "Fetched correctAnswer: " + correctAnswer);

                        if (correctAnswer == null) {
                            Log.e(TAG, "Correct answer is null");
                            correctAnswer = "";
                        }

                        String imageUrl1 = game.getImageUrl1();
                        String imageUrl2 = game.getImageUrl2();
                        String imageUrl3 = game.getImageUrl3();
                        String imageUrl4 = game.getImageUrl4();
                        String level = game.getLevel();

                        loadImages(imageUrl1, imageUrl2, imageUrl3, imageUrl4);

                        TextView levelTextView = findViewById(R.id.level);
                        levelTextView.setText("Level " + level);

                        setupAnswerBoxes();
                        setupKeyboard();
                    } else {
                        Log.e(TAG, "No games available for the current level in the database.");
                    }
                });
            } else {
                Log.e(TAG, "User's current level data is not available.");
            }
        });
    }







    private void loadImages(String imageUrl1, String imageUrl2, String imageUrl3, String imageUrl4) {
        Glide.with(this)
                .load(imageUrl1)
                .placeholder(R.drawable.image) // Optional placeholder image
                .error(R.drawable.error_outline) // Optional error image
                .into((ImageView) findViewById(R.id.image1));

        Glide.with(this)
                .load(imageUrl2)
                .placeholder(R.drawable.image)
                .error(R.drawable.error_outline)
                .into((ImageView) findViewById(R.id.image2));

        Glide.with(this)
                .load(imageUrl3)
                .placeholder(R.drawable.image)
                .error(R.drawable.error_outline)
                .into((ImageView) findViewById(R.id.image3));

        Glide.with(this)
                .load(imageUrl4)
                .placeholder(R.drawable.image)
                .error(R.drawable.error_outline)
                .into((ImageView) findViewById(R.id.image4));
    }

    private void setupAnswerBoxes() {
        if (correctAnswer == null) {
            Log.e(TAG, "Cannot setup answer boxes as correctAnswer is null");
            return;
        }
        int answerLength = correctAnswer.length();
        answerBoxes = new TextView[answerLength];

        for (int i = 0; i < answerLength; i++) {
            TextView answerBox = new TextView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    150, 150);

            answerBox.setLayoutParams(layoutParams);
            answerBox.setTextSize(24);
            answerBox.setGravity(Gravity.CENTER);
            answerBox.setBackgroundResource(R.drawable.rounded_box);
            answerBox.setTextColor(getResources().getColor(android.R.color.white));
            answerBox.setTag(null);
            answerBox.setText("");
            ((LinearLayout.LayoutParams) answerBox.getLayoutParams()).setMargins(5, 0, 5, 0);

            answerBoxes[i] = answerBox;
            answerBoxesLayout.addView(answerBox);
        }
    }

    private void onKeyboardButtonClick(View view) {
        Button clickedButton = (Button) view;
        String letter = clickedButton.getText().toString();

        if (currentAnswerIndex < answerBoxes.length) {
            answerBoxes[currentAnswerIndex].setText(letter);

            Drawable background = clickedButton.getBackground();
            int originalBackgroundColor = Color.WHITE;

            if (background instanceof ColorDrawable) {
                originalBackgroundColor = ((ColorDrawable) background).getColor();
            } else if (background instanceof RippleDrawable) {
                RippleDrawable rippleDrawable = (RippleDrawable) background;
                Drawable rippleBackground = rippleDrawable.findDrawableByLayerId(android.R.id.background);
                if (rippleBackground instanceof ColorDrawable) {
                    originalBackgroundColor = ((ColorDrawable) rippleBackground).getColor();
                }
            }

            clickedButton.setTag(originalBackgroundColor);
            answerBoxes[currentAnswerIndex].setTag(clickedButton);
            currentAnswerIndex++;

            clickedButton.setEnabled(false);
            clickedButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }

        if (currentAnswerIndex == answerBoxes.length) {
            validateAnswer();
        }
    }

    private void onDeleteButtonClick() {
        if (currentAnswerIndex > 0) {
            currentAnswerIndex--;

            Button associatedButton = (Button) answerBoxes[currentAnswerIndex].getTag();
            if (associatedButton != null) {
                associatedButton.setEnabled(true);

                int originalBackgroundColor = (int) associatedButton.getTag();
                associatedButton.setBackgroundColor(originalBackgroundColor);
            }

            answerBoxes[currentAnswerIndex].setText("");
            answerBoxes[currentAnswerIndex].setTag(null);

            if (isAnswerIncorrect) {
                resetAnswerBoxColors();
            }
        }
    }

    private void resetAnswerBoxColors() {
        for (TextView answerBox : answerBoxes) {
            answerBox.setBackgroundResource(R.drawable.rounded_box);
        }
        isAnswerIncorrect = false;
    }

    private void shuffleKeyboard() {
        setupKeyboard();
    }

    private void setupKeyboard() {
        if (correctAnswer == null || correctAnswer.isEmpty()) {
            Log.e(TAG, "Cannot setup keyboard as correctAnswer is null or empty");
            return;
        }

        List<String> letters = new ArrayList<>();

        for (char letter : correctAnswer.toCharArray()) {
            letters.add(String.valueOf(letter));
        }

        while (letters.size() < keyboardButtons.length) {
            char randomLetter = (char) ('A' + (int) (Math.random() * 26));
            letters.add(String.valueOf(randomLetter));
        }

        Collections.shuffle(letters);

        for (int i = 0; i < keyboardButtons.length; i++) {
            keyboardButtons[i].setText(letters.get(i));
            keyboardButtons[i].setEnabled(true);
            keyboardButtons[i].setOnClickListener(this::onKeyboardButtonClick);
        }
    }

    private void validateAnswer() {
        StringBuilder enteredAnswer = new StringBuilder();
        for (TextView answerBox : answerBoxes) {
            enteredAnswer.append(answerBox.getText());
        }

        if (enteredAnswer.toString().equalsIgnoreCase(correctAnswer)) {
            // Answer is correct
            int points = 5; // Points for a correct answer
            int currentLevel = 1; // Add new level + 1

            AppDatabase db = AppDatabase.getDatabase(this);
            FourPicsOneWordDao dao = db.fourPicsOneWordDao();

            // Update points for the user on a background thread
            Executors.newSingleThreadExecutor().execute(() -> {
                dao.addPoints(userId, points);
                dao.addLevel(userId, currentLevel);

                // After updating points, start the next activity
                runOnUiThread(() -> {
                    Intent intent = new Intent(this, NextActivity.class);
                    intent.putExtra("CORRECT_ANSWER", correctAnswer);
                    startActivity(intent);
                    finish();
                });
            });
        } else {
            // Answer is incorrect
            isAnswerIncorrect = true;
            for (TextView answerBox : answerBoxes) {
                answerBox.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            }
        }
    }


}


