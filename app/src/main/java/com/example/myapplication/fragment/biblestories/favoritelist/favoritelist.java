package com.example.myapplication.fragment.biblestories.favoritelist;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.database.Converters;
import com.example.myapplication.fragment.biblestories.favoritelist.Adapterfavoritelist;
import com.example.myapplication.fragment.biblestories.favoritelist.Modelfavoritelist;
import com.example.myapplication.fragment.biblestories.favoritelist.favoritelist;
import com.google.firebase.Timestamp;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;


public class favoritelist extends AppCompatActivity{

    ImageView arrowback;
    RecyclerView recyclerView;
    Adapterfavoritelist Adapterfavoritelist;
    List<Modelfavoritelist> bibleStories;
    FirebaseFirestore db;
    AppDatabase appDatabase;
    TextView Empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_list);

        // Initialize Firebase Firestore and Room Database
        db = FirebaseFirestore.getInstance();
        appDatabase = AppDatabase.getDatabase(this);

        // Initialize arrowback ImageView
        arrowback = findViewById(R.id.arrowback);
        //navigating

        // Set the click listener for arrowback
        arrowback.setOnClickListener(v -> onBackPressed());



        // Initialize RecyclerView with the correct ID
        recyclerView = findViewById(R.id.recyclep);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Initialize "Coming Soon!" TextView
        Empty = findViewById(R.id.empty);

        // Load Bible stories initially
        loadBibleStories();

    }
    private void loadBibleStories() {
        bibleStories = new ArrayList<>();

        // Load data from local storage first
        loadFromLocalStorage();

        // Check if device is online
        if (isOnline()) {
            // Fetch data from Firestore
            fetchFromFirestore();
        } else {
            // No internet connection, show local data
            Adapterfavoritelist = new Adapterfavoritelist(this, bibleStories);  // Pass context and list
            recyclerView.setAdapter(Adapterfavoritelist);
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
                        bibleStories.clear(); // Clear existing stories to avoid duplicates
                        for (DocumentSnapshot document : task.getResult()) {
                            String id = document.getId();  // Fetch Firestore document ID
                            String title = document.getString("title");  // Fetch title
                            String description = document.getString("description");  // Fetch description
                            String verse = document.getString("verse");  // Fetch verse
                            Timestamp timestamp = document.getTimestamp("timestamp");  // Fetch timestamp as Timestamp object
                            String imageUrl = document.getString("imageUrl");  // Fetch image URL

                            // Convert Timestamp to formatted String
                            String formattedTimestamp = null;
                            if (timestamp != null) {
                                formattedTimestamp = Converters.fromTimestampToString(timestamp.toDate().getTime());
                            }

                            // Create ModelBible object
                            Modelfavoritelist story = new Modelfavoritelist(id, title, description, verse, formattedTimestamp, imageUrl);
                            bibleStories.add(story);

                            // Save to local database
                            Executors.newSingleThreadExecutor().execute(() -> {
                                appDatabase.FavoriteDao().insert(story);  // Insert each story into the local database
                            });
                        }

                        // Update RecyclerView adapter with the list of stories
                        Adapterfavoritelist = new Adapterfavoritelist(favoritelist.this, bibleStories);  // Pass context and list
                        recyclerView.setAdapter(Adapterfavoritelist);
                    }
                });
    }
    private void loadFromLocalStorage() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Modelfavoritelist> localStories = appDatabase.FavoriteDao().getAllBibleStories();  // Retrieve all stories from the local database

            runOnUiThread(() -> {
                if (localStories.isEmpty()) {
                    // If the list is empty, show the "Empty" TextView and hide RecyclerView
                    Empty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    // If data exists, hide the "Empty" TextView and show RecyclerView
                    Empty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    bibleStories.addAll(localStories);  // Add local stories to the main list

                    // Update RecyclerView with the stories
                    Adapterfavoritelist = new Adapterfavoritelist(favoritelist.this, bibleStories);
                    recyclerView.setAdapter(Adapterfavoritelist);
                }
            });
        });
    }

    private void loadAllStories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Modelfavoritelist> allStories = appDatabase.FavoriteDao().getAllBibleStories();  // Retrieve all stories from the local database

            runOnUiThread(() -> {
                if (allStories.isEmpty()) {
                    Empty.setVisibility(View.VISIBLE);  // Show "Coming Soon!" message
                    recyclerView.setVisibility(View.GONE);  // Hide RecyclerView
                } else {
                    Empty.setVisibility(View.GONE);  // Hide "Coming Soon!" message
                    recyclerView.setVisibility(View.VISIBLE);  // Show RecyclerView
                    Adapterfavoritelist = new Adapterfavoritelist(favoritelist.this, allStories);  // Pass context and list
                    recyclerView.setAdapter(Adapterfavoritelist);
                }
            });
        });
    }

}
