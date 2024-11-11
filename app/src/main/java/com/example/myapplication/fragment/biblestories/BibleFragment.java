package com.example.myapplication.fragment.biblestories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.database.Converters;
import com.example.myapplication.fragment.achievement.StoryAchievementModel;
import com.example.myapplication.fragment.biblestories.favoritelist.favoritelist;
import com.google.firebase.Timestamp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import android.media.MediaPlayer;

public class BibleFragment extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    ImageView arrowback;
    RecyclerView recyclerView;
    AdapterBible adapterBible;
    List<ModelBible> bibleStories;
    FirebaseFirestore db;
    AppDatabase appDatabase;
    RadioGroup storiesSwitch;
    TextView comingSoonTextView;
    // Existing member variables
    MediaPlayer clickSound;
    // New UI components
    LottieAnimationView noConnectionAnimation, playlist;
    TextView noConnectionMessage;
    ImageButton restartButton;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_bible);

        // Initialize Firebase Firestore and Room Database
        db = FirebaseFirestore.getInstance();
        appDatabase = AppDatabase.getDatabase(this);

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);  // Make sure the ID matches your layout

        // Initialize arrowback ImageView
        arrowback = findViewById(R.id.arrowback);


        // Set the click listener for arrowback
        arrowback.setOnClickListener(v -> onBackPressed());

        playlist = findViewById(R.id.playlist);
        playlist.setOnClickListener(v -> {
            startActivity(new Intent(BibleFragment.this, favoritelist.class));
        });

        // Initialize MediaPlayer for sound effect
        clickSound = MediaPlayer.create(this, R.raw.clickpop);
        arrowback.setOnClickListener(v -> {
            playClickSound(); // Play sound effect
            onBackPressed(); // Handle back press
        });
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

        // Initialize "No Connection" UI components
        noConnectionAnimation = findViewById(R.id.noconnection);
        noConnectionMessage = findViewById(R.id.no_connection_message);
        restartButton = findViewById(R.id.restart);

        // Set onClickListener for restart button
        restartButton.setOnClickListener(v -> refreshBibleStories());

        // Load Bible stories initially without triggering refresh
        loadBibleStories();


        // Setup the SwipeRefreshLayout listener for refreshing Bible stories
        swipeRefreshLayout.setOnRefreshListener(this::refreshBibleStories);

        // Set up listener for RadioGroup
        storiesSwitch.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_all_stories) {
                rbAllStories.setBackgroundResource(R.drawable.bg_selected);
                rbUpcomingStories.setBackgroundResource(R.drawable.bg_unselected);
                loadAllStories();
            } else if (checkedId == R.id.rb_upcoming_stories) {
                rbAllStories.setBackgroundResource(R.drawable.bg_unselected);
                rbUpcomingStories.setBackgroundResource(R.drawable.bg_selected);
                loadUpcomingStories();
            }
        });
    }

    // Function to play the click sound
    private void playClickSound() {
        if (clickSound != null) {
            clickSound.start(); // Play the sound
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clickSound != null) {
            clickSound.release(); // Release the MediaPlayer resources
            clickSound = null;
        }
    }
    // Function to refresh Bible stories manually only on swipe or restart button click
    private void refreshBibleStories() {
        swipeRefreshLayout.setRefreshing(true);

        if (isOnline()) {
            // Hide "No Connection" UI components
            noConnectionAnimation.setVisibility(View.GONE);
            noConnectionMessage.setVisibility(View.GONE);
            restartButton.setVisibility(View.GONE);

            // Show RecyclerView and other content
            recyclerView.setVisibility(View.VISIBLE);
            storiesSwitch.setVisibility(View.VISIBLE);
            playlist.setVisibility(View.VISIBLE);
            arrowback.setVisibility(View.VISIBLE);

            fetchFromFirestore();
        } else {
            // Check if there's any data in local storage
            Executors.newSingleThreadExecutor().execute(() -> {
                List<ModelBible> localStories = appDatabase.bibleDao().getAllBibleStories();
                if (!localStories.isEmpty()) {
                    runOnUiThread(() -> {
                        adapterBible = new AdapterBible(BibleFragment.this, localStories);
                        recyclerView.setAdapter(adapterBible);

                        recyclerView.setVisibility(View.VISIBLE);
                        storiesSwitch.setVisibility(View.VISIBLE);
                        playlist.setVisibility(View.VISIBLE);
                        arrowback.setVisibility(View.VISIBLE);

                        // Hide "No Connection" UI because we have local data
                        noConnectionAnimation.setVisibility(View.GONE);
                        noConnectionMessage.setVisibility(View.GONE);
                        restartButton.setVisibility(View.GONE);
                    });
                } else {
                    // Show "No Connection" UI if no local data is available
                    runOnUiThread(() -> {
                        noConnectionAnimation.setVisibility(View.VISIBLE);
                        noConnectionMessage.setVisibility(View.VISIBLE);
                        restartButton.setVisibility(View.VISIBLE);

                        recyclerView.setVisibility(View.GONE);
                        storiesSwitch.setVisibility(View.GONE);
                        playlist.setVisibility(View.GONE);
                        arrowback.setVisibility(View.GONE);
                        comingSoonTextView.setVisibility(View.GONE);
                    });
                }

                runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
            });
        }
    }


    private void loadBibleStories() {
        bibleStories = new ArrayList<>();
        loadFromLocalStorage();

        if (isOnline()) {
            fetchFromFirestore();
        } else {
            adapterBible = new AdapterBible(this, bibleStories);
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
                        bibleStories.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String verse = document.getString("verse");
                            Timestamp timestamp = document.getTimestamp("timestamp");
                            String imageUrl = document.getString("imageUrl");

                            String formattedTimestamp = null;
                            if (timestamp != null) {
                                formattedTimestamp = Converters.fromTimestampToString(timestamp.toDate().getTime());
                            }

                            ModelBible story = new ModelBible(id, title, description, verse, formattedTimestamp, imageUrl);
                            bibleStories.add(story);

                            // Insert story into SQLite and create an achievement entry
                            Executors.newSingleThreadExecutor().execute(() -> {
                                // Insert the story into the Bible table
                                appDatabase.bibleDao().insert(story);

                                // Create an achievement entry related to this story
                                StoryAchievementModel achievement = new StoryAchievementModel(
                                        title, "Story Achievement", id);
                                appDatabase.storyAchievementDao().insert(achievement); // Ensure you have this method in the DAO
                            });
                        }

                        if (adapterBible == null) {
                            adapterBible = new AdapterBible(BibleFragment.this, bibleStories);
                            recyclerView.setAdapter(adapterBible);
                        } else {
                            adapterBible.notifyDataSetChanged();
                        }
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Executors.newSingleThreadExecutor().execute(() -> {
                        List<ModelBible> localStories = appDatabase.bibleDao().getAllBibleStories();
                        if (localStories.isEmpty()) {
                            runOnUiThread(this::showNoConnectionUI);
                        }
                    });
                });
    }





    private void loadFromLocalStorage() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ModelBible> localStories = appDatabase.bibleDao().getAllBibleStories();
            runOnUiThread(() -> {
                if (!localStories.isEmpty()) {
                    bibleStories.clear();
                    bibleStories.addAll(localStories); // Add local stories
                    adapterBible = new AdapterBible(BibleFragment.this, bibleStories);
                    recyclerView.setAdapter(adapterBible);
                } else {
                    // Show "No Data" UI if no stories are found and there's no internet connection
                    if (!isOnline()) {
                        showNoConnectionUI();
                    }
                }
            });
        });
    }

    private void showNoConnectionUI() {
        noConnectionAnimation.setVisibility(View.VISIBLE);
        noConnectionMessage.setVisibility(View.VISIBLE);
        restartButton.setVisibility(View.VISIBLE);

        recyclerView.setVisibility(View.GONE);
        storiesSwitch.setVisibility(View.GONE);
        playlist.setVisibility(View.GONE);
        arrowback.setVisibility(View.GONE);
        comingSoonTextView.setVisibility(View.GONE);
    }


    private void loadAllStories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ModelBible> allStories = appDatabase.bibleDao().getAllBibleStories();

            runOnUiThread(() -> {
                if (allStories.isEmpty()) {
                    comingSoonTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    comingSoonTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapterBible = new AdapterBible(BibleFragment.this, allStories);
                    recyclerView.setAdapter(adapterBible);
                }
            });
        });
    }

    private void loadUpcomingStories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            List<ModelBible> upcomingStories = appDatabase.bibleDao().getUpcomingBibleStories(currentDate);

            runOnUiThread(() -> {
                if (upcomingStories.isEmpty()) {
                    comingSoonTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    comingSoonTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapterBible = new AdapterBible(BibleFragment.this, upcomingStories);
                    recyclerView.setAdapter(adapterBible);
                }
            });
        });
    }
}