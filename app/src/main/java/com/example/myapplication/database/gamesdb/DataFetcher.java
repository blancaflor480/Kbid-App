package com.example.myapplication.database.gamesdb;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.myapplication.database.achievement.GameAchievementDao;
import com.example.myapplication.fragment.achievement.GameAchievementModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DataFetcher {
    private static final String TAG = "DataFetcher";
    private final GamesDao gamesDao;
    private final Executor executor;
    private final Context context;
    private final GameAchievementDao gameAchievementDao;

    public DataFetcher(Context context, GamesDao gamesDao, GameAchievementDao gameAchievementDao) {
        this.context = context;
        this.gamesDao = gamesDao;
        this.gameAchievementDao = gameAchievementDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public interface DownloadProgressListener {
        void onProgressUpdate(int progress, int total);
        void onDownloadComplete();
    }

    public void fetchGamesFromFirestore(DownloadProgressListener listener) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference gamesCollection = firestore.collection("games");

        gamesCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                int totalImages = task.getResult().size() * 4; // 4 images per game
                AtomicInteger downloadedImages = new AtomicInteger(0);

                // First, clear existing games data
                executor.execute(() -> {
                    gamesDao.deleteAll();

                    // Now process each document
                    for (DocumentSnapshot document : task.getResult()) {
                        Games game = createGameFromDocument(document);
                        // Insert the game first
                        gamesDao.insert(game);

                        createGameAchievement(game);
                        // Then download images
                        processGameDocument(document, downloadedImages, totalImages, listener);
                    }
                });
            } else {
                Log.e(TAG, "Error fetching games from Firestore", task.getException());
            }
        });
    }

    private void createGameAchievement(Games game) {
        GameAchievementModel achievement = new GameAchievementModel(
                game.getFirestoreId(),        // Game ID reference
                game.getTitle(),              // Game title
                game.getLevel(),              // Game level
                null,                         // Points (null by default)
                "locked"                      // Initial state is locked
        );

        // Save the achievement to the database
        executor.execute(() -> {
            gameAchievementDao.insert(achievement);
        });
    }


    private void processGameDocument(DocumentSnapshot document, AtomicInteger downloadedImages,
                                     int totalImages, DownloadProgressListener listener) {
        String firestoreId = document.getId();
        Games game = createGameFromDocument(document);

        // Download images and update local paths
        downloadAndSaveImage(game.getImageUrl1(), firestoreId, 1, game, downloadedImages, totalImages, listener);
        downloadAndSaveImage(game.getImageUrl2(), firestoreId, 2, game, downloadedImages, totalImages, listener);
        downloadAndSaveImage(game.getImageUrl3(), firestoreId, 3, game, downloadedImages, totalImages, listener);
        downloadAndSaveImage(game.getImageUrl4(), firestoreId, 4, game, downloadedImages, totalImages, listener);
    }

    private Games createGameFromDocument(DocumentSnapshot document) {
        String firestoreId = document.getId();
        String title = document.getString("title");
        String answer = document.getString("answer");
        String level = document.getString("level");
        String imageUrl1 = document.getString("imageUrl1");
        String imageUrl2 = document.getString("imageUrl2");
        String imageUrl3 = document.getString("imageUrl3");
        String imageUrl4 = document.getString("imageUrl4");
        Timestamp timestamp = document.getTimestamp("timestamp");
        String timestampString = timestamp != null ? timestamp.toDate().toString() : null;

        return new Games(firestoreId, title, answer, level, imageUrl1, imageUrl2,
                imageUrl3, imageUrl4, timestampString);
    }

    private void downloadAndSaveImage(String imageUrl, String firestoreId, int imageNumber,
                                      Games game, AtomicInteger downloadedImages, int totalImages,
                                      DownloadProgressListener listener) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            updateProgress(downloadedImages, totalImages, listener);
            return;
        }

        File localFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "game_" + firestoreId + "_image" + imageNumber + ".jpg");

        // Skip if file already exists
        if (localFile.exists()) {
            updateLocalPath(game, localFile.getAbsolutePath(), imageNumber);
            updateProgress(downloadedImages, totalImages, listener);
            return;
        }

        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        saveImageToFile(resource, localFile, game, imageNumber);
                        updateProgress(downloadedImages, totalImages, listener);
                    }
                });
    }

    private void saveImageToFile(Bitmap bitmap, File file, Games game, int imageNumber) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            updateLocalPath(game, file.getAbsolutePath(), imageNumber);
        } catch (IOException e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
        }
    }

    private void updateLocalPath(Games game, String path, int imageNumber) {
        executor.execute(() -> {
            switch (imageNumber) {
                case 1:
                    game.setLocalImagePath1(path);
                    break;
                case 2:
                    game.setLocalImagePath2(path);
                    break;
                case 3:
                    game.setLocalImagePath3(path);
                    break;
                case 4:
                    game.setLocalImagePath4(path);
                    break;
            }
            gamesDao.update(game);
        });
    }

    private void updateProgress(AtomicInteger downloadedImages, int totalImages,
                                DownloadProgressListener listener) {
        int progress = downloadedImages.incrementAndGet();

        // Ensure UI updates are done on the main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                listener.onProgressUpdate(progress, totalImages);
            }
        });

        if (progress == totalImages) {
            // Make sure onDownloadComplete is also called on the main thread
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onDownloadComplete();
                }
            });
        }
    }

}