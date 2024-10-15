package com.example.myapplication.database.favorite;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapplication.fragment.biblestories.favoritelist.Modelfavoritelist;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Modelfavoritelist story); // This method handles inserting a ModelBible object

    @Query("SELECT * FROM favorite")
    List<Modelfavoritelist> getAllBibleStories();

}
