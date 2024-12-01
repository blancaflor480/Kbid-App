package com.example.myapplication.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.example.myapplication.fragment.achievement.GameAchievementModel;
import com.example.myapplication.fragment.achievement.GameAdapterAchievement;
import com.example.myapplication.fragment.achievement.LeaderBoard;
import com.example.myapplication.fragment.achievement.StoryAchievementModel;
import com.example.myapplication.fragment.achievement.StoryAdapterAchievement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class FragmentAchievement extends Fragment {

    private ViewPager2 homepage;
    private ImageButton leaderboard;
    private TextView storytitle, gametitle, emptyMessage;
    private RadioGroup achievementSwitch;
    private RecyclerView recyclepstory, recyclepgame;
    private StoryAdapterAchievement storyAdapter;
    private GameAdapterAchievement gameAdapter;
    private List<GameAchievementModel> gameList;
    private List<StoryAchievementModel> storyList;
    private AppDatabase appDatabase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_achievement, container, false);

        // Initialize Views
        leaderboard = rootView.findViewById(R.id.leaderboard);
        recyclepstory = rootView.findViewById(R.id.recyclepstory);
        recyclepgame = rootView.findViewById(R.id.recyclepgame);
        storytitle = rootView.findViewById(R.id.storytitle);
        gametitle = rootView.findViewById(R.id.gametitle);
        emptyMessage = rootView.findViewById(R.id.emptyMessage);
        achievementSwitch = rootView.findViewById(R.id.achievement_switch);
        homepage = rootView.findViewById(R.id.homepage);

        // Initialize RecyclerView and Adapters
        storyList = new ArrayList<>();
        gameList = new ArrayList<>();
        storyAdapter = new StoryAdapterAchievement(requireContext(), storyList);
        gameAdapter = new GameAdapterAchievement(requireContext(), gameList);

        recyclepstory.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclepgame.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclepstory.setAdapter(storyAdapter);
        recyclepgame.setAdapter(gameAdapter);

        // Initialize Database
        appDatabase = AppDatabase.getDatabase(requireContext());

        // Set Initial State
        RadioButton storyButton = rootView.findViewById(R.id.story);
        RadioButton gamesButton = rootView.findViewById(R.id.games);
        setRadioButtonStyle(storyButton, true);
        setRadioButtonStyle(gamesButton, false);
        loadStoryAchievements(); // Default to story achievements

        // Listeners
        leaderboard.setOnClickListener(v -> navigateLeaderboard());
        storyButton.setOnClickListener(v -> {
            toggleAchievements(true);
        });
        gamesButton.setOnClickListener(v -> {
            toggleAchievements(false);
        });
        achievementSwitch.setOnCheckedChangeListener((group, checkedId) -> {
            resetRadioButtonStyles(storyButton, gamesButton);
            if (checkedId == R.id.story) {
                setRadioButtonStyle(storyButton, true);
                loadStoryAchievements();
            } else if (checkedId == R.id.games) {
                setRadioButtonStyle(gamesButton, true);
                loadGameAchievements();
            }
        });

        return rootView;
    }

    private void toggleAchievements(boolean isStory) {
        if (isStory) {
            recyclepstory.setVisibility(View.VISIBLE);
            recyclepgame.setVisibility(View.GONE);
            storytitle.setVisibility(View.VISIBLE);
            gametitle.setVisibility(View.GONE);
        } else {
            recyclepstory.setVisibility(View.GONE);
            recyclepgame.setVisibility(View.VISIBLE);
            storytitle.setVisibility(View.GONE);
            gametitle.setVisibility(View.VISIBLE);
        }
    }

    private void setRadioButtonStyle(RadioButton radioButton, boolean isSelected) {
        radioButton.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
        radioButton.setTextColor(getResources().getColor(isSelected ? android.R.color.black : android.R.color.darker_gray));
        radioButton.setBackgroundResource(isSelected ? R.drawable.bg_selected_achievement : R.drawable.bg_unselected_achievement);
    }

    private void resetRadioButtonStyles(RadioButton... buttons) {
        for (RadioButton button : buttons) {
            setRadioButtonStyle(button, false);
        }
    }

    private void loadStoryAchievements() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<StoryAchievementModel> storyAchievements = appDatabase.storyAchievementDao().getAchievementsForStory();
            for (StoryAchievementModel achievement : storyAchievements) {
                achievement.setIsCompleted(appDatabase.storyAchievementDao().isStoryCompleted(achievement.getStoryId()) ? "completed" : "locked");
            }
            requireActivity().runOnUiThread(() -> updateAchievements(storyAchievements));
        });
    }

    private void loadGameAchievements() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Fetch all game achievements
            List<GameAchievementModel> gameAchievements = appDatabase.gameAchievementDao().getAchievementsForGames();

            for (GameAchievementModel achievement : gameAchievements) {
                // Create a unique key based on gameId and level (or other unique fields)
                achievement.setIsCompleted(appDatabase.gameAchievementDao().isGameCompleted(achievement.getGameId()) ? "completed" : "locked");
            }
            requireActivity().runOnUiThread(() -> updateGameAchievements(gameAchievements));
        });
    }



    private void updateAchievements(List<StoryAchievementModel> achievements) {
        storyList.clear();
        if (achievements.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            storyList.addAll(achievements);
        }
        storyAdapter.notifyDataSetChanged();
    }

    private void updateGameAchievements(List<GameAchievementModel> achievements) {
        gameList.clear();
        if (achievements.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            gameList.addAll(achievements);
        }
        gameAdapter.notifyDataSetChanged();
    }

    private void navigateLeaderboard() {
        startActivity(new Intent(getActivity(), LeaderBoard.class));
    }
}
