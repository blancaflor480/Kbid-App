package com.example.myapplication.database;

import androidx.room.TypeConverter;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class convertersDate {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy: HH:mm", Locale.getDefault());

    // Convert Timestamp to Long (timestamp in milliseconds)
    @TypeConverter
    public static Long fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        } else {
            return timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000;
        }
    }

    // Convert Long (timestamp) to Timestamp
    @TypeConverter
    public static Timestamp toTimestamp(Long value) {
        if (value == null) {
            return null;
        } else {
            return new Timestamp(new Date(value));
        }
    }

    // Convert Long (timestamp) to formatted String
    @TypeConverter
    public static String fromLongToString(Long value) {
        if (value == null) {
            return null;
        } else {
            Date date = new Date(value);
            return formatter.format(date);
        }
    }

    // Convert formatted String back to Long (timestamp)
    @TypeConverter
    public static Long fromStringToLong(String value) {
        try {
            if (value == null) {
                return null;
            } else {
                Date date = formatter.parse(value);
                return date != null ? date.getTime() : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
