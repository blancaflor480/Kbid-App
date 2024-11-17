package com.example.myapplication.database.achievement;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapplication.fragment.achievement.StoryAchievementModel;

import java.util.List;

@Dao
public interface StoryAchievementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StoryAchievementModel achievement);

    // Query to get all achievements for Story
    @Query("SELECT * FROM achievements WHERE type = 'Story Achievement'")
    List<StoryAchievementModel> getAchievementsForStory();

    // Query to get all achievements for Games
    @Query("SELECT * FROM achievements WHERE type = 'Games Achievement'")
    List<StoryAchievementModel> getAchievementsForGames();

    // Delete achievement by ID
    @Query("DELETE FROM achievements WHERE id = :achievementId")
    void deleteAchievement(int achievementId);
}
