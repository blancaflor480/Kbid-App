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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.fragment.achievement.LeaderBoard;
import com.example.myapplication.fragment.achievement.StoryAchievementModel;
import com.example.myapplication.fragment.achievement.StoryAdapterAchievement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class FragmentAchievement extends Fragment {

    private ImageView underMaintenance;
    private ViewPager2 homepage;
    private ImageButton leaderboard;
    private TextView storytitle, gametitle, emptyMessage;
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

        // Initialize the TextViews for storytitle, gametitle, and emptyMessage
        storytitle = rootView.findViewById(R.id.storytitle);
        gametitle = rootView.findViewById(R.id.gametitle);
        emptyMessage = rootView.findViewById(R.id.emptyMessage);
        homepage = rootView.findViewById(R.id.homepage);

        // Set initial style for RadioButtons
        setRadioButtonStyle(storyButton, true);
        setRadioButtonStyle(gamesButton, false);

        leaderboard.setOnClickListener(v -> {
            Navigateleaderboard();
        });

        // Set up listener for RadioGroup
        achievementSwitch.setOnCheckedChangeListener((group, checkedId) -> {
            resetRadioButtonStyles(storyButton, gamesButton);

            if (checkedId == R.id.story) {
                setRadioButtonStyle(storyButton, true);
                loadStoryAchievements();
                showStoryTitle(); // Load achievements for Story
            } else if (checkedId == R.id.games) {
                setRadioButtonStyle(gamesButton, true);
                loadGameAchievements();
                showGameTitle(); // Load achievements for Games
            }
        });

        // Load initial achievements
        loadStoryAchievements(); // Default to loading story achievements

        return rootView;
    }

    private void showStoryTitle() {
        storytitle.setVisibility(View.VISIBLE);
        gametitle.setVisibility(View.GONE);
        homepage.setBackgroundColor(getResources().getColor(R.color.purplelight));
    }

    private void showGameTitle() {
        gametitle.setVisibility(View.VISIBLE);
        storytitle.setVisibility(View.GONE);
        homepage.setBackgroundColor(getResources().getColor(R.color.redlight));
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

    private void loadStoryAchievements() {
        Log.d("FragmentAchievement", "Loading achievements for Story...");
        Executors.newSingleThreadExecutor().execute(() -> {
            // Fetch the sorted achievements from the database
            List<StoryAchievementModel> storyAchievements = appDatabase.storyAchievementDao().getAchievementsForStory();

            // Update the achievement statuses based on story completion
            for (StoryAchievementModel achievement : storyAchievements) {
                // Check if the associated story is completed
                boolean isCompleted = appDatabase.bibleDao().isStoryCompleted(achievement.getStoryId());
                if (isCompleted) {
                    achievement.setIsCompleted("completed");  // Set the achievement status to "completed"
                } else {
                    achievement.setIsCompleted("locked");  // Set the achievement status to "locked"
                }
            }

            // Log the number of fetched achievements for debugging
            Log.d("FragmentAchievement", "Fetched " + storyAchievements.size() + " achievements for Story.");

            // Update the UI on the main thread with the sorted achievements
            requireActivity().runOnUiThread(() -> {
                updateAchievements(storyAchievements);
            });
        });
    }



    private void loadGameAchievements() {
        Log.d("FragmentAchievement", "Loading achievements for Games...");
        Executors.newSingleThreadExecutor().execute(() -> {
            List<StoryAchievementModel> gameAchievements = appDatabase.storyAchievementDao().getAchievementsForGames();
            Log.d("FragmentAchievement", "Fetched " + gameAchievements.size() + " achievements for Games.");

            requireActivity().runOnUiThread(() -> {
                updateAchievements(gameAchievements);
            });
        });
    }

    private void updateAchievements(List<StoryAchievementModel> achievements) {
        storyList.clear();

        if (achievements.isEmpty()) {
            recyclepstory.setVisibility(View.GONE);
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            for (StoryAchievementModel achievement : achievements) {
                // Change appearance based on completed status
                if ("completed".equalsIgnoreCase(achievement.getIsCompleted())) {
                    achievement.setIsCompleted("completed");  // If the achievement is completed
                } else {
                    achievement.setIsCompleted("locked");  // If the achievement is locked
                }
            }

            storyList.addAll(achievements);
            storyAdapter.notifyDataSetChanged();
            recyclepstory.setVisibility(View.VISIBLE);
            emptyMessage.setVisibility(View.GONE);
        }
    }

    private void Navigateleaderboard() {
        Intent intent = new Intent(getActivity(), LeaderBoard.class);
        startActivity(intent);
    }
}
