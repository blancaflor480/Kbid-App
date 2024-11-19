package com.example.myapplication.fragment.biblegames.fourpiconeword;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWord;
import com.example.myapplication.database.fourpicsdb.FourPicsOneWordDao;
import com.example.myapplication.database.gamesdb.DataFetcher;
import com.example.myapplication.database.gamesdb.Games;
import com.example.myapplication.database.gamesdb.GamesDao;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FourPicOneword extends AppCompatActivity {
    private static final String TAG = "FourPicOneword";
    private ImageView arrowback, ImageView;
    private ProgressBar progressBar;
    private LinearLayout answerBoxesLayout;
    private TextView[] answerBoxes;
    private TextView progressText;
    private int currentAnswerIndex = 0;
    private boolean isAnswerIncorrect = false;
    private Button[] keyboardButtons;
    private String correctAnswer;
    private GamesDao gamesDao;
    private int userId;
    private long startTime;

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
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

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

        // Automatically start downloading all resources when activity starts
        startTime = System.currentTimeMillis(); // Initialize startTime
        fetchAndDownloadAllResources();
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
        if (userId == -1) {
            Log.e(TAG, "User ID is invalid.");
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(this);
        LiveData<List<FourPicsOneWord>> userLevels = db.fourPicsOneWordDao().getCurrentLevel(userId);

        userLevels.observe(this, levels -> {
            if (levels != null && !levels.isEmpty()) {
                for (FourPicsOneWord levelData : levels) {
                    int currentLevel = levelData.getCurrentLevel();
                    fetchGamesForLevel(currentLevel);
                }
            } else {
                Log.e(TAG, "No levels available for the current user in the database.");
            }
        });
    }

    private void fetchGamesForLevel(int level) {
        AppDatabase db = AppDatabase.getDatabase(this);
        LiveData<List<Games>> liveData = db.gamesDao().getGamesByLevel(level);

        liveData.observe(this, gamesList -> {
            if (gamesList != null && !gamesList.isEmpty()) {
                Games game = gamesList.get(0);
                correctAnswer = game.getAnswer();

                // Log fetched answer for debugging
                Log.d(TAG, "Fetched correctAnswer for level " + level + ": " + correctAnswer);

                if (correctAnswer == null) {
                    Log.e(TAG, "Correct answer is null for level " + level);
                    correctAnswer = "";
                }

                List<String> imageUrls = new ArrayList<>();
                List<String> localPaths = new ArrayList<>();

                imageUrls.add(game.getImageUrl1());
                imageUrls.add(game.getImageUrl2());
                imageUrls.add(game.getImageUrl3());
                imageUrls.add(game.getImageUrl4());

                localPaths.add(game.getLocalImagePath1());
                localPaths.add(game.getLocalImagePath2());
                localPaths.add(game.getLocalImagePath3());
                localPaths.add(game.getLocalImagePath4());

                // Correct call to loadImages method
                loadImages(imageUrls, localPaths);

                // Update the level display for the current level
                TextView levelTextView = findViewById(R.id.level);
                levelTextView.setText(String.valueOf(level)); // Corrected to handle int as String

                // Clear existing answer boxes and keyboard buttons before setting up new ones
                answerBoxesLayout.removeAllViews();
                currentAnswerIndex = 0;

                setupAnswerBoxes();
                setupKeyboard();
            } else {
                Log.e(TAG, "No games available for level " + level + " in the database.");
            }
        });
    }

    private void fetchAndDownloadAllResources() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference gamesCollection = firestore.collection("games");

        gamesCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                int totalImages = documents.size() * 4; // 4 images per game
                AtomicInteger downloadedImages = new AtomicInteger(0);

                // Show progress UI
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
                progressBar.setMax(totalImages);

                for (DocumentSnapshot document : documents) {
                    String firestoreId = document.getId();
                    String title = document.getString("title");
                    String answer = document.getString("answer");
                    String level = document.getString("level");
                    String[] imageUrls = {
                            document.getString("imageUrl1"),
                            document.getString("imageUrl2"),
                            document.getString("imageUrl3"),
                            document.getString("imageUrl4")
                    };

                    Games game = new Games(firestoreId, title, answer, level, imageUrls[0], imageUrls[1], imageUrls[2], imageUrls[3], null);

                    // Download each image
                    for (int i = 0; i < imageUrls.length; i++) {
                        if (imageUrls[i] != null && !imageUrls[i].isEmpty()) {
                            File localFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image_" + firestoreId + "_" + i + ".jpg");
                            downloadAndSaveImage(imageUrls[i], localFile, game, i, totalImages, downloadedImages);
                        }
                    }
                }
            } else {
                Log.e(TAG, "Error fetching games from Firestore", task.getException());
            }
        });
    }
    private void loadImages(List<String> imageUrls, List<String> localPaths) {
        for (int i = 0; i < imageUrls.size(); i++) {
            String imageUrl = imageUrls.get(i);
            String localPath = localPaths.get(i);
            ImageView imageView = findViewById(getResources().getIdentifier("image" + (i + 1), "id", getPackageName()));
            loadImageWithCaching(imageUrl, imageView, localPath, "image" + i + ".jpg");
        }
    }


    private void loadImageWithCaching(String imageUrl, ImageView imageView, String localPath, String fileName) {
        File localFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

        if (localFile.exists()) {
            // Load the image from local storage if it exists
            Glide.with(this)
                    .load(localFile)
                    .placeholder(R.drawable.image) // Optional placeholder image
                    .error(R.drawable.error_outline) // Optional error image
                    .into(imageView);
        } else if (localPath != null && !localPath.isEmpty()) {
            // Load the image from the saved local path in the database
            File imagePathFile = new File(localPath);
            if (imagePathFile.exists()) {
                Glide.with(this)
                        .load(imagePathFile)
                        .placeholder(R.drawable.image)
                        .error(R.drawable.error_outline)
                        .into(imageView);
            } else {
                // If the file path doesn't exist locally anymore, download again
                downloadAndSaveImage(imageUrl, localFile);  // Uses the new overloaded method
            }
        } else {
            // Download and save the image locally if not already cached
            downloadAndSaveImage(imageUrl, localFile);  // Uses the new overloaded method
        }
    }

    private void downloadAndSaveImage(String imageUrl, File localFile) {
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .placeholder(R.drawable.image) // Optional placeholder image
                .error(R.drawable.error_outline) // Optional error image
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        try {
                            // Save the bitmap to local storage
                            saveImageToLocalStorage(resource, localFile);
                        } catch (Exception e) {
                            Log.e(TAG, "Error saving image to local storage: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Log.e(TAG, "Image download failed for URL: " + imageUrl);
                    }
                });
    }

    private void downloadAndSaveImage(String imageUrl, File localFile, Games game, int index, int totalImages, AtomicInteger downloadedImages) {
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .placeholder(R.drawable.image) // Optional placeholder image
                .error(R.drawable.error_outline) // Optional error image
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        try {
                            // Save the bitmap to local storage
                            saveImageToLocalStorage(resource, localFile);
                            // Save the local path to the database using the Firestore ID as a String
                            saveImagePathToDatabase(game.getFirestoreId(), index, localFile.getAbsolutePath());

                            // Update progress
                            int currentDownloads = downloadedImages.incrementAndGet();
                            runOnUiThread(() -> {
                                progressBar.setProgress(currentDownloads);
                                int percentage = (int) ((currentDownloads / (float) totalImages) * 100);
                                progressText.setText("Downloading resources... " + percentage + "%");

                                // Calculate estimated time remaining
                                long elapsedMillis = System.currentTimeMillis() - startTime;
                                float avgTimePerImage = elapsedMillis / (float) currentDownloads;
                                float estimatedTimeRemaining = (totalImages - currentDownloads) * avgTimePerImage / 1000; // seconds
                                progressText.append(" - " + Math.round(estimatedTimeRemaining) + " sec remaining");

                                if (currentDownloads == totalImages) {
                                    // Hide progress UI when done
                                    progressBar.setVisibility(View.GONE);
                                    progressText.setText("Download complete!");
                                }
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error saving image to local storage: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Log.e(TAG, "Image download failed for URL: " + imageUrl);
                    }
                });
    }

    private void saveImageToLocalStorage(Bitmap bitmap, File file) throws IOException {
        File directory = file.getParentFile();
        if (!directory.exists()) {
            boolean dirCreated = directory.mkdirs();
            if (!dirCreated) {
                throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.d(TAG, "Image saved successfully: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new IOException("Error saving image to file: " + file.getAbsolutePath(), e);
        }
    }
    private void saveImagePathToDatabase(String gameId, int imageIndex, String localPath) {
        Executors.newSingleThreadExecutor().execute(() -> {
            GamesDao dao = AppDatabase.getDatabase(this).gamesDao();
            try {
                switch (imageIndex) {
                    case 0:
                        dao.updateLocalImagePath1(gameId, localPath);
                        break;
                    case 1:
                        dao.updateLocalImagePath2(gameId, localPath);
                        break;
                    case 2:
                        dao.updateLocalImagePath3(gameId, localPath);
                        break;
                    case 3:
                        dao.updateLocalImagePath4(gameId, localPath);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected image index: " + imageIndex);
                }
                Log.d(TAG, "Database updated with image path: " + localPath);
            } catch (Exception e) {
                Log.e(TAG, "Error updating database with image path: " + e.getMessage());
            }
        });
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


