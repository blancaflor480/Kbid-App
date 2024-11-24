package com.example.myapplication.fragment.useraccount;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {
    private final Context context;
    private final List<Badge> badges;

    public BadgeAdapter(Context context, List<Badge> badges) {
        this.context = context;
        this.badges = badges;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = badges.get(position);

        // Set the badge thumbnail
        holder.thumbnailp.setImageResource(badge.getBadgeImage());

        // Show/hide indicators based on the badge's properties
        //holder.unlockachivements.setVisibility(badge.isUnlocked() ? View.VISIBLE : View.GONE);
    }


    @Override
    public int getItemCount() {
        return badges.size();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailp;
       // ImageView unlockachivements;

        BadgeViewHolder(View itemView) {
            super(itemView);
            thumbnailp = itemView.findViewById(R.id.thumbnailp);
            //unlockachivements = itemView.findViewById(R.id.unlockachivements);

        }
    }
}

// Badge.java - Model class for badges


// Implementation in your account.java
