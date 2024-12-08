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

        // Get today's date range as Date objects
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 6);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar endOfToday = (Calendar) today.clone();
        endOfToday.add(Calendar.DAY_OF_MONTH, 1);
        endOfToday.add(Calendar.MILLISECOND, -1);

        // Log the date range for debugging
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Log.d(TAG, "Searching for devotional between:");
        Log.d(TAG, "Start: " + sdf.format(today.getTime()));
        Log.d(TAG, "End: " + sdf.format(endOfToday.getTime()));

        db.collection("devotional")
                .whereGreaterThanOrEqualTo("timestamp", today.getTime())
                .whereLessThanOrEqualTo("timestamp", endOfToday.getTime())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Query successful. Documents found: "
                            + queryDocumentSnapshots.size());

                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot devotionalDoc = queryDocumentSnapshots.getDocuments().get(0);

                        String memoryVerse = devotionalDoc.getString("memoryverse");
                        String verse = devotionalDoc.getString("verse");
                        String title = devotionalDoc.getString("title");

                        // Log timestamp for debugging
                        Date timestamp = devotionalDoc.getDate("timestamp");
                        if (timestamp != null) {
                            Log.d(TAG, "Devotional Timestamp: " + sdf.format(timestamp));
                        }

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
}