package com.example.myapplication.fragment.biblestories;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stories")
public class ModelBible {
    @PrimaryKey
    @NonNull // Ensure the primary key cannot be null
    private String id;  // Firestore ID as the primary key
    private String title;  // Title of the story
    private String description;  // Description of the story
    private String verse; // New field for verse
    private String imageUrl; // New field for image URL

    // No-argument constructor required by Firebase Firestore
    public ModelBible() {
    }

    // Constructor to initialize the ModelBible object
    public ModelBible(@NonNull String id, String title, String description, String verse, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.verse = verse;
        this.imageUrl = imageUrl;
    }

    // Getter for the Firestore ID
    @NonNull  // Indicate that this method will never return null
    public String getId() {
        return id;  // Return the Firestore ID
    }

    public void setId(@NonNull String id) {
        this.id = id;  // Set the Firestore ID
    }

    public String getTitle() {
        return title;  // Return the title
    }

    public void setTitle(String title) {
        this.title = title;  // Set the title
    }

    public String getDescription() {
        return description;  // Return the description
    }

    public void setDescription(String description) {
        this.description = description;  // Set the description
    }
    public String getVerse() { return verse; }
    public void setVerse(String verse) { this.verse = verse; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
