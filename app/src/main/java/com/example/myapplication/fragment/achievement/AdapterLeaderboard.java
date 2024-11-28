package com.example.myapplication.fragment.achievement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class AdapterLeaderboard extends RecyclerView.Adapter<AdapterLeaderboard.ViewHolder> {

    private Context context;
    private List<leaderboardmodel> leaderboardList;

    public AdapterLeaderboard(Context context, List<leaderboardmodel> leaderboardList) {
        this.context = context;
        this.leaderboardList = leaderboardList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_rank, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        leaderboardmodel leaderboardModel = leaderboardList.get(position);

        // Set rank
        holder.rank.setText("#" + (position + 1));
        // Set user name (childName)
        holder.name.setText(leaderboardModel.getUserName());
        // Set total achievements (total count)
        holder.points.setText("Total Achievements: " + leaderboardModel.getTotalPoints()); // Display total points

        // Set user profile image (if any)
        Glide.with(context)
                .load(leaderboardModel.getImageUrl())
                .placeholder(R.drawable.lion) // Default placeholder
                .into(holder.profileImage);
    }


    @Override
    public int getItemCount() {
        return leaderboardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rank, name, points;
        CircleImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.rank);
            name = itemView.findViewById(R.id.namep);
            points = itemView.findViewById(R.id.points);
            profileImage = itemView.findViewById(R.id.imagenon);
        }
    }
}

