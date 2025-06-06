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

public class StoryAdapterAchievement extends RecyclerView.Adapter<StoryAdapterAchievement.StoryViewHolder> {
    private List<StoryAchievementModel> storyList;
    private Context context;

    public StoryAdapterAchievement(Context context, List<StoryAchievementModel> storyList) {
        this.context = context;
        this.storyList = storyList;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_achievement_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        StoryAchievementModel story = storyList.get(position);
        holder.titleTextView.setText(story.getTitle());

        // Update lock and star reward icons based on the isCompleted flag
        if ("completed".equalsIgnoreCase(story.getIsCompleted())) {
            holder.unlockAchievementsImageView.setVisibility(View.GONE); // Hide lock icon
            holder.starRewardAchievementsImageView.setVisibility(View.VISIBLE); // Show star reward icon
        } else {
            holder.unlockAchievementsImageView.setVisibility(View.VISIBLE); // Show lock icon
            holder.starRewardAchievementsImageView.setVisibility(View.GONE); // Hide star reward icon
        }
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    // ViewHolder for holding the UI components
    static class StoryViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView unlockAchievementsImageView;  // Lock icon
        ImageView starRewardAchievementsImageView; // Star reward icon

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titlep); // Replace with your actual TextView ID
            unlockAchievementsImageView = itemView.findViewById(R.id.unlockachivements);  // This is your lock icon
            starRewardAchievementsImageView = itemView.findViewById(R.id.starrewardachivements); // This is your star reward icon
        }
    }
}
