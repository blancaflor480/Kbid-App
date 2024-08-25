package com.example.myapplication.fragment.biblestories;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class BibleFragment extends AppCompatActivity {

    ImageView arrowback;
    RecyclerView recyclerView;
    AdapterBible adapterBible;
    List<ModelBible> bibleStories;
    FirebaseFirestore db;
    AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_bible);



        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        // Initialize Room Database
        appDatabase = AppDatabase.getDatabase(this);

        // Initialize arrowback ImageView
        arrowback = findViewById(R.id.arrowback);

        // Set the click listener for arrowback
        arrowback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();  // This will handle back navigation
            }
        });

        // Initialize RecyclerView with the correct ID
        recyclerView = findViewById(R.id.recyclep);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load Bible stories
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
            adapterBible = new AdapterBible(this, bibleStories);  // Pass context and list
            recyclerView.setAdapter(adapterBible);
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void fetchFromFirestore() {
        db.collection("stories")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        bibleStories.clear(); // Clear existing stories to avoid duplicates
                        for (DocumentSnapshot document : task.getResult()) {
                            String id = document.getId(); // Fetch Firestore document ID
                            String title = document.getString("title"); // Fetch title
                            String description = document.getString("description"); // Fetch description
                            String verse = document.getString("verse"); // Fetch verse
                            String imageUrl = document.getString("imageUrl"); // Fetch imageUrl

                            // Create ModelBible object
                            ModelBible story = new ModelBible(id, title, description, verse, imageUrl);
                            bibleStories.add(story);

                            // Save to local database
                            Executors.newSingleThreadExecutor().execute(() -> {
                                appDatabase.bibleDao().insert(story); // Insert the ModelBible object directly
                            });
                        }
                        runOnUiThread(() -> {
                            adapterBible.notifyDataSetChanged();
                        });
                    } else {
                        Log.w("BibleActivity", "Error getting documents.", task.getException());
                    }
                });
    }

    private void loadFromLocalStorage() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ModelBible> stories = appDatabase.bibleDao().getAllBibleStories();
            runOnUiThread(() -> {
                bibleStories.addAll(stories);
                adapterBible = new AdapterBible(this, bibleStories);  // Pass context and list
                recyclerView.setAdapter(adapterBible);
            });
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("BibleActivity", "Navigating back to FragmentHome");
    }
}
