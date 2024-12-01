package com.example.myapplication;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.adapter.AdapterViewPager;
import com.example.myapplication.fragment.FragmentAchievement;
import com.example.myapplication.fragment.FragmentHome;
import com.example.myapplication.fragment.FragmentSettings;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private static final int BACK_PRESS_INTERVAL = 2000;
    ViewPager2 homepage;
    ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
    BottomNavigationView bottom_nav;
    MediaPlayer mediaPlayer;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Initialize MediaPlayer with the audio resource
        mediaPlayer = MediaPlayer.create(this, R.raw.bg_music); // Replace 'your_audio_file' with the actual file name
        // Set the MediaPlayer to loop
        mediaPlayer.setLooping(true); // Enable looping

        homepage = findViewById(R.id.homepage);
        bottom_nav = findViewById(R.id.bottom_nav);

        fragmentArrayList.add(new FragmentHome());
        fragmentArrayList.add(new FragmentAchievement());
        fragmentArrayList.add(new FragmentSettings());

        AdapterViewPager adapterViewPager = new AdapterViewPager(this, fragmentArrayList);
        homepage.setAdapter(adapterViewPager);

        homepage.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottom_nav.setSelectedItemId(R.id.itHome);
                        break;
                    case 1:
                        bottom_nav.setSelectedItemId(R.id.itAchievement);
                        break;
                    case 2:
                        bottom_nav.setSelectedItemId(R.id.itSettings);
                        break;
                }
                super.onPageSelected(position);
            }
        });

        bottom_nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.itHome){
                    homepage.setCurrentItem(0);
                } else if (itemId == R.id.itAchievement) {
                    homepage.setCurrentItem(1);
                }else if (itemId == R.id.itSettings) {
                    homepage.setCurrentItem(2);
                }
                return true;
            }
        });
        mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the music when the activity is not in the foreground
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the music when the activity comes back to the foreground
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }
    public void toggleMusic(boolean playMusic) {
        if (playMusic) {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } else {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }
    @Override
    public void onBackPressed() {
        // Check if dialog is not already showing

        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit the app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity(); // Exit the app
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel(); // Dismiss the dialog
                    }
                })
                .setCancelable(true) // Allow dialog to be cancelable
                .show();
        super.onBackPressed();
    }
}

