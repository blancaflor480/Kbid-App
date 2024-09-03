package com.example.myapplication.database.gamesdb;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.example.myapplication.database.AppDatabase;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DataFetcher {
    private final GamesDao gamesDao;
    private final Executor executor;  // Executor for background operations

    public DataFetcher(GamesDao gamesDao) {
        this.gamesDao = gamesDao;
        this.executor = Executors.newSingleThreadExecutor(); // Initializes the Executor
    }

    public void fetchGamesFromFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference gamesCollection = firestore.collection("games");

        gamesCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot document : task.getResult()) {
                    String firestoreId = document.getId();
                    String title = document.getString("title");
                    String answer = document.getString("answer");
                    String level = document.getString("level");
                    String imageUrl1 = document.getString("imageUrl1");
                    String imageUrl2 = document.getString("imageUrl2");
                    String imageUrl3 = document.getString("imageUrl3");
                    String imageUrl4 = document.getString("imageUrl4");

                    // Fetch timestamp as a Timestamp object
                    Timestamp timestamp = document.getTimestamp("timestamp");

                    // Convert Timestamp to String or Date as needed
                    String timestampString = timestamp != null ? timestamp.toDate().toString() : null; // Converts to String

                    // Create a Games object
                    Games game = new Games(firestoreId, title, answer, level, imageUrl1, imageUrl2, imageUrl3, imageUrl4,  timestampString);

                    // Use Executor to perform the database operation in the background
                    executor.execute(() -> gamesDao.insert(game));
                }
            } else {
                Log.e("DataFetcher", "Error fetching games from Firestore", task.getException());
            }
        });
    }
}
