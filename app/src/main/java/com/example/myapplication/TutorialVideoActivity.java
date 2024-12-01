package com.example.myapplication;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class TutorialVideoActivity extends AppCompatActivity {
    private VideoView tutorialVideoView;
    private ImageButton closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Force landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.tutorial_video);

        tutorialVideoView = findViewById(R.id.tutorialVideoView);
        closeButton = findViewById(R.id.closeVideoButton);

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.tutorialhomepage;
        tutorialVideoView.setVideoURI(Uri.parse(videoPath));

        tutorialVideoView.setOnPreparedListener(mp -> tutorialVideoView.start());

        closeButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tutorialVideoView != null) {
            tutorialVideoView.stopPlayback();
        }
    }
}