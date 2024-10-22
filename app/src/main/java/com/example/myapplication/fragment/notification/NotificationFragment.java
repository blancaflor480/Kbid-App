package com.example.myapplication.fragment.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<ModelNotification> notifications; // Change to List<ModelNotification>

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.notifcation, container, false); // Corrected "notifcation" to "notification"

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclep);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get the list of notifications
        notifications = getNotificationList(); // Populate the notifications list

        // Set the adapter
        adapter = new NotificationAdapter(notifications); // Pass List<ModelNotification> to adapter
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<ModelNotification> getNotificationList() {
        List<ModelNotification> notifications = new ArrayList<>();

        // Add your notifications here
        notifications.add(new ModelNotification("Notification 1: New update available!", "2024-10-22", null));
        notifications.add(new ModelNotification("Notification 2: Your profile has been updated.", "2024-10-21", null));
        notifications.add(new ModelNotification("Notification 3: You have a new message.", "2024-10-20", null));

        return notifications; // Return the list of notifications
    }
}
