package com.example.myapplication.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "DevotionalNotification";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Notification receiver triggered at: " + new Date());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        long startOfDay = getTodayStartTimestamp();
        long endOfDay = getTodayEndTimestamp();

        Log.d(TAG, "Searching for devotional between timestamps: "
                + startOfDay + " - " + endOfDay);

        db.collection("devotional")
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .whereLessThanOrEqualTo("timestamp", endOfDay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Query successful. Documents found: "
                            + queryDocumentSnapshots.size());

                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot devotionalDoc = queryDocumentSnapshots.getDocuments().get(0);

                        String memoryVerse = devotionalDoc.getString("memoryverse");
                        String verse = devotionalDoc.getString("verse");
                        String title = devotionalDoc.getString("title");

                        Log.d(TAG, "Devotional Details:");
                        Log.d(TAG, "Title: " + title);
                        Log.d(TAG, "Memory Verse: " + memoryVerse);
                        Log.d(TAG, "Verse: " + verse);

                        NotificationHelper.sendNotification(context, title, memoryVerse + "\n" + verse);
                    } else {
                        Log.d(TAG, "No devotional found for today");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching devotional", e);
                });
    }

    private long getTodayStartTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getTodayEndTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
}