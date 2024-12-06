package com.example.myapplication.fragment.useraccount;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class DevotionalAchievementAdapter extends RecyclerView.Adapter<DevotionalAchievementAdapter.DevotionalViewHolder> {
    private List<Badge> badges;
    private Context context;

    public DevotionalAchievementAdapter(Context context, List<Badge> badges) {
        this.context = context;
        this.badges = badges;
    }

    @NonNull
    @Override
    public DevotionalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_badge, parent, false);
        return new DevotionalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DevotionalViewHolder holder, int position) {
        Badge badge = badges.get(position);
        holder.thumbnailp.setImageResource(badge.getBadgeImage());

        // Optional: Set alpha for locked badges
        holder.thumbnailp.setAlpha(badge.isUnlocked() ? 1.0f : 0.5f);
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    static class DevotionalViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailp;

        DevotionalViewHolder(View itemView) {
            super(itemView);
            thumbnailp = itemView.findViewById(R.id.thumbnailp);
        }
    }
}