package com.example.myapplication.fragment.achievement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class GameAdapterAchievement extends RecyclerView.Adapter<GameAdapterAchievement.GameViewHolder>{
    private List<GameAchievementModel> gameList;
    private Context context;

    public GameAdapterAchievement(Context context, List<GameAchievementModel> gameList) {
        this.context = context;
        this.gameList = gameList;
    }

    @NonNull
    @Override
    public GameAdapterAchievement.GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_achievement_games, parent, false);
        return new GameAdapterAchievement.GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameAdapterAchievement.GameViewHolder holder, int position) {
        GameAchievementModel game = gameList.get(position);
        holder.titleTextView.setText("Level: " +game.getLevel());

        // Update lock and star reward icons based on the isCompleted flag
        if ("completed".equalsIgnoreCase(game.getIsCompleted())) {
            holder.unlockAchievementsImageView.setVisibility(View.GONE); // Hide lock icon
            holder.starRewardAchievementsImageView.setVisibility(View.VISIBLE); // Show star reward icon
        } else {
            holder.unlockAchievementsImageView.setVisibility(View.VISIBLE); // Show lock icon
            holder.starRewardAchievementsImageView.setVisibility(View.GONE); // Hide star reward icon
        }
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    // ViewHolder for holding the UI components
    static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView unlockAchievementsImageView;  // Lock icon
        ImageView starRewardAchievementsImageView; // Star reward icon

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titlep); // Replace with your actual TextView ID
            unlockAchievementsImageView = itemView.findViewById(R.id.unlockachivements);  // This is your lock icon
            starRewardAchievementsImageView = itemView.findViewById(R.id.starrewardachivements); // This is your star reward icon
        }
    }

}
