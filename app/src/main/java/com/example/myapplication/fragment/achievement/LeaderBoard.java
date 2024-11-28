package com.example.myapplication.fragment.achievement;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderBoard extends AppCompatActivity {
    private ImageView arrowback;
    private RecyclerView recyclerView;
    private AdapterLeaderboard adapterLeaderboard;
    private List<leaderboardmodel> leaderboardList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboard);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclep);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        arrowback = findViewById(R.id.arrowback);
        arrowback.setOnClickListener(v -> onBackPressed());
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Load Leaderboard Data
        loadLeaderboardData();
    }

    private void loadLeaderboardData() {
        db.collection("user")
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        leaderboardList.clear();
                        for (QueryDocumentSnapshot userDoc : userTask.getResult()) {
                            String userEmail = userDoc.getString("email");
                            String childName = userDoc.getString("childName") != null ? userDoc.getString("childName") : "Unknown User";
                            String imageUrl = userDoc.getString("avatarName"); // Optional profile image

                            if (userEmail != null) {
                                fetchUserAchievements(userEmail, childName, imageUrl);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserAchievements(String userEmail, String childName, String imageUrl) {
        AtomicInteger totalAchievements = new AtomicInteger();

        // Count story achievements (completed only)
        db.collection("storyAchievement")
                .document(userEmail) // Assuming the userEmail is the document ID
                .collection("storiesdata") // Assuming "storiesdata" holds achievement data
                .get()
                .addOnCompleteListener(storyTask -> {
                    if (storyTask.isSuccessful()) {
                        long storyCompletedCount = 0;
                        for (QueryDocumentSnapshot storyDoc : storyTask.getResult()) {
                            // Count completed story achievements
                            if ("completed".equals(storyDoc.getString("isCompleted"))) {
                                storyCompletedCount++;
                            }
                        }

                        // Count game achievements (completed only)
                        long finalStoryCompletedCount = storyCompletedCount;
                        db.collection("gameAchievements") // Adjusted path for game achievements
                                .document(userEmail) // Using userEmail as the document ID for the user
                                .collection("gamesdata") // Assuming the achievements are stored here
                                .get()
                                .addOnCompleteListener(gameTask -> {
                                    if (gameTask.isSuccessful()) {
                                        long gameCompletedCount = 0;
                                        for (QueryDocumentSnapshot gameDoc : gameTask.getResult()) {
                                            // Count completed game achievements
                                            if ("completed".equals(gameDoc.getString("isCompleted"))) {
                                                gameCompletedCount++;
                                            }
                                        }

                                        // Aggregate total completed achievements (story + game)
                                        totalAchievements.set((int) (finalStoryCompletedCount + gameCompletedCount));

                                        // Add to leaderboard list
                                        leaderboardList.add(new leaderboardmodel(
                                                childName,
                                                imageUrl,
                                                totalAchievements.get()
                                        ));

                                        // Update RecyclerView
                                        updateLeaderboard();
                                    } else {
                                        Log.e("Leaderboard", "Error fetching game achievements", gameTask.getException());
                                    }
                                });
                    } else {
                        Log.e("Leaderboard", "Error fetching story achievements", storyTask.getException());
                    }
                });
    }


    private void updateLeaderboard() {
        leaderboardList.sort((o1, o2) -> Integer.compare(o2.getTotalPoints(), o1.getTotalPoints()));
        if (adapterLeaderboard == null) {
            adapterLeaderboard = new AdapterLeaderboard(this, leaderboardList);
            recyclerView.setAdapter(adapterLeaderboard);
        } else {
            adapterLeaderboard.notifyDataSetChanged();
        }
    }
}
