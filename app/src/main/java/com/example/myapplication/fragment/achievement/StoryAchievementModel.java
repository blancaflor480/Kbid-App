package com.example.myapplication.fragment.achievement;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.myapplication.fragment.biblestories.ModelBible;

@Entity(
        tableName = "achievements",
        foreignKeys = @ForeignKey(
                entity = ModelBible.class,
                parentColumns = "id", // Primary key in ModelBible
                childColumns = "storyId", // Foreign key in StoryAchievementModel
                onDelete = ForeignKey.CASCADE // Cascade delete if the referenced story is deleted
        )
)
public class StoryAchievementModel {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String type;
    private String storyId; // Foreign key

    public StoryAchievementModel(String title, String type, String storyId) {
        this.title = title;
        this.type = type;
        this.storyId = storyId;
    }


    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

}
