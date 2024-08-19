package com.example.myapplication.fragment.biblegames;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class GamesFragment extends AppCompatActivity {

    ImageView arrowback;
    RecyclerView recyclerView;
    AdapterGames adapterBible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_bible); // Ensure the correct layout is used

        Log.d("BibleActivity", "RecyclerView and Adapter setup complete.");

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
        recyclerView = findViewById(R.id.recyclep); // Use the correct RecyclerView ID
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load Bible stories
        loadBibleStories();
    }

    private void loadBibleStories() {
        // Create a list of Bible stories
        List<ModelGames> bibleStories = new ArrayList<>();
        bibleStories.add(new ModelGames("Creation of the World", "In the beginning, God created the heavens and the earth."));
        bibleStories.add(new ModelGames("Noah's Ark", "Noah built an ark to save his family and two of every kind of animal."));
        bibleStories.add(new ModelGames("David and Goliath", "David defeated the giant Goliath with a sling and a stone."));
        bibleStories.add(new ModelGames("Daniel in the Lion's Den", "Daniel was saved by God when thrown into a den of lions."));
        bibleStories.add(new ModelGames("Jonah and the Whale", "Jonah was swallowed by a whale for three days and three nights."));

        // Initialize AdapterBible with the list
        adapterBible = new AdapterGames(bibleStories);
        recyclerView.setAdapter(adapterBible);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("BibleActivity", "Navigating back to FragmentHome");
    }
}
