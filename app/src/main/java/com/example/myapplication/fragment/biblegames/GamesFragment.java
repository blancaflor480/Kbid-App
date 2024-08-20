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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_games); // Ensure the correct layout is used

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

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("BibleActivity", "Navigating back to FragmentHome");
    }
}
