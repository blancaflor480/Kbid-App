package com.example.myapplication.fragment.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<ModelNotification> notifications; // Accepts List<ModelNotification>

    public NotificationAdapter(List<ModelNotification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_notification, parent, false); // Adjust to your row layout
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelNotification notification = notifications.get(position);
        holder.announcementTextView.setText(notification.getAnnouncement());
        holder.dateTextView.setText(notification.getDate());
        // Handle image loading if necessary
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView announcementTextView;
        TextView dateTextView;
        ImageView thumbnailImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            announcementTextView = itemView.findViewById(R.id.announcement);
            dateTextView = itemView.findViewById(R.id.date);
            thumbnailImageView = itemView.findViewById(R.id.thumbnailp);
        }
    }
}
