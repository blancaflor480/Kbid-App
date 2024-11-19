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
import com.example.myapplication.fragment.achievement.StoryAchievementModel;
import java.util.List;

public class StoryAdapterAchievement extends RecyclerView.Adapter<StoryAdapterAchievement.StoryViewHolder>{
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

        // Update lock icon visibility based on isUnlocked flag
        if (story.getUnlock()) {
            holder.lockImageView.setVisibility(View.GONE);  // Hide lock icon when unlocked
        } else {
            holder.lockImageView.setVisibility(View.VISIBLE); // Show lock icon when not unlocked
        }
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }
    static class StoryViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView lockImageView;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titlep); // Replace with your actual TextView ID
            lockImageView = itemView.findViewById(R.id.unlockachivements);  // This is your lock icon
        }
    }
}
