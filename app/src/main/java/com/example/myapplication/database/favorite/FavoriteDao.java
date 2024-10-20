package com.example.myapplication.database.favorite;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapplication.fragment.biblestories.favoritelist.FavoriteWithStoryDetails;
import com.example.myapplication.fragment.biblestories.favoritelist.Modelfavoritelist;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Modelfavoritelist favorite); // This method handles inserting a ModelBible object

    @Query("SELECT * FROM favorite")
    List<Modelfavoritelist> getAllBibleStories();

    @Query("SELECT * FROM favorite WHERE storyId = :storyId LIMIT 1")
    Modelfavoritelist getFavoriteByStoryIdAndUserId(String storyId);

    @Query("SELECT f.*, s.title, s.description, s.verse, s.imageUrl " +
            "FROM favorite f " +
            "INNER JOIN stories s ON f.storyId = s.id")
    List<Modelfavoritelist> getAllFavoriteStoriesWithDetails();

    @Query("SELECT f.*, s.title, s.description, s.verse, s.imageUrl " +
            "FROM favorite f " +
            "INNER JOIN stories s ON f.storyId = s.id " +
            "WHERE f.storyId = :storyId LIMIT 1")
    Modelfavoritelist getFavoriteStoryWithDetails(String storyId);


    @Delete // Add this annotation to indicate this method deletes an entity
    void delete(Modelfavoritelist existingFavorite);
}
