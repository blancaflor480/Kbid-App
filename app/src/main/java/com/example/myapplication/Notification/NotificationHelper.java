package com.example.myapplication.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "memory_verse_channel";
    private static final String CHANNEL_NAME = "Memory Verse Notifications";

    public static void sendNotification(Context context, String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH); // Use IMPORTANCE_HIGH
            channel.setDescription("Notifications for Memory Verses");
            notificationManager.createNotificationChannel(channel);
        }

        // Create notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_notification_overlay) // Replace with custom icon
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Ensure visibility
                .setAutoCancel(true) // Dismiss on click
                .build();

        // Show notification with a unique ID
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }
}
