package com.example.myapplication.fragment.biblestories.playlist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

public class BiblePlay extends AppCompatActivity {

    private TextView titleView, verseView, storyView;
    private ImageView thumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_strories);

        // Initialize views
        titleView = findViewById(R.id.title);
        verseView = findViewById(R.id.verse);
        storyView = findViewById(R.id.textstory);
        thumbnail = findViewById(R.id.thumbnail);

        // Get data from intent
        String storyId = getIntent().getStringExtra("id");
        String title = getIntent().getStringExtra("title");
        String verse = getIntent().getStringExtra("verse");
        String description = getIntent().getStringExtra("description");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String timestamp = getIntent().getStringExtra("timestamp");

        // Set data to views
        titleView.setText(title);
        storyView.setText(description);
        verseView.setText(verse);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.image) // Default image while loading
                    .error(R.drawable.image) // Default image in case of error
                    .into(thumbnail);
        } else {
            thumbnail.setImageResource(R.drawable.image);
        }
    }
}
