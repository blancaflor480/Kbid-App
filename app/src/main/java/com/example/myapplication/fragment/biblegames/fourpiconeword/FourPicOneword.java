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
import android.os.Looper;
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

import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
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
    private ImageView arrowback;
    private ProgressBar progressBar;
    private LinearLayout answerBoxesLayout;
    private TextView[] answerBoxes;
    private TextView progressText;
    private int currentAnswerIndex = 0;
    private Button[] keyboardButtons;
    private String correctAnswer;
    private GamesDao gamesDao;
    private int userId;
    private boolean isAnswerIncorrect = false;

    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_fourpiconeword);

        // Initialize database
        AppDatabase db = AppDatabase.getDatabase(this);
        gamesDao = db.gamesDao();


        DataFetcher dataFetcher = new DataFetcher(this, gamesDao);
        dataFetcher.fetchGamesFromFirestore(new DataFetcher.DownloadProgressListener() {
            @Override
            public void onProgressUpdate(int progress, int total) {
                progressBar.setProgress(progress);
                progressText.setText(progress + "/" + total);
            }

            @Override
            public void onDownloadComplete() {
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                fetchGameData();
            }
        });

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

        setupAnswerBoxes();
        adjustKeyboardPosition();
    }

    private void fetchGameData() {
        if (userId == -1) {
            Log.e(TAG, "User ID is invalid.");
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(this);
        LiveData<List<FourPicsOneWord>> userLevels = db.fourPicsOneWordDao().getCurrentLevel(userId);

        // Make sure observe is done on the main thread
        runOnUiThread(() -> {
            userLevels.observe(FourPicOneword.this, levels -> {
                if (levels != null && !levels.isEmpty()) {
                    for (FourPicsOneWord levelData : levels) {
                        int currentLevel = levelData.getCurrentLevel();
                        fetchGamesForLevel(currentLevel);  // Fetch games and images for the level
                    }
                } else {
                    Log.e(TAG, "No levels available for the current user in the database.");
                }
            });
        });
    }



    private void fetchGamesForLevel(int level) {
        AppDatabase db = AppDatabase.getDatabase(this);
        LiveData<List<Games>> liveData = db.gamesDao().getGamesByLevel(level);

        liveData.observe(this, gamesList -> {
            if (gamesList != null && !gamesList.isEmpty()) {
                Games game = gamesList.get(0);
                correctAnswer = game.getAnswer();

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

                if (allImagesLocal(localPaths)) {
                    loadImages(localPaths, localPaths); // Load images from local storage
                } else {
                    loadImages(imageUrls, localPaths); // Download and load images
                }

                // Update the level display for the current level
                TextView levelTextView = findViewById(R.id.level);
                levelTextView.setText(String.valueOf(level));

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


    private boolean allImagesLocal(List<String> localPaths) {
        for (String path : localPaths) {
            if (path == null || path.isEmpty() || !new File(path).exists()) {
                return false; // If any image is not found locally, return false
            }
        }
        return true; // All images are found locally
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
            Glide.with(this)
                    .load(localFile)
                    .placeholder(R.drawable.image)
                    .error(R.drawable.error_outline)
                    .into(imageView);
        } else if (localPath != null && !localPath.isEmpty()) {
            File imagePathFile = new File(localPath);
            Glide.with(this).load(imagePathFile).into(imageView);
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(imageView);
        }
    }

    private void adjustKeyboardPosition() {
        // Get references to views
        View answerBox1 = findViewById(R.id.answerBox1);
        LinearLayout enterAnswer = findViewById(R.id.enteranswer);

        // Wait for layout to be measured
        answerBox1.post(new Runnable() {
            @Override
            public void run() {
                // Get the bottom position of the answer boxes
                int[] answerBoxLocation = new int[2];
                answerBox1.getLocationInWindow(answerBoxLocation);
                int answerBoxBottom = answerBoxLocation[1] + answerBox1.getHeight();

                // Get screen height
                int screenHeight = getResources().getDisplayMetrics().heightPixels;

                // Calculate available space
                int availableSpace = screenHeight - answerBoxBottom;

                // Set up ConstraintLayout parameters
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                        (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) enterAnswer.getLayoutParams();

                // Clear existing constraints
                params.topToTop = -1;
                params.topToBottom = R.id.answerBox1;
                params.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;

                // Add bottom margin to push keyboard up if needed
                int keyboardHeight = enterAnswer.getHeight();
                if (keyboardHeight > availableSpace) {
                    params.bottomMargin = 50; // Add some padding from bottom
                }

                enterAnswer.setLayoutParams(params);
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

        // Clear existing layout
        answerBoxesLayout.removeAllViews();

        // Calculate dimensions
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int maxBoxesPerRow = Math.min(5, (screenWidth - 40) / 160); // 160 = box width (150) + margins (10)
        int numRows = (int) Math.ceil((double) answerLength / maxBoxesPerRow);

        // Create vertical layout
        LinearLayout verticalLayout = new LinearLayout(this);
        verticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setGravity(Gravity.CENTER);

        int currentBoxIndex = 0;

        // Create rows
        for (int row = 0; row < numRows; row++) {
            LinearLayout horizontalRow = new LinearLayout(this);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            // Add spacing between rows
            if (row > 0) {
                rowParams.topMargin = 20;
            }

            horizontalRow.setLayoutParams(rowParams);
            horizontalRow.setOrientation(LinearLayout.HORIZONTAL);
            horizontalRow.setGravity(Gravity.CENTER);

            // Calculate boxes for this row
            int boxesInThisRow = Math.min(maxBoxesPerRow, answerLength - (row * maxBoxesPerRow));

            // Add boxes to row
            for (int i = 0; i < boxesInThisRow; i++) {
                TextView answerBox = createAnswerBox();
                answerBoxes[currentBoxIndex] = answerBox;
                horizontalRow.addView(answerBox);
                currentBoxIndex++;
            }

            verticalLayout.addView(horizontalRow);
        }

        // Update the answer boxes layout
        answerBoxesLayout.removeAllViews();
        answerBoxesLayout.addView(verticalLayout);

        // Trigger keyboard position adjustment
        answerBoxesLayout.post(() -> adjustKeyboardPosition());
    }

    private TextView createAnswerBox() {
        TextView answerBox = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
        layoutParams.setMargins(5, 0, 5, 0);

        answerBox.setLayoutParams(layoutParams);
        answerBox.setTextSize(24);
        answerBox.setGravity(Gravity.CENTER);
        answerBox.setBackgroundResource(R.drawable.rounded_box);
        answerBox.setTextColor(getResources().getColor(android.R.color.white));
        answerBox.setTag(null);
        answerBox.setText("");

        return answerBox;
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

    private void downloadAndSaveImage(String imageUrl, File localFile, Games game, int imageIndex, int totalImages, AtomicInteger downloadedImages) {
        Glide.with(this)
                .asBitmap() // Load the image as a Bitmap
                .load(imageUrl)
                .into(new SimpleTarget<Bitmap>() { // Use SimpleTarget instead of Target
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        // Optional: Show a loading indicator
                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        Log.e(TAG, "Error loading image from URL: " + imageUrl);
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        try (FileOutputStream outputStream = new FileOutputStream(localFile)) {
                            resource.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                            downloadedImages.incrementAndGet();
                            progressBar.setProgress(downloadedImages.get());

                            if (downloadedImages.get() == totalImages) {
                                progressBar.setVisibility(View.INVISIBLE);
                                progressText.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "All images downloaded");
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error saving image: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        // This method is required but can be left empty or used for cleanup
                    }
                });
    }

}
