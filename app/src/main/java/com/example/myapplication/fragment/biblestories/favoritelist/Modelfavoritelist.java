package com.example.myapplication.fragment.biblestories.favoritelist;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.myapplication.database.Converters;
import com.google.firebase.Timestamp;

@Entity(tableName = "favorite")
@TypeConverters({Converters.class}) // Ensure the Timestamp is handled correctly with Room
public class Modelfavoritelist {
    @PrimaryKey
    @NonNull
    private String id;  // Firestore ID as the primary key

    private String title;  // Title of the story
    private String description;  // Description of the story
    private String verse; // New field for verse
    private String timestamp;  // Change this to String for formatted date
    private String imageUrl; // New field for image URL

    public Modelfavoritelist(){

    }

    // Constructor to initialize the ModelBible object
    public Modelfavoritelist(@NonNull String id, String title, String description, String verse, String timestamp, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.verse = verse;
        this.timestamp = timestamp; // Updated to String
        this.imageUrl = imageUrl;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
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
        return timestamp; // Change return type to String
    }

    public void setTimestamp(String timestamp) { // Change parameter type to String
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


}
