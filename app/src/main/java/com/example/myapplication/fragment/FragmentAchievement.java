package com.example.myapplication.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.fragment.achievement.LeaderBoard;
import com.example.myapplication.fragment.achievement.StoryAchievementModel;
import com.example.myapplication.fragment.achievement.StoryAdapterAchievement;
import com.example.myapplication.fragment.biblemusic.MusicFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class FragmentAchievement extends Fragment {

    private ImageView underMaintenance;
    private ImageButton leaderboard;
    private RadioGroup achievementSwitch;
    private RecyclerView recyclepstory;
    private StoryAdapterAchievement storyAdapter;
    private List<StoryAchievementModel> storyList;
    private AppDatabase appDatabase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_achievement, container, false);

        // Initialize the RecyclerView and the achievement list
        leaderboard = rootView.findViewById(R.id.leaderboard);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapterAchievement(requireContext(), storyList);

        recyclepstory = rootView.findViewById(R.id.recyclep);
        recyclepstory.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclepstory.setAdapter(storyAdapter);
        // Initialize the Database Helper
        appDatabase = AppDatabase.getDatabase(requireContext());

        // Initialize the RadioGroup and RadioButtons
        achievementSwitch = rootView.findViewById(R.id.achievement_switch);
        RadioButton storyButton = rootView.findViewById(R.id.story);
        RadioButton gamesButton = rootView.findViewById(R.id.games);

        // Set initial style for RadioButtons
        setRadioButtonStyle(storyButton, true);
        setRadioButtonStyle(gamesButton, false);

        leaderboard.setOnClickListener(v -> {
            Navigateleaderboard();
        });
        // Set up listener for RadioGroup
        achievementSwitch.setOnCheckedChangeListener((group, checkedId) -> {
            resetRadioButtonStyles(storyButton,gamesButton);

            if (checkedId == R.id.story) {
                setRadioButtonStyle(storyButton, true);
                loadAllAchievements(); // Load all achievements// Load achievements for specific story
            } else if (checkedId == R.id.games) {
                setRadioButtonStyle(gamesButton, true);
                loadGameAchievements(); // Load achievements for games
            }
        });

        // Load initial achievements
        loadAllAchievements(); // Default to loading all achievements

        return rootView;
    }

    private void setRadioButtonStyle(RadioButton radioButton, boolean isSelected) {
        if (isSelected) {
            radioButton.setTypeface(null, Typeface.BOLD);
            radioButton.setTextColor(getResources().getColor(android.R.color.black));
            radioButton.setBackgroundResource(R.drawable.bg_selected_achievement);
        } else {
            radioButton.setTypeface(null, Typeface.NORMAL);
            radioButton.setTextColor(getResources().getColor(android.R.color.darker_gray));
            radioButton.setBackgroundResource(R.drawable.bg_unselected_achievement);
        }
    }

    private void resetRadioButtonStyles(RadioButton... buttons) {
        for (RadioButton button : buttons) {
            setRadioButtonStyle(button, false);
        }
    }

    private void loadAllAchievements() {
        Log.d("FragmentAchievement", "Loading all achievements...");
        Executors.newSingleThreadExecutor().execute(() -> {
            List<StoryAchievementModel> localAchievements = appDatabase.storyAchievementDao().getAchievementsForStory();
            Log.d("FragmentAchievement", "Fetched " + localAchievements.size() + " achievements from DB");

            requireActivity().runOnUiThread(() -> {
                updateAchievements(localAchievements);
            });
        });
    }

    private void loadStoryAchievements() {
        Log.d("FragmentAchievement", "Loading achievements for story...");
        Executors.newSingleThreadExecutor().execute(() -> {
            List<StoryAchievementModel> storyAchievements = appDatabase.storyAchievementDao().getAchievementsForStory();
            Log.d("FragmentAchievement", "Fetched " + storyAchievements.size() + " achievements for story.");

            requireActivity().runOnUiThread(() -> {
                updateAchievements(storyAchievements);
            });
        });
    }

    private void loadGameAchievements() {
        Log.d("FragmentAchievement", "Loading achievements for games...");
        Executors.newSingleThreadExecutor().execute(() -> {
            List<StoryAchievementModel> gameAchievements = appDatabase.storyAchievementDao().getAchievementsForStory();
            Log.d("FragmentAchievement", "Fetched " + gameAchievements.size() + " achievements for games.");

            requireActivity().runOnUiThread(() -> {
                updateAchievements(gameAchievements);
            });
        });
    }

    private void updateAchievements(List<StoryAchievementModel> achievements) {
        storyList.clear();
        if (!achievements.isEmpty()) {
            storyList.addAll(achievements);
            storyAdapter.notifyDataSetChanged(); // Update RecyclerView
            Log.d("FragmentAchievement", "Added achievements to list. RecyclerView updated.");
        } else {
            Log.d("FragmentAchievement", "No achievements found.");
        }
    }

    private void Navigateleaderboard() {
        Intent intent = new Intent(getActivity(), LeaderBoard.class);
        startActivity(intent);
    }
}
