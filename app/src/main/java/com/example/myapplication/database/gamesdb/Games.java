package com.example.myapplication.database.gamesdb;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.myapplication.database.Converters;

import java.util.Date;

@Entity(tableName = "games")
@TypeConverters({Converters.class}) // This tells Room to use the Converters class
public class Games {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id; // Room primary key, auto-generated

    private String firestoreId; // Firestore document ID, stored as a string
    private String title;
    private String answer;
    private String level;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private String imageUrl4;
    private String localImagePath1; // Local file path for image 1
    private String localImagePath2; // Local file path for image 2
    private String localImagePath3; // Local file path for image 3
    private String localImagePath4; // Local file path for image 4
    private String timestamp;

    // No-argument constructor
    public Games() {
        // Required for Firebase deserialization
    }

    // Constructor with all arguments (excluding auto-generated ID)
    public Games(String firestoreId, String title, String answer, String level, String imageUrl1, String imageUrl2, String imageUrl3, String imageUrl4, String timestamp) {
        this.firestoreId = firestoreId; // Firestore ID from Firebase
        this.title = title;
        this.answer = answer;
        this.level = level;
        this.imageUrl1 = imageUrl1;
        this.imageUrl2 = imageUrl2;
        this.imageUrl3 = imageUrl3;
        this.imageUrl4 = imageUrl4;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirestoreId() {
        return firestoreId;
    }

    public void setFirestoreId(String firestoreId) {
        this.firestoreId = firestoreId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getImageUrl1() {
        return imageUrl1;
    }

    public void setImageUrl1(String imageUrl1) {
        this.imageUrl1 = imageUrl1;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }

    public void setImageUrl2(String imageUrl2) {
        this.imageUrl2 = imageUrl2;
    }

    public String getImageUrl3() {
        return imageUrl3;
    }

    public void setImageUrl3(String imageUrl3) {
        this.imageUrl3 = imageUrl3;
    }

    public String getImageUrl4() {
        return imageUrl4;
    }

    public void setImageUrl4(String imageUrl4) {
        this.imageUrl4 = imageUrl4;
    }
    public String getLocalImagePath1() {
        return localImagePath1;
    }

    public void setLocalImagePath1(String localImagePath1) {
        this.localImagePath1 = localImagePath1;
    }

    public String getLocalImagePath2() {
        return localImagePath2;
    }

    public void setLocalImagePath2(String localImagePath2) {
        this.localImagePath2 = localImagePath2;
    }

    public String getLocalImagePath3() {
        return localImagePath3;
    }

    public void setLocalImagePath3(String localImagePath3) {
        this.localImagePath3 = localImagePath3;
    }

    public String getLocalImagePath4() {
        return localImagePath4;
    }

    public void setLocalImagePath4(String localImagePath4) {
        this.localImagePath4 = localImagePath4;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestamp(Date timestamp) {
        // Handle Date conversion if necessary
        this.timestamp = timestamp.toString();
    }
}
