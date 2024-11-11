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

    @Query("SELECT * FROM achievements")
    List<StoryAchievementModel> getAchievementsForStory();


    @Query("DELETE FROM achievements WHERE id = :achievementId")
    void deleteAchievement(int achievementId);

}
