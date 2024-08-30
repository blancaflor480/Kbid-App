package com.example.myapplication.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.myapplication.fragment.biblestories.ModelBible;

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

    private static final String CREATE_STORIES_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FIRESTORE_ID + " TEXT,"
            + COLUMN_TITLE + " TEXT,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_VERSE + " TEXT,"
            + COLUMN_DATE + " DATE,"
            + COLUMN_IMAGE_URL + " TEXT" + ")";

    public BibleDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public ModelBible getNextStory(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ModelBible story = null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " > ? ORDER BY " + COLUMN_ID + " LIMIT 1", new String[]{String.valueOf(id)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                story = new ModelBible();
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
                int descriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION);
                int verseIndex = cursor.getColumnIndex(COLUMN_VERSE);
                int imageUrlIndex = cursor.getColumnIndex(COLUMN_IMAGE_URL);

                // Ensure indexes are valid before accessing the cursor
                if (idIndex != -1) {
                    story.setId(String.valueOf(cursor.getInt(idIndex))); // Convert int to String
                }
                if (titleIndex != -1) {
                    story.setTitle(cursor.getString(titleIndex));
                }
                if (descriptionIndex != -1) {
                    story.setDescription(cursor.getString(descriptionIndex));
                }
                if (verseIndex != -1) {
                    story.setVerse(cursor.getString(verseIndex));
                }
                if (imageUrlIndex != -1) {
                    story.setImageUrl(cursor.getString(imageUrlIndex));
                }
            }
            cursor.close();
        }
        return story; // Return null if no next story is found
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STORIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            // You can implement a more sophisticated upgrade strategy here
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
