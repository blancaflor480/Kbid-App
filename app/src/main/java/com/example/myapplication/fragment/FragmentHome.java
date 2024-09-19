package com.example.myapplication.fragment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.fragment.biblegames.GamesFragment;
import com.example.myapplication.fragment.biblestories.BibleFragment;

public class FragmentHome extends Fragment {
    TextView clickStories;
    TextView clickGames;
    VideoView videoView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize VideoView using the view object
        videoView = view.findViewById(R.id.videoView);

        // Set the video URI
        Uri uri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.bg_gif1);
        videoView.setVideoURI(uri);

        // Set video scaling mode for full stretch
        videoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true); // Set video to loop
            mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        });

        // Initialize the TextViews
        clickStories = view.findViewById(R.id.clickStories);
        clickGames = view.findViewById(R.id.clickGames);

        // Set click listeners for the TextViews
        clickStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToBibleActivity();
            }
        });

        clickGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToBiblegamesActivity();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start playing the video when the fragment resumes
        if (videoView != null) {
            videoView.start();
        }
    }

    private void navigateToBibleActivity() {
        Intent intent = new Intent(getActivity(), BibleFragment.class);
        startActivity(intent);
        Log.d("FragmentHome", "Navigating to BibleActivity");
    }

    private void navigateToBiblegamesActivity() {
        Intent intent = new Intent(getActivity(), GamesFragment.class);
        startActivity(intent);
        Log.d("FragmentHome", "Navigating to Biblegames");
    }
}
