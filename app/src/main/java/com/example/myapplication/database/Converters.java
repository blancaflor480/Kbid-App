package com.example.myapplication.database;

import androidx.room.TypeConverter;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Converters {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy: HH:mm", Locale.getDefault());

    @TypeConverter
    public static Timestamp fromTimestamp(Long value) {
        if (value == null) {
            return null;
        } else {
            Date date = new Date(value);
            return new Timestamp(date);
        }
    }

    @TypeConverter
    public static Long dateToTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        } else {
            Date date = timestamp.toDate();
            return date.getTime();
        }
    }

    // Convert from Date to formatted string
    @TypeConverter
    public static String fromTimestampToString(Long value) {
        if (value == null) {
            return null;
        } else {
            Date date = new Date(value);
            return formatter.format(date);
        }
    }

    // Convert from formatted string back to Date
    @TypeConverter
    public static Long fromStringToTimestamp(String value) {
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
