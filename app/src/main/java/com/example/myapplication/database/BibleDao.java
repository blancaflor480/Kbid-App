package com.example.myapplication.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapplication.fragment.biblestories.ModelBible;

import java.util.List;

@Dao
public interface BibleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ModelBible story); // This method handles inserting a ModelBible object

    @Query("SELECT * FROM stories WHERE timestamp >= :currentDate ORDER BY timestamp ASC")
    List<ModelBible> getUpcomingBibleStories(String currentDate); // Retrieves upcoming stories

    @Query("SELECT * FROM stories") // Retrieves all stories
    List<ModelBible> getAllBibleStories();

    @Query("SELECT * FROM stories ORDER BY id ASC")  // or DESC depending on desired order
    List<ModelBible> getAllBibleStoriesSortedById();

    @Query("SELECT * FROM stories ORDER BY timestamp DESC")  // Sort by timestamp
    List<ModelBible> getAllBibleStoriesSortedByTimestamp();

    @Query("SELECT * FROM stories WHERE isCompleted = :status") // Retrieves stories based on their completion status ("locked" or "unlocked")
    List<ModelBible> getStoriesByCompletionStatus(String status);

    @Query("SELECT * FROM stories WHERE id = :storyId LIMIT 1")
    ModelBible getStoryById(String storyId);

    @Query("SELECT isCompleted FROM stories WHERE id = :storyId LIMIT 1")
    String getStoryCompletionStatusById(String storyId); // Retrieves only the completion status for a given story

    @Query("SELECT COUNT(*) > 0 FROM stories WHERE id = :storyId AND isCompleted = 'unlocked'")
    boolean isStoryCompleted(String storyId); // Checks if a story is unlocked or not
}
