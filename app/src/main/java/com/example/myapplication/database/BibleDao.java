package com.example.myapplication.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("SELECT * FROM stories ORDER BY CAST(timestamp AS INTEGER) ASC")
    List<ModelBible> getAllBibleStoriesSortedByTimestamp();


    @Query("SELECT * FROM stories WHERE isCompleted = :status") // Retrieves stories based on their completion status ("locked" or "unlocked")
    List<ModelBible> getStoriesByCompletionStatus(String status);

    @Query("SELECT * FROM stories WHERE id = :storyId LIMIT 1")
    ModelBible getStoryById(String storyId);

    @Query("SELECT isCompleted FROM stories WHERE id = :storyId LIMIT 1")
    String getStoryCompletionStatusById(String storyId); // Retrieves only the completion status for a given story

    @Query("SELECT COUNT(*) > 0 FROM stories WHERE id = :storyId AND isCompleted = 'unlocked'")
    boolean isStoryCompleted(String storyId); // Checks if a story is unlocked or not

    @Query("SELECT * FROM stories WHERE CAST(id AS INTEGER) > CAST(:currentStoryId AS INTEGER) AND isCompleted = 'locked' ORDER BY CAST(id AS INTEGER) ASC LIMIT 1")
    ModelBible getNextStory(String currentStoryId);

    @Query("SELECT * FROM stories WHERE count = :count LIMIT 1")
    ModelBible getStoryByCount(int count);


    @Update
    void update(ModelBible story);
}
