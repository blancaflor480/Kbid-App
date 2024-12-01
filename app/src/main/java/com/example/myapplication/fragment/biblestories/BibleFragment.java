package com.example.myapplication.fragment.biblestories;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import static java.security.AccessController.getContext;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.security.AccessControlContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
            // Play the sound on tab switch
            playClickSound();

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

    private void refreshBibleStories() {
        swipeRefreshLayout.setRefreshing(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            // Step 1: Fetch stories from SQLite first
            List<ModelBible> localStories = appDatabase.bibleDao().getAllBibleStoriesSortedByTimestamp();

            // Step 2: Update UI with local stories if available
            runOnUiThread(() -> {
                if (!localStories.isEmpty()) {
                    // Display local stories in RecyclerView
                    updateRecyclerViewWithData(localStories);
                } else {
                    // Show "No Data" UI if SQLite is empty
                    showNoConnectionUI();
                }

                // Step 3: Trigger Firestore fetch to update data if online
                if (isOnline()) {
                    fetchFromFirestore();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        });
    }

    private void updateRecyclerViewWithData(List<ModelBible> stories) {
        if (adapterBible == null) {
            adapterBible = new AdapterBible(BibleFragment.this, stories);
            recyclerView.setAdapter(adapterBible);
        } else {
            adapterBible.updateStories(stories);
        }
        recyclerView.setVisibility(View.VISIBLE);
        storiesSwitch.setVisibility(View.VISIBLE);
        playlist.setVisibility(View.VISIBLE);
        arrowback.setVisibility(View.VISIBLE);
        comingSoonTextView.setVisibility(View.GONE);

        noConnectionAnimation.setVisibility(View.GONE);
        noConnectionMessage.setVisibility(View.GONE);
        restartButton.setVisibility(View.GONE);
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
                        Executors.newSingleThreadExecutor().execute(() -> {
                            List<ModelBible> newStories = new ArrayList<>();

                            for (DocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String title = document.getString("title");
                                String description = document.getString("description");
                                String verse = document.getString("verse");
                                Timestamp timestamp = document.getTimestamp("timestamp");
                                String imageUrl = document.getString("imageUrl");
                                String audioUrl = document.getString("audioUrl");

                                boolean isAudioDownloaded = false;

                                // Properly format the timestamp
                                String formattedTimestamp = "";
                                if (timestamp != null) {
                                    @SuppressLint("SimpleDateFormat")
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    formattedTimestamp = sdf.format(timestamp.toDate());
                                }

                                // Safely retrieve the count value
                                int count = 0; // Default value
                                if (document.contains("count") && document.get("count") != null) {
                                    try {
                                        count = document.getLong("count").intValue();
                                    } catch (Exception e) {
                                        Log.e("FirestoreFetch", "Error parsing count for document ID: " + id, e);
                                    }
                                }

                                ModelBible localStory = appDatabase.bibleDao().getStoryById(id);
                                isAudioDownloaded = (localStory != null) && localStory.isAudioDownloaded();

                                String isCompleted = (localStory != null) ? localStory.getIsCompleted() : document.getString("isCompleted");

                                // Use the formatted timestamp when creating the ModelBible object
                                ModelBible story = new ModelBible(
                                        id,
                                        title,
                                        description,
                                        verse,
                                        formattedTimestamp,
                                        imageUrl,
                                        audioUrl,
                                        isCompleted,
                                        count,
                                        isAudioDownloaded
                                );

                                if (!isAudioDownloaded && audioUrl != null && !audioUrl.isEmpty()) {
                                    downloadAudioFile(id, audioUrl);
                                }

                                if (localStory == null) {
                                    newStories.add(story);
                                }
                            }

                            if (!newStories.isEmpty()) {
                                for (ModelBible newStory : newStories) {
                                    // Insert the story into the Bible table
                                    appDatabase.bibleDao().insert(newStory);
                                    // Create an achievement entry related to this story
                                    StoryAchievementModel achievement = new StoryAchievementModel(
                                            newStory.getTitle(), "Story Achievement", newStory.getIsCompleted(), newStory.getCount(), newStory.getId());
                                    appDatabase.storyAchievementDao().insert(achievement);
                                }
                            }

                            Calendar calendar = Calendar.getInstance();
                            Date currentDate = new Date();

                            // Format current date for comparison
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String currentDateString = dateFormat.format(currentDate);

                            List<ModelBible> updatedStories = appDatabase.bibleDao().getCurrentBibleStoriesBeforeDate(currentDateString);

                            Log.d("SQLiteFetch", "Fetched " + updatedStories.size() + " stories from SQLite.");
                            for (ModelBible story : updatedStories) {
                                Log.d("SQLiteFetch", "Story ID: " + story.getId() + ", Title: " + story.getTitle() + ", Timestamp: " + story.getTimestamp());
                            }

                            // Update the UI on the main thread
                            runOnUiThread(() -> {
                                if (updatedStories.isEmpty()) {
                                    Log.e("RefreshError", "Updated stories list is empty after fetching from SQLite.");
                                    // Show a fallback UI if no stories are available
                                    showNoConnectionUI();
                                } else {
                                    // Update the list and notify the adapter
                                    bibleStories.clear();  // Clear only after successfully fetching updated data
                                    bibleStories.addAll(updatedStories);

                                    if (adapterBible == null) {
                                        adapterBible = new AdapterBible(BibleFragment.this, bibleStories);
                                        recyclerView.setAdapter(adapterBible);
                                    } else {
                                        adapterBible.updateStories(bibleStories);
                                    }

                                    recyclerView.setVisibility(View.VISIBLE);
                                    swipeRefreshLayout.setRefreshing(false);
                                    Log.d("RefreshSuccess", "Bible stories successfully updated in the RecyclerView.");
                                }
                            });
                        });
                    } else {
                        // Handle Firestore failure
                        Log.e("FirestoreFetch", "Failed to fetch stories from Firestore.");
                        swipeRefreshLayout.setRefreshing(false);
                        Executors.newSingleThreadExecutor().execute(() -> {
                            List<ModelBible> localStories = appDatabase.bibleDao().getAllBibleStoriesSortedByTimestamp();
                            runOnUiThread(() -> {
                                if (localStories.isEmpty()) {
                                    showNoConnectionUI();
                                } else {
                                    // Update the list with local stories
                                    bibleStories.clear();
                                    bibleStories.addAll(localStories);

                                    if (adapterBible == null) {
                                        adapterBible = new AdapterBible(BibleFragment.this, bibleStories);
                                        recyclerView.setAdapter(adapterBible);
                                    } else {
                                        adapterBible.updateStories(bibleStories);
                                    }

                                    recyclerView.setVisibility(View.VISIBLE);
                                    swipeRefreshLayout.setRefreshing(false);
                                    Log.d("SQLiteFallback", "Displayed stories from SQLite after Firestore failure.");
                                }
                            });
                        });
                    }
                });
    }

    private void downloadAudioFile(String storyId, String audioUrl) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Context context = BibleFragment.this;

            if (context == null) {
                Log.e("AudioDownload", "Context is null, cannot download audio file");
                return;
            }

            try {
                // Remove the query part of the URL (if any)
                String fileUrlWithoutQuery = audioUrl.split("\\?")[0];

                // Decode the file name from the URL
                String fileName = fileUrlWithoutQuery.substring(fileUrlWithoutQuery.lastIndexOf('/') + 1);
                fileName = URLDecoder.decode(fileName, "UTF-8"); // Decode URL-encoded characters

                // Make sure the file name doesn't have slashes (replace with underscores)
                fileName = fileName.replace("/", "_");

                // Prepare the storage directory
                File storageDir = new File(context.getFilesDir(), "audio_stories/stories/audio");

                // Ensure all parent directories are created
                if (!storageDir.mkdirs() && !storageDir.exists()) {
                    Log.e("AudioDownload", "Failed to create directory: " + storageDir.getAbsolutePath());
                    return;
                }

                // Create the file in the storage directory
                File audioFile = new File(storageDir, fileName);

                // Ensure the parent directory exists before creating the file
                File parentDir = audioFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    if (!parentDir.mkdirs()) {
                        Log.e("AudioDownload", "Failed to create parent directory: " + parentDir.getAbsolutePath());
                        return;
                    }
                }
                Log.d("AudioDownload", "Saving audio to: " + audioFile.getAbsolutePath());
                // Check if file already exists
                if (audioFile.exists()) {
                    Log.d("AudioDownload", "Audio file already exists: " + audioFile.getAbsolutePath());
                    appDatabase.bibleDao().updateAudioDownloadedStatus(storyId, true);
                    return;
                }
                // Download audio file
                URL url = new URL(audioUrl); // Use the full URL with token
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("AudioDownload", "Server returned HTTP " + connection.getResponseCode());
                    return;
                }

                // Ensure the file can be created
                if (!audioFile.createNewFile()) {
                    Log.e("AudioDownload", "Failed to create file: " + audioFile.getAbsolutePath());
                    return;
                }

                // Download the file
                try (InputStream input = connection.getInputStream();
                     FileOutputStream output = new FileOutputStream(audioFile)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                        Log.d("AudioDownload", "Downloaded " + bytesRead + " bytes");
                    }

                } finally {
                    connection.disconnect();
                }

                // Update database if download is successful
                if (audioFile.exists() && audioFile.length() > 0) {
                    Log.d("AudioDownload", "Audio downloaded successfully: " + audioFile.getAbsolutePath());
                    appDatabase.bibleDao().updateAudioDownloadedStatus(storyId, true);
                } else {
                    Log.e("AudioDownload", "Audio file not found or empty after download.");
                }

            } catch (Exception e) {
                Log.e("AudioDownload", "Error downloading audio file", e);
            }
        });
    }

    private void loadFromLocalStorage() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Modify this line to sort by count, starting from 1
            Calendar calendar = Calendar.getInstance();
            Date currentDate = new Date();

            // Format current date for comparison
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateString = dateFormat.format(currentDate);

            List<ModelBible> localStories = appDatabase.bibleDao().getCurrentBibleStoriesBeforeDate(currentDateString);
            runOnUiThread(() -> {
                if (!localStories.isEmpty()) {
                    if (adapterBible == null) {
                        adapterBible = new AdapterBible(BibleFragment.this, localStories);
                        recyclerView.setAdapter(adapterBible);
                    } else {
                        adapterBible.updateStories(localStories);
                    }
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
            // Get the current date
            Calendar calendar = Calendar.getInstance();
            Date currentDate = new Date();

            // Format current date for comparison
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateString = dateFormat.format(currentDate);

            // Fetch current stories (excluding upcoming stories)
            List<ModelBible> currentStories = appDatabase.bibleDao().getCurrentBibleStoriesBeforeDate(currentDateString);

            runOnUiThread(() -> {
                if (currentStories.isEmpty()) {
                    comingSoonTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    comingSoonTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    if (adapterBible == null) {
                        adapterBible = new AdapterBible(BibleFragment.this, currentStories);
                        recyclerView.setAdapter(adapterBible);
                    } else {
                        adapterBible.updateStories(currentStories); // Update adapter data
                    }
                }
            });
        });
    }

    private void loadUpcomingStories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Get the current date and calculate the date one week from now
            Calendar calendar = Calendar.getInstance();
            Date currentDate = new Date();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_YEAR, 7); // Add 7 days
            Date oneWeekFromNow = calendar.getTime();

            // Format dates for comparison
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateString = dateFormat.format(currentDate);
            String oneWeekFromNowString = dateFormat.format(oneWeekFromNow);

            // Fetch upcoming stories within the next week
            List<ModelBible> upcomingStories = appDatabase.bibleDao().getUpcomingBibleStoriesWithinNextWeek(currentDateString, oneWeekFromNowString);

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