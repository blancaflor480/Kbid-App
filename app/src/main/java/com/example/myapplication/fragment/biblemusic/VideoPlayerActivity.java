package com.example.myapplication.fragment.biblemusic;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private Button downloadButton;
    private ImageButton playButton, pauseButton, restartButton, repeatButton;
    private String videoUrl;
    private View controlPanel;  // Control panel for video controls

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player); // Make sure this XML matches the updated layout

        // Initialize the VideoView and Buttons
        videoView = findViewById(R.id.video_view);
        downloadButton = findViewById(R.id.download_button);
        playButton = findViewById(R.id.buttonplay);
        pauseButton = findViewById(R.id.buttonpause);
        restartButton = findViewById(R.id.buttonrestart);
        repeatButton = findViewById(R.id.buttonrepeat);
        controlPanel = findViewById(R.id.control_panel);  // Control panel for play/pause buttons

        // Get the video URL passed from the previous activity
        Intent intent = getIntent();
        videoUrl = intent.getStringExtra("videoUrl");

        // If the video URL is valid, prepare the video player
        if (videoUrl != null && !videoUrl.isEmpty()) {
            videoView.setVideoURI(Uri.parse(videoUrl));

            // Set listener to ensure the media is prepared before starting playback
            videoView.setOnPreparedListener(mp -> {
                // Start the video after it is prepared
                videoView.start();
                playButton.setVisibility(View.GONE);  // Hide the play button
                pauseButton.setVisibility(View.VISIBLE);  // Show the pause button
                controlPanel.setVisibility(View.GONE); // Show the control panel
            });

            // Handle error listener if video fails to load
            videoView.setOnErrorListener((mp, what, extra) -> {
                // Handle the error here, e.g., show a message to the user
                return true; // Indicating the error has been handled
            });

            // Request focus for the video to ensure it plays
            videoView.requestFocus();
        }

        // Set up the download button click listener
        downloadButton.setOnClickListener(v -> downloadVideo(videoUrl));

        // Set up play/pause buttons
        playButton.setOnClickListener(v -> playVideo());
        pauseButton.setOnClickListener(v -> pauseVideo());

        // Set up the rewind, forward, and restart buttons
        restartButton.setOnClickListener(v -> restartVideo());

        // Set up repeat button if necessary
        repeatButton.setOnClickListener(v -> repeatVideo());

        // Show the control panel when the video is clicked
        videoView.setOnClickListener(v -> {
            if (controlPanel.getVisibility() == View.GONE) {
                controlPanel.setVisibility(View.VISIBLE);  // Show control panel if it's hidden
            } else {
                controlPanel.setVisibility(View.GONE);  // Hide control panel if it's visible
            }
        });
    }

    // Method to play the video
    private void playVideo() {
        if (!videoView.isPlaying()) {
            videoView.start();
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }
    }

    // Method to pause the video
    private void pauseVideo() {
        if (videoView.isPlaying()) {
            videoView.pause();
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        }
    }

    // Method to restart the video from the beginning
    private void restartVideo() {
        videoView.seekTo(0);
        videoView.start();
    }

    // Method to repeat the video (you could use this if needed for loop)
    private void repeatVideo() {
        videoView.seekTo(0);
        videoView.start();
    }

    // Method to download the video
    private void downloadVideo(String url) {
        if (url != null && !url.isEmpty()) {
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);

            // Set download request parameters
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir("/Download", "downloaded_video.mp4");

            // Enqueue the download request
            downloadManager.enqueue(request);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set the activity orientation to landscape when the activity is resumed
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the video if it's playing when the activity is paused
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Optionally, reset the video view when the back button is pressed
        videoView.stopPlayback();
    }
}
