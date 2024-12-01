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

public class DevotionalAchievementAdapter extends RecyclerView.Adapter<DevotionalAchievementAdapter.DevotionalViewHolder>{

    private List<DevotionalAchievementModel> devotionalList;
    private Context context;

    public DevotionalAchievementAdapter(Context context, List<DevotionalAchievementModel> devotionalList) {
        this.context = context;
        this.devotionalList = devotionalList;
    }

    @NonNull
    @Override
    public DevotionalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_achievement_story, parent, false);
        return new DevotionalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DevotionalViewHolder holder, int position) {
        DevotionalAchievementModel achievement = devotionalList.get(position);
        holder.thumbnailp.setImageResource(achievement.getBadge());
    }

    @Override
    public int getItemCount() {
        return devotionalList.size();
    }

    static class DevotionalViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailp;

        DevotionalViewHolder(View itemView) {
            super(itemView);
            thumbnailp = itemView.findViewById(R.id.thumbnailp);
        }
    }
}