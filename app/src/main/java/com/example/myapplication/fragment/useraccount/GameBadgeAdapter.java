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

public class GameBadgeAdapter extends RecyclerView.Adapter<GameBadgeAdapter.GameBadgeViewHolder> {
    private final Context context;
    private final List<Badge> badges;

    public GameBadgeAdapter(Context context, List<Badge> badges) {
        this.context = context;
        this.badges = badges;
    }

    @NonNull
    @Override
    public GameBadgeAdapter.GameBadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_badge, parent, false);
        return new GameBadgeAdapter.GameBadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameBadgeAdapter.GameBadgeViewHolder holder, int position) {
        Badge badge = badges.get(position);

        // Set the badge thumbnail
        holder.thumbnailp.setImageResource(badge.getBadgeImage());
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    static class GameBadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailp;
        // ImageView unlockachivements;

        GameBadgeViewHolder(View itemView) {
            super(itemView);
            thumbnailp = itemView.findViewById(R.id.thumbnailp);
            //unlockachivements = itemView.findViewById(R.id.unlockachivements);
        }
    }
}