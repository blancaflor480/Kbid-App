package com.example.myapplication.fragment.biblestories.favoritelist;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.Converters;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class favoritelist extends AppCompatActivity {

    ImageView arrowback;
    RecyclerView recyclerView;
    Adapterfavoritelist adapterfavoritelist;
    List<Modelfavoritelist> bibleStories;
    FirebaseFirestore db;
    AppDatabase appDatabase;
    TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_list);

        // Initialize Firebase Firestore and Room Database
        db = FirebaseFirestore.getInstance();
        appDatabase = AppDatabase.getDatabase(this);

        // Initialize views
        arrowback = findViewById(R.id.arrowback);
        recyclerView = findViewById(R.id.recyclep);
        emptyTextView = findViewById(R.id.empty);

        // Set RecyclerView layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set the back button action
        arrowback.setOnClickListener(v -> onBackPressed());

        // Load Bible stories from local storage or Firestore
        loadBibleStories();
    }

    private void loadBibleStories() {
        bibleStories = new ArrayList<>();

        // Load from local storage first
        loadFromLocalStorage();

        // If online, fetch from Firestore and check for updates
        if (isOnline()) {
            fetchFromFirestore();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void fetchFromFirestore() {
        db.collection("favorite")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Modelfavoritelist> fetchedStories = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            // Fetch data from Firestore
                            String storyId = document.getId();
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String verse = document.getString("verse");
                            Timestamp timestamp = document.getTimestamp("timestamp");
                            String imageUrl = document.getString("imageUrl");

                            // Convert timestamp to string
                            String formattedTimestamp = null;
                            if (timestamp != null) {
                                formattedTimestamp = Converters.fromTimestampToString(timestamp.toDate().getTime());
                            }

                            // Create the story object
                            Modelfavoritelist story = new Modelfavoritelist(storyId, title, description, verse, formattedTimestamp, imageUrl);
                            fetchedStories.add(story);

                            // Check if story exists locally
                            Executors.newSingleThreadExecutor().execute(() -> {
                                Modelfavoritelist localStory = appDatabase.FavoriteDao().getFavoriteByStoryIdAndUserId(storyId);
                                if (localStory == null || !localStory.equals(story)) {
                                    // Update local SQLite if story doesn't exist or is different
                                    appDatabase.FavoriteDao().insert(story);
                                }
                            });
                        }

                        // Update UI with fetched data from Firestore
                        runOnUiThread(() -> {
                            // If fetched stories have updates, update the local list
                            if (!fetchedStories.isEmpty()) {
                                bibleStories.clear();
                                bibleStories.addAll(fetchedStories);
                                adapterfavoritelist.notifyDataSetChanged();
                            }
                        });
                    }
                });
    }

    private void loadFromLocalStorage() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Modelfavoritelist> localStoriesWithDetails = appDatabase.FavoriteDao().getAllFavoriteStoriesWithDetails();

            runOnUiThread(() -> {
                if (localStoriesWithDetails.isEmpty()) {
                    // Show empty view if no data is found
                    emptyTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    // Hide empty view and display RecyclerView
                    emptyTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    bibleStories.clear();
                    bibleStories.addAll(localStoriesWithDetails);

                    adapterfavoritelist = new Adapterfavoritelist(favoritelist.this, bibleStories);
                    recyclerView.setAdapter(adapterfavoritelist);
                }
            });
        });
    }

}
