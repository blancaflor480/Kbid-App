package com.example.myapplication.database.userdb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Query("SELECT * FROM user WHERE id = 1 LIMIT 1")
    User getFirstUser();

    @Update
    void updateUser(User user);

   // @Query("SELECT level FROM user WHERE id = 1 LIMIT 1")
    //int getUserLevel();

    //@Query("SELECT COUNT(*) FROM user WHERE id = 1")
    //int countUserWithIdOne();
}
