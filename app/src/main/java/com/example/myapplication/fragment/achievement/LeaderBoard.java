package com.example.myapplication.fragment.achievement;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.ui.record.RecordModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderBoard extends AppCompatActivity {
    private ImageView arrowback;
    private RecyclerView recyclerView;
    private AdapterLeaderboard adapterLeaderboard;
    private List<leaderboardmodel> leaderboardList = new ArrayList<>();
    private FirebaseFirestore db;
    private boolean isHighestFirst = true; // Add this field
    private ImageButton sortButton;
    private TextView textSort;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private List<leaderboardmodel> topThreeList = new ArrayList<>(); // Add this for storing top 3
    private List<leaderboardmodel> recyclerViewList = new ArrayList<>(); // Add this for RecyclerView


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

        sortButton = findViewById(R.id.sort);
        textSort = findViewById(R.id.textsort);

        sortButton.setOnClickListener(v -> {
            isHighestFirst = !isHighestFirst;
            textSort.setText(isHighestFirst ? "Highest" : "Lowest");
            updateLeaderboard();
        });
        // Load Leaderboard Data
        loadLeaderboardData();
    }

    private void loadLeaderboardData() {
        executorService.submit(() -> {
            try {
                leaderboardList.clear();
                List<LeaderBoard> aggregatedRecords = new ArrayList<>();

                QuerySnapshot usersSnapshot = Tasks.await(db.collection("user").get());

                for (DocumentSnapshot userDoc : usersSnapshot.getDocuments()) {
                    String userEmail = userDoc.getString("email");
                    if (userEmail == null) continue;

                    String childName = userDoc.getString("childName") != null ? userDoc.getString("childName") : "Unknown User";
                    String imageUrl = userDoc.getString("avatarName");
                    String borderImage = userDoc.getString("borderName");

                    // Count story achievements
                    long storyCompletedCount = Tasks.await(db.collection("storyachievements")
                            .document(userEmail)
                            .collection("achievements")
                            .whereEqualTo("isCompleted", "completed")
                            .count()
                            .get(AggregateSource.SERVER)).getCount();

                    // Count game achievements
                    long gameCompletedCount = Tasks.await(db.collection("gameachievements")
                            .document(userEmail)
                            .collection("gamedata")
                            .whereEqualTo("isCompleted", "completed")
                            .count()
                            .get(AggregateSource.SERVER)).getCount();

                    // Count kids reflections
                    long reflectionCount = Tasks.await(db.collection("kidsReflection")
                            .whereEqualTo("email", userEmail)
                            .count()
                            .get(AggregateSource.SERVER)).getCount();

                    // Calculate total achievements
                    long totalAchievements = storyCompletedCount + gameCompletedCount + reflectionCount;

                    // Add to leaderboard list
                    leaderboardList.add(new leaderboardmodel(
                            childName,
                            imageUrl,
                            borderImage,
                            (int) totalAchievements
                    ));
                }
                // After loading all data, store top 3
                leaderboardList.sort((o1, o2) -> Integer.compare(o2.getTotalPoints(), o1.getTotalPoints()));

                topThreeList.clear();
                for (int i = 0; i < Math.min(3, leaderboardList.size()); i++) {
                    topThreeList.add(leaderboardList.get(i));
                }
                recyclerViewList.addAll(leaderboardList);
                // Update UI on main thread
                runOnUiThread(() -> {
                    updateLeaderboard();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading leaderboard: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateLeaderboard() {
        // Sort recyclerViewList based on current sorting order
        recyclerViewList.clear();
        recyclerViewList.addAll(leaderboardList);

        recyclerViewList.sort((o1, o2) -> Integer.compare(o2.getTotalPoints(), o1.getTotalPoints()));


        int currentRank = 1;
        int previousPoints = -1;
        int sameRankCount = 0;

        for (int i = 0; i < recyclerViewList.size(); i++) {
            leaderboardmodel current = recyclerViewList.get(i);

            if (i > 0 && current.getTotalPoints() == previousPoints) {
                // Same points as previous user, assign same rank
                current.setRank(String.valueOf(currentRank));
                sameRankCount++;
            } else {
                // Different points, assign new rank
                currentRank = i + 1;
                current.setRank(String.valueOf(currentRank));
                sameRankCount = 0;
            }

            previousPoints = current.getTotalPoints();
        }
        if (!isHighestFirst) {
            recyclerViewList.sort((o1, o2) -> {
                // First sort by points (lowest first)
                int pointsCompare = Integer.compare(o1.getTotalPoints(), o2.getTotalPoints());
                if (pointsCompare != 0) {
                    return pointsCompare;
                }
                // If points are equal, sort by rank (lowest first)
                return Integer.compare(
                        Integer.parseInt(o1.getRank()),
                        Integer.parseInt(o2.getRank())
                );
            });
        }


        // Update UI with fixed top 3
        runOnUiThread(() -> {
            TextView firstRankName = findViewById(R.id.firstrank_name);
            TextView secondRankName = findViewById(R.id.secondrank_name);
            TextView thirdRankName = findViewById(R.id.thirdrank_name);

            ImageView rank1Image = findViewById(R.id.rank1);
            ImageView rank2Image = findViewById(R.id.rank2);
            ImageView rank3Image = findViewById(R.id.rank3);

            if (topThreeList.size() > 0) {
                firstRankName.setText(topThreeList.get(0).getUserName());
                int resourceId = getResources().getIdentifier(topThreeList.get(0).getImageUrl(), "drawable", getPackageName());
                rank1Image.setImageResource(resourceId != 0 ? resourceId : R.drawable.lion);
            }

            if (topThreeList.size() > 1) {
                secondRankName.setText(topThreeList.get(1).getUserName());
                int resourceId = getResources().getIdentifier(topThreeList.get(1).getImageUrl(), "drawable", getPackageName());
                rank2Image.setImageResource(resourceId != 0 ? resourceId : R.drawable.lion);
            }

            if (topThreeList.size() > 2) {
                thirdRankName.setText(topThreeList.get(2).getUserName());
                int resourceId = getResources().getIdentifier(topThreeList.get(2).getImageUrl(), "drawable", getPackageName());
                rank3Image.setImageResource(resourceId != 0 ? resourceId : R.drawable.lion);
            }
        });

        if (adapterLeaderboard == null) {
            adapterLeaderboard = new AdapterLeaderboard(this, recyclerViewList);
            recyclerView.setAdapter(adapterLeaderboard);
        } else {
            adapterLeaderboard.notifyDataSetChanged();
        }
    }

}
