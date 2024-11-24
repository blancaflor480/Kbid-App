package com.example.myapplication.database.achievement;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.fragment.achievement.GameAchievementModel;
import java.util.List;
@Dao
public interface GameAchievementDao {
    @Insert
    void insert(GameAchievementModel achievement);

    @Update
    void update(GameAchievementModel achievement);

    @Query("DELETE FROM gamesachievements")
    void deleteAll();

    @Query("SELECT * FROM gamesachievements")
    List<GameAchievementModel> getAllAchievements();

    @Query("SELECT * FROM gamesachievements WHERE gameId = :gameId")
    GameAchievementModel getAchievementForGame(String gameId);

    @Query("UPDATE gamesachievements SET points = :points, isCompleted = :isCompleted WHERE gameId = :gameId AND level = :level")
    void updateAchievement(String gameId, String level, String points, String isCompleted);

    // Optional: Retrieve the current game achievement for validation or display
    @Query("SELECT * FROM gamesachievements WHERE gameId = :gameId AND level = :level LIMIT 1")
    GameAchievementModel getAchievement(String gameId, String level);

}
