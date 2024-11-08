package com.example.myapplication.fragment.biblemusic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.ui.content.song.ModelSong;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MusicFragment extends AppCompatActivity {
    SwipeRefreshLayout swipeRefreshLayout;
    ImageView arrowback;
    RecyclerView recyclerView;
    AdapterMusic adaptermusic;
    List<ModelMusic> bibleMusic;
    FirebaseFirestore db;
    AppDatabase appDatabase;

    TextView comingSoonTextView;
    LottieAnimationView noConnectionAnimation;
    TextView noConnectionMessage;
    ImageButton restartButton;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_music);

        db = FirebaseFirestore.getInstance();
        appDatabase = AppDatabase.getDatabase(this);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        arrowback = findViewById(R.id.arrowback);
        arrowback.setOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclep);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        comingSoonTextView = findViewById(R.id.comingsoon);
        noConnectionAnimation = findViewById(R.id.noconnection);
        noConnectionMessage = findViewById(R.id.no_connection_message);
        restartButton = findViewById(R.id.restart);

        restartButton.setOnClickListener(v -> refreshBibleMusic());
        swipeRefreshLayout.setOnRefreshListener(this::refreshBibleMusic);

        loadBibleMusic();
    }

    private void refreshBibleMusic() {
        swipeRefreshLayout.setRefreshing(true);
        if (isOnline()) {
            hideNoConnectionUI();
            fetchFromFirestore();
        } else {
            loadFromLocalStorage();
        }
    }

    private void loadBibleMusic() {
        bibleMusic = new ArrayList<>();
        adaptermusic = new AdapterMusic(this, bibleMusic);
        recyclerView.setAdapter(adaptermusic);

        loadFromLocalStorage();

        if (isOnline()) {
            fetchFromFirestore();
        } else {
            adaptermusic.notifyDataSetChanged();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void fetchFromFirestore() {
        db.collection("song")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<ModelMusic> fetchedMusic = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String firebaseId = document.getString("id");
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String timestamp = String.valueOf(document.getTimestamp("timestamp"));
                            String imageUrl = document.getString("imageUrl");
                            String audioUrl = document.getString("audioUrl");

                            ModelMusic music = new ModelMusic(firebaseId, title, audioUrl, description, imageUrl, timestamp);
                            fetchedMusic.add(music);
                        }

                        if (!fetchedMusic.isEmpty()) {
                            bibleMusic.clear();
                            bibleMusic.addAll(fetchedMusic);
                            adaptermusic.notifyDataSetChanged();

                            // Store fetched data into SQLite
                            Executors.newSingleThreadExecutor().execute(() -> {
                                appDatabase.musicDao().deleteAll(); // Clear old records
                                for (ModelMusic music : fetchedMusic) {
                                    appDatabase.musicDao().insert(music); // Insert each record individually
                                }
                            });
                        }
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showNoConnectionUIIfNoData();
                });
    }

    private void loadFromLocalStorage() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ModelMusic> localMusic = appDatabase.musicDao().getAllBibleMusic();
            runOnUiThread(() -> {
                if (!localMusic.isEmpty()) {
                    bibleMusic.clear();
                    bibleMusic.addAll(localMusic);
                    adaptermusic.notifyDataSetChanged();
                } else if (!isOnline()) {
                    showNoConnectionUI();
                }
            });
        });
    }

    private void showNoConnectionUIIfNoData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ModelMusic> localMusic = appDatabase.musicDao().getAllBibleMusic();
            if (localMusic.isEmpty()) {
                runOnUiThread(this::showNoConnectionUI);
            }
        });
    }

    private void hideNoConnectionUI() {
        if (noConnectionAnimation != null) noConnectionAnimation.setVisibility(View.GONE);
        if (noConnectionMessage != null) noConnectionMessage.setVisibility(View.GONE);
        if (restartButton != null) restartButton.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showNoConnectionUI() {
        if (noConnectionAnimation != null) noConnectionAnimation.setVisibility(View.VISIBLE);
        if (noConnectionMessage != null) noConnectionMessage.setVisibility(View.VISIBLE);
        if (restartButton != null) restartButton.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        comingSoonTextView.setVisibility(View.GONE);
    }
}
