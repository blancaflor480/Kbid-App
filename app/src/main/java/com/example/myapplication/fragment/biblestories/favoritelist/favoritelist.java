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

        // If online, fetch from Firestore
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
                        bibleStories.clear();  // Clear to avoid duplicates

                        for (DocumentSnapshot document : task.getResult()) {
                            // Fetch data from Firestore
                            int id = Integer.parseInt(document.getId());
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
                            Modelfavoritelist story = new Modelfavoritelist(id, title, description, verse, formattedTimestamp, imageUrl);
                            bibleStories.add(story);

                            // Save story locally
                            Executors.newSingleThreadExecutor().execute(() -> appDatabase.FavoriteDao().insert(story));
                        }

                        // Update RecyclerView with fetched data
                        runOnUiThread(() -> {
                            adapterfavoritelist = new Adapterfavoritelist(favoritelist.this, bibleStories);
                            recyclerView.setAdapter(adapterfavoritelist);
                        });
                    }
                });
    }

    private void loadFromLocalStorage() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Modelfavoritelist> localStories = appDatabase.FavoriteDao().getAllBibleStories();

            runOnUiThread(() -> {
                if (localStories.isEmpty()) {
                    // Show empty view if no data is found
                    emptyTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    // Hide empty view and display RecyclerView
                    emptyTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    bibleStories.addAll(localStories);

                    // Update adapter with local data
                    adapterfavoritelist = new Adapterfavoritelist(favoritelist.this, bibleStories);
                    recyclerView.setAdapter(adapterfavoritelist);
                }
            });
        });
    }
}
