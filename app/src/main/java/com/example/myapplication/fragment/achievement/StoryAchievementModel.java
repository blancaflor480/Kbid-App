package com.example.myapplication.fragment.achievement;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.myapplication.fragment.biblestories.ModelBible;

@Entity(
        tableName = "achievements"
)
public class StoryAchievementModel {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String type;
    private String storyId;
    private String isCompleted;
    private int count; // Foreign key

    public StoryAchievementModel(){

    }

    public StoryAchievementModel(String title, String type, String isCompleted, int count, String storyId) {
        this.title = title;
        this.type = type;
        this.storyId = storyId;
        this.isCompleted = isCompleted;
        this.count = count;
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

    // Corrected getter method for 'isCompleted'
    public String getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(String isCompleted) {
        this.isCompleted = isCompleted;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
