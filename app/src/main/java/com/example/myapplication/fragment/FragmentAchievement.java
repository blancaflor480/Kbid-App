package com.example.myapplication.fragment;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.BibleDatabaseHelper;
import com.example.myapplication.database.achievement.StoryAchievementDao;
import com.example.myapplication.fragment.achievement.StoryAchievementModel;
import com.example.myapplication.fragment.achievement.StoryAdapterAchievement;

import java.util.ArrayList;
import java.util.List;

public class FragmentAchievement extends Fragment {

    private ImageView underMaintenance;
    private RadioGroup achievementSwitch;
    private RecyclerView recyclepstory;
    private StoryAdapterAchievement storyAdapter;
    private List<StoryAchievementModel> storyList;

    @SuppressLint("NonConstantResourceId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_achievement, container, false);

        // Initialize the RecyclerView and the achievement list
        recyclepstory = view.findViewById(R.id.recyclepstory);
        storyList = new ArrayList<>(); // Initialize the list here
        storyAdapter = new StoryAdapterAchievement(storyList);
        recyclepstory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclepstory.setAdapter(storyAdapter); // Set the adapter to the RecyclerView

        // Initialize the RadioGroup
        achievementSwitch = view.findViewById(R.id.achievement_switch);

        // Initialize RadioButtons
        RadioButton allrecord = view.findViewById(R.id.allrecord);
        RadioButton story = view.findViewById(R.id.story);
        RadioButton games = view.findViewById(R.id.games);

        // Initially set the correct background and text style
        setRadioButtonStyle(allrecord, true); // Selected
        setRadioButtonStyle(story, false); // Unselected
        setRadioButtonStyle(games, false); // Unselected

        String storyId = String.valueOf(getFirstStoryId());
        insertAchievement(storyId);
        // Set up listener for RadioGroup
        achievementSwitch.setOnCheckedChangeListener((group, checkedId) -> {
            setRadioButtonStyle(allrecord, false);
            setRadioButtonStyle(story, false);
            setRadioButtonStyle(games, false);

            if (checkedId == R.id.allrecord) {
                setRadioButtonStyle(allrecord, true);
                loadAchievements(null); // Load all achievements
            } else if (checkedId == R.id.story) {
                setRadioButtonStyle(story, true);
                String selectedStoryId = getFirstStoryId(); // Replace with actual method to get story ID
                loadAchievements(String.valueOf(selectedStoryId)); // Load achievements for specific story
            } else if (checkedId == R.id.games) {
                setRadioButtonStyle(games, true);
                loadAchievements(null); // Optionally handle games separately
            }
        });

        // Load initial achievements
        loadAchievements(null);

        return view;
    }

    private void setRadioButtonStyle(RadioButton radioButton, boolean isSelected) {
        if (isSelected) {
            radioButton.setTypeface(null, Typeface.BOLD); // Set text to bold
            radioButton.setTextColor(getResources().getColor(android.R.color.black)); // Set text color to black
            radioButton.setBackgroundResource(R.drawable.bg_selected_achievement);
        } else {
            radioButton.setTypeface(null, Typeface.NORMAL); // Set text to normal
            radioButton.setTextColor(getResources().getColor(android.R.color.darker_gray)); // Set text color to gray
            radioButton.setBackgroundResource(R.drawable.bg_unselected_achievement);
        }
    }
    private boolean isStoryIdValid(String storyId) {
        BibleDatabaseHelper dbHelper = new BibleDatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BibleDatabaseHelper.getTableStories() + " WHERE " + BibleDatabaseHelper.getColumnId() + " = ?", new String[]{storyId});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }


    private void insertAchievement(String storyId) {
        if (!isStoryIdValid(storyId)) {
            // Log a message if the storyId is not valid
            Log.e("FragmentAchievement", "Invalid storyId: " + storyId);
            return; // Handle the case where storyId does not exist
        }

        // Proceed with insertion
        AppDatabase db = AppDatabase.getDatabase(getContext());
        StoryAchievementDao achievementDao = db.storyAchievementDao();

        StoryAchievementModel achievement = new StoryAchievementModel("Read Story", "Completion", storyId);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                achievementDao.insertAchievement(achievement);
                Log.d("FragmentAchievement", "Achievement inserted successfully for storyId: " + storyId);
            } catch (SQLiteConstraintException e) {
                Log.e("FragmentAchievement", "Failed to insert achievement: " + e.getMessage());
            }
        });
    }



    private void loadAchievements(String storyId) {
        BibleDatabaseHelper dbHelper = new BibleDatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor;
        if (storyId == null) {
            cursor = db.rawQuery("SELECT * FROM " + BibleDatabaseHelper.getTableAchievements(), null);
        } else {
            cursor = db.rawQuery("SELECT * FROM " + BibleDatabaseHelper.getTableAchievements() +
                    " WHERE " + BibleDatabaseHelper.getColumnStoryId() + " = ?", new String[]{storyId});
        }

        if (cursor != null) {
            storyList.clear();

            while (cursor.moveToNext()) {
                StoryAchievementModel achievement = new StoryAchievementModel();

                int idIndex = cursor.getColumnIndex(BibleDatabaseHelper.getColumnAchievementId());
                int storyIdIndex = cursor.getColumnIndex(BibleDatabaseHelper.getColumnStoryId()); // Correctly get the index as int
                int titleIndex = cursor.getColumnIndex(BibleDatabaseHelper.getColumnAchievementTitle());

                if (idIndex >= 0) {
                    achievement.setId(cursor.getInt(idIndex)); // No need for parseInt here
                }
                if (storyIdIndex >= 0) {
                    achievement.setStoryId(cursor.getString(storyIdIndex)); // Retrieve directly
                }
                if (titleIndex >= 0) {
                    achievement.setTitle(cursor.getString(titleIndex));
                }

                storyList.add(achievement);
            }
            cursor.close();
        }

        storyAdapter.notifyDataSetChanged();
        db.close();
    }


    // Method to get the ID of the first story, replace with your logic
    private String getFirstStoryId() {
        BibleDatabaseHelper dbHelper = new BibleDatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String storyId = null;

        Cursor cursor = db.rawQuery("SELECT " + BibleDatabaseHelper.getColumnId() + " FROM " + BibleDatabaseHelper.getTableStories() + " LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(BibleDatabaseHelper.getColumnId());
            if (idIndex >= 0) { // Check if the column index is valid
                storyId = String.valueOf(cursor.getInt(idIndex));
            }
            cursor.close();
        }

        db.close();
        return storyId;
    }


}
