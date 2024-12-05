package com.example.myapplication.fragment.achievement;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        // Ensure ranks are displayed continuously
        // Assign rank explicitly for each position

        holder.rank.setText("#" + leaderboardModel.getRank());
        holder.rank.setVisibility(View.VISIBLE);

        // Set user name
        holder.name.setText(leaderboardModel.getUserName() != null ? leaderboardModel.getUserName() : "Unknown User");

        // Set points
        holder.points.setText("Points: " + leaderboardModel.getTotalPoints());

        // Handle border image loading
        if (leaderboardModel.getBorderImage() != null && !leaderboardModel.getBorderImage().isEmpty()) {
            try {
                int drawableResourceId = context.getResources().getIdentifier(
                        leaderboardModel.getBorderImage(), "drawable", context.getPackageName()
                );

                if (drawableResourceId != 0) {
                    Glide.with(context)
                            .load(drawableResourceId)
                            .placeholder(R.drawable.bronze)
                            .into(holder.border);
                } else {
                    holder.border.setImageResource(R.drawable.bronze);
                }
            } catch (Exception e) {
                holder.border.setImageResource(R.drawable.bronze);
            }
        } else {
            holder.border.setImageResource(R.drawable.bronze);
        }

        // Handle profile image loading
        if (leaderboardModel.getImageUrl() != null && !leaderboardModel.getImageUrl().isEmpty()) {
            try {
                int drawableResourceId = context.getResources().getIdentifier(
                        leaderboardModel.getImageUrl(), "drawable", context.getPackageName()
                );

                if (drawableResourceId != 0) {
                    Glide.with(context)
                            .load(drawableResourceId)
                            .placeholder(R.drawable.lion)
                            .into(holder.profileImage);
                } else {
                    Glide.with(context)
                            .load(leaderboardModel.getImageUrl())
                            .placeholder(R.drawable.lion)
                            .into(holder.profileImage);
                }
            } catch (Exception e) {
                holder.profileImage.setImageResource(R.drawable.lion);
            }
        } else {
            holder.profileImage.setImageResource(R.drawable.lion);
        }
    }


    @Override
    public int getItemCount() {
        return leaderboardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rank, name, points;
        CircleImageView profileImage;
        ImageView border;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.rank);
            name = itemView.findViewById(R.id.namep);
            points = itemView.findViewById(R.id.points);
            border = itemView.findViewById(R.id.border);
            profileImage = itemView.findViewById(R.id.imagenon);
        }
    }
}

