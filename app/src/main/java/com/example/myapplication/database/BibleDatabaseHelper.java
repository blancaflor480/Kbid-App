package com.example.myapplication.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BibleDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "kbid-app.db";
    private static final int DATABASE_VERSION = 1; // Incremented version for new fields

    private static final String TABLE_NAME = "stories";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FIRESTORE_ID = "firestoreId";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_VERSE = "verse";  // New column
    private static final String COLUMN_DATE = "timestamp";  // New column
    private static final String COLUMN_IMAGE_URL = "imageUrl";  // New column

    public BibleDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_STORIES_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FIRESTORE_ID + " TEXT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_VERSE + " TEXT,"
                + COLUMN_DATE + " DATE,"
                + COLUMN_IMAGE_URL + " TEXT" + ")";
        db.execSQL(CREATE_STORIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
