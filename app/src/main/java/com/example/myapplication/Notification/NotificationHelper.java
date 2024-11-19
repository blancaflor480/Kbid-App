package com.example.myapplication.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "devotional_channel";
    private static final String CHANNEL_NAME = "Devotional Notifications";
    private static final int NOTIFICATION_ID = 1; // Unique ID for notifications

    // Method to show a notification
    public static void showDevotionalNotification(Context context, String title, String message) {
        // Create a NotificationManager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // For Android O and above, we need to create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notif) // Customize the icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
