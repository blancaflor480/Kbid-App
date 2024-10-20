package com.example.myapplication.fragment.biblestories.favoritelist;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.myapplication.database.Converters;
import com.example.myapplication.fragment.biblestories.ModelBible;

@Entity(tableName = "favorite",
        foreignKeys = @ForeignKey(
                entity = ModelBible.class,  // Reference to the ModelBible (stories) entity
                parentColumns = "id",       // The primary key of the ModelBible table
                childColumns = "storyId",   // Foreign key in the favorite table
                onDelete = ForeignKey.CASCADE))  // Cascade delete to remove favorites if the story is deleted
@TypeConverters({Converters.class})  // Convert complex types like Timestamps
public class FavoriteWithStoryDetails {

    @PrimaryKey(autoGenerate = true)
    private int id; // Primary key for the favorite entry

    private String storyId; // Foreign key referencing the ModelBible's id field
    private String title;    // Story title from ModelBible
    private String description; // Story description from ModelBible
    private String verse;    // Bible verse from ModelBible
    private String imageUrl; // Image URL from ModelBible

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
