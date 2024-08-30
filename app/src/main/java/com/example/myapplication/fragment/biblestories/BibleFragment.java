package com.example.myapplication.fragment.biblestories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.database.Converters;
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

public class BibleFragment extends AppCompatActivity {

    ImageView arrowback;
    RecyclerView recyclerView;
    AdapterBible adapterBible;
    List<ModelBible> bibleStories;
    FirebaseFirestore db;
    AppDatabase appDatabase;
    RadioGroup storiesSwitch;
    TextView comingSoonTextView;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_bible);

        // Initialize Firebase Firestore and Room Database
        db = FirebaseFirestore.getInstance();
        appDatabase = AppDatabase.getDatabase(this);

        // Initialize arrowback ImageView
        arrowback = findViewById(R.id.arrowback);

        // Set the click listener for arrowback
        arrowback.setOnClickListener(v -> onBackPressed());

        //navigating


        // Initialize RecyclerView with the correct ID
        recyclerView = findViewById(R.id.recyclep);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize RadioGroup for story selection
        storiesSwitch = findViewById(R.id.stories_switch);

        // Initialize RadioButtons
        RadioButton rbAllStories = findViewById(R.id.rb_all_stories);
        RadioButton rbUpcomingStories = findViewById(R.id.rb_upcoming_stories);

        // Initially set the correct background
        rbAllStories.setBackgroundResource(R.drawable.bg_selected);
        rbUpcomingStories.setBackgroundResource(R.drawable.bg_unselected);

        // Initialize "Coming Soon!" TextView
        comingSoonTextView = findViewById(R.id.comingsoon);

        // Load Bible stories initially
        loadBibleStories();

        // Set up listener for RadioGroup
        storiesSwitch.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_all_stories) {
                // Set backgrounds
                rbAllStories.setBackgroundResource(R.drawable.bg_selected);
                rbUpcomingStories.setBackgroundResource(R.drawable.bg_unselected);
                // Load all stories
                loadAllStories();
            } else if (checkedId == R.id.rb_upcoming_stories) {
                // Set backgrounds
                rbAllStories.setBackgroundResource(R.drawable.bg_unselected);
                rbUpcomingStories.setBackgroundResource(R.drawable.bg_selected);
                // Load upcoming stories
                loadUpcomingStories();
            }
        });
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
                            ModelBible story = new ModelBible(id, title, description, verse, formattedTimestamp, imageUrl);
                            bibleStories.add(story);

                            // Save to local database
                            Executors.newSingleThreadExecutor().execute(() -> {
                                appDatabase.bibleDao().insert(story);  // Insert each story into the local database
                            });
                        }

                        // Update RecyclerView adapter with the list of stories
                        adapterBible = new AdapterBible(BibleFragment.this, bibleStories);  // Pass context and list
                        recyclerView.setAdapter(adapterBible);
                    }
                });
    }

    private void loadFromLocalStorage() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ModelBible> localStories = appDatabase.bibleDao().getAllBibleStories();  // Retrieve all stories from the local database
            bibleStories.addAll(localStories);  // Add local stories to the main list
        });
    }

    private void loadAllStories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ModelBible> allStories = appDatabase.bibleDao().getAllBibleStories();  // Retrieve all stories from the local database

            runOnUiThread(() -> {
                if (allStories.isEmpty()) {
                    comingSoonTextView.setVisibility(View.VISIBLE);  // Show "Coming Soon!" message
                    recyclerView.setVisibility(View.GONE);  // Hide RecyclerView
                } else {
                    comingSoonTextView.setVisibility(View.GONE);  // Hide "Coming Soon!" message
                    recyclerView.setVisibility(View.VISIBLE);  // Show RecyclerView
                    adapterBible = new AdapterBible(BibleFragment.this, allStories);  // Pass context and list
                    recyclerView.setAdapter(adapterBible);
                }
            });
        });
    }

    private void loadUpcomingStories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            List<ModelBible> upcomingStories = appDatabase.bibleDao().getUpcomingBibleStories(currentDate);  // Retrieve upcoming stories from the local database

            runOnUiThread(() -> {
                if (upcomingStories.isEmpty()) {
                    comingSoonTextView.setVisibility(View.VISIBLE);  // Show "Coming Soon!" message
                    recyclerView.setVisibility(View.GONE);  // Hide RecyclerView
                } else {
                    comingSoonTextView.setVisibility(View.GONE);  // Hide "Coming Soon!" message
                    recyclerView.setVisibility(View.VISIBLE);  // Show RecyclerView
                    adapterBible = new AdapterBible(BibleFragment.this, upcomingStories);  // Pass context and list
                    recyclerView.setAdapter(adapterBible);
                }
            });
        });
    }
}
