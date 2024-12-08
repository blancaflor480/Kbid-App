package com.example.myapplication.Notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DevotionalNotificationScheduler {
    private static final String TAG = "DevotionalScheduler";
    private static final int NOTIFICATION_REQUEST_CODE = 123;

    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    public static Intent getExactAlarmSettingsIntent(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        }
        return null;
    }

    public static void scheduleNotificationCheck(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, NotificationReceiver.class);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICATION_REQUEST_CODE,
                    intent,
                    flags
            );

            // Set time for 6 AM
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 6);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // If it's already past 6 AM, schedule for next day
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Schedule repeating alarm at 6 AM daily
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            // Log the next scheduled time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Log.d(TAG, "Next notification scheduled for: " + sdf.format(calendar.getTime()));

        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification", e);
        }
    }

    public static void checkDevotionalForToday(Context context) {
        Log.d(TAG, "Checking devotional for today...");

        // Get current time
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 6);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // End of today
        Calendar endOfToday = (Calendar) today.clone();
        endOfToday.add(Calendar.DAY_OF_MONTH, 1);
        endOfToday.add(Calendar.MILLISECOND, -1);

        Log.d(TAG, "Searching for devotional between dates:");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Log.d(TAG, "Start: " + sdf.format(today.getTime()));
        Log.d(TAG, "End: " + sdf.format(endOfToday.getTime()));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("devotional")
                .whereGreaterThanOrEqualTo("timestamp", today.getTime())
                .whereLessThanOrEqualTo("timestamp", endOfToday.getTime())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Total documents found: " + queryDocumentSnapshots.size());

                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                        String memoryVerse = doc.getString("memoryverse");
                        String verse = doc.getString("verse");
                        String title = doc.getString("title");

                        Log.d(TAG, "Found today's devotional:");
                        Log.d(TAG, "Title: " + title);
                        Log.d(TAG, "Memory Verse: " + memoryVerse);
                        Log.d(TAG, "Verse: " + verse);

                        NotificationHelper.sendNotification(
                                context,
                                title,
                                memoryVerse + "\n" + verse
                        );
                    } else {
                        Log.d(TAG, "No devotional found for today");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking devotional", e);
                });
    }

}