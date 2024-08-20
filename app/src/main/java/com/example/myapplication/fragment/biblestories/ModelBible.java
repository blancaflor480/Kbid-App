package com.example.myapplication.fragment.biblestories;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bible_stories")
public class ModelBible {
    @PrimaryKey(autoGenerate = true)
    private int id;  // For Room

    private String firestoreId;  // ID from Firestore
    private String verseName;

    // No-argument constructor required by Firebase Firestore
    public ModelBible() {
    }

    // Constructor without the `id` (for Firestore usage)
    public ModelBible(String firestoreId, String verseName) {
        this.firestoreId = firestoreId;
        this.verseName = verseName;
    }

    // Getters and Setters
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

    public String getVerseName() {
        return verseName;
    }

    public void setVerseName(String verseName) {
        this.verseName = verseName;
    }
}
