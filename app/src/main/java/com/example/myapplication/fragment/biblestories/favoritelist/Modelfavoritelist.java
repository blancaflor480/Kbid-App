package com.example.myapplication.fragment.biblestories.favoritelist;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.myapplication.database.Converters;
import com.example.myapplication.fragment.biblestories.ModelBible;

@Entity(tableName = "favorite",
        foreignKeys = @ForeignKey(entity = ModelBible.class,  // Use the correct Story entity
                parentColumns = "id", // Make sure this matches the primary key of Story
                childColumns = "storyId",
                onDelete = ForeignKey.CASCADE))
@TypeConverters({Converters.class})  // Ensure the Timestamp is handled correctly with Room
public class Modelfavoritelist {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;  // Change to String to match the Firestore ID

    private String storyId; // This should reference the 'id' in the Story entity

    private String userId;


    private String title;  // Title of the story
    private String description;  // Description of the story
    private String verse; // Bible verse
    private String timestamp;  // Formatted date as String
    private String imageUrl;  // Image URL for the story

    // Default constructor
    public Modelfavoritelist() {}


    // Constructor to initialize the object
    public Modelfavoritelist(String storyId, String title, String description, String verse, String timestamp, String imageUrl) {
        this.storyId = storyId;
        this.title = title;
        this.description = description;
        this.verse = verse;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
    }

    // Getters and setters for all fields
    @NonNull
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // Getter and setter for storyId
    public String getStoryId() {
        return storyId;
    }
    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    // Getter and setter for userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVerse() {
        return verse;
    }

    public void setVerse(String verse) {
        this.verse = verse;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
