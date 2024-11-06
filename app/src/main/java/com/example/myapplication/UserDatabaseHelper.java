package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "kbid-app.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "user";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "uid TEXT,"
                + "childName TEXT,"
                + "childBirthday TEXT,"
                + "avatarName TEXT,"
                + "avatarResourceId INTEGER,"
                + "avatarImage BLOB,"
                + "email TEXT)";
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addUser(String uid, String childName, String childBirthday, String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("uid", uid);
        values.put("childName", childName);
        values.put("childBirthday", childBirthday);
        values.put("email", email);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }
}
