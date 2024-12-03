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

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("DELETE FROM user")
    void deleteAllUsers();

    // Corrected query for deleting by UID
    @Query("DELETE FROM user WHERE uid = :uid")
    void deleteByUid(String uid);

    // Optional: Add a query to get user by UID
    @Query("SELECT * FROM user WHERE uid = :uid LIMIT 1")
    User getUserByUid(String uid);

    @Query("DELETE FROM sqlite_sequence WHERE name = 'user'")
    void resetUserTableAutoIncrement();

    // After deleting all users, call this method to reset the sequence
    default void clearUsersAndResetSequence() {
        deleteAllUsers();
        resetUserTableAutoIncrement();
    }
}