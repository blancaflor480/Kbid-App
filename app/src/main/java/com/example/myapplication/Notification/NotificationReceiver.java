package com.example.myapplication.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Send notification when alarm triggers
        NotificationHelper.sendNotification(context, "Memory Verse", "It's time to reflect on today's memory verse!");
    }
}
