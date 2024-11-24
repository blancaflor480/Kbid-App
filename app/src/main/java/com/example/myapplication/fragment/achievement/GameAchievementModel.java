package com.example.myapplication.fragment.achievement;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.myapplication.database.gamesdb.DataFetcher;
import com.example.myapplication.fragment.biblegames.fourpiconeword.FourPicOneword;
import com.example.myapplication.fragment.biblestories.ModelBible;

@Entity(
        tableName = "gamesachievements"
)
public class GameAchievementModel {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String gameId;
    private String title;
    private String level;
    private String points;
    private String isCompleted;

    // Constructor used by Room
    public GameAchievementModel(int id, String gameId, String title, String level, String points, String isCompleted) {
        this.id = id;
        this.gameId = gameId;
        this.title = title;
        this.level = level;
        this.points = points;
        this.isCompleted = isCompleted;
    }

    // Constructor ignored by Room
    @Ignore
    public GameAchievementModel(String gameId, String title, String level, String points, String isCompleted) {
        this.gameId = gameId;
        this.title = title;
        this.level = level;
        this.points = points;
        this.isCompleted = isCompleted;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(String isCompleted) {
        this.isCompleted = isCompleted;
    }
}

