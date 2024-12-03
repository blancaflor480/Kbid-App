package com.example.myapplication.database.achievement;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.fragment.achievement.StoryAchievementModel;

import java.util.List;

@Dao
public interface StoryAchievementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(StoryAchievementModel achievement);

    // Query to get all achievements for Story
    @Query("SELECT * FROM achievements WHERE type = 'Story Achievement' ORDER BY count ASC")
    List<StoryAchievementModel> getAchievementsForStory();

    @Query("DELETE FROM achievements")
    void deleteAllUserAchievements();
    // Query to get all achievements for Games
    @Query("SELECT * FROM achievements WHERE type = 'Games Achievement'")
    List<StoryAchievementModel> getAchievementsForGames();

    @Query("DELETE FROM achievements")
    void deleteAllStoryAchievements();

    @Query("SELECT COUNT(*) FROM achievements")
    int getAchievementsCount();

    // Delete achievement by ID
    @Query("DELETE FROM achievements WHERE id = :achievementId")
    void deleteAchievement(int achievementId);

    @Query("SELECT * FROM achievements WHERE storyId = :storyId LIMIT 1")
    StoryAchievementModel getAchievementByStoryId(String storyId);

    @Query("SELECT COUNT(*) > 0 FROM stories WHERE id = :storyId AND isCompleted = 'completed'")
    boolean isStoryCompleted(String storyId); // Checks if a story is unlocked or not

    @Query("SELECT * FROM achievements ORDER BY count ASC")
    List<StoryAchievementModel> getAllAchievements();

    @Query("SELECT COUNT(*) FROM achievements WHERE isCompleted = 'completed'")
    int getCompletedStoryCount();

    @Query("SELECT * FROM achievements")
    List<StoryAchievementModel> getAllStoryAchievements();

    @Update
    void update(StoryAchievementModel achievement);
}
