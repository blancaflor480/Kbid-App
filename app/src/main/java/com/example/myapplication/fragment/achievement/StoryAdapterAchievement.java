package com.example.myapplication.fragment.achievement;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.fragment.achievement.StoryAchievementModel;
import java.util.List;

public class StoryAdapterAchievement extends RecyclerView.Adapter<StoryAdapterAchievement.StoryViewHolder>{
    private List<StoryAchievementModel> storyList;
    public StoryAdapterAchievement(List<StoryAchievementModel> storyList) {
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
    }
    @Override
    public int getItemCount() {
        return storyList.size();
    }
    static class StoryViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titlep); // Replace with your actual TextView ID
        }
    }
}
