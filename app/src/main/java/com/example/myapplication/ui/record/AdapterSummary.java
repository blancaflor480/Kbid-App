package com.example.myapplication.ui.record;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterSummary extends RecyclerView.Adapter<AdapterSummary.SummaryMyHolder>{

    private final Context context;
    private final List<RecordModel> list;

    public AdapterSummary(Context context, List<RecordModel> list) {
        this.context = context;
        this.list = list;
    }
    @NonNull
    @Override
    public AdapterSummary.SummaryMyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_summaryreport, parent, false);
        return new AdapterSummary.SummaryMyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryMyHolder holder, int position) {
        RecordModel record = list.get(position);

        holder.emailTextView.setText(record.getEmail());
        holder.rank.setText("Rank# : " + record.getRank());
        holder.totalachievement.setText("Total Achievements: " + record.getTotalachievements());

        if (record.getImageUrl() != null && !record.getImageUrl().isEmpty()) {
            int drawableResourceId = context.getResources().getIdentifier(
                    record.getImageUrl(),
                    "drawable",
                    context.getPackageName()
            );

            if (drawableResourceId != 0) {
                Glide.with(context)
                        .load(drawableResourceId)
                        .placeholder(R.drawable.userkids)
                        .into(holder.profileImageView);
            } else {
                // Fallback to default image if drawable not found
                Glide.with(context)
                        .load(R.drawable.userkids)
                        .into(holder.profileImageView);
            }
        }
        holder.itemView.setOnClickListener(v -> showUserDetailsDialog(record));
    }
    private void showUserDetailsDialog(RecordModel record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_userreport_details, null);

        // Initialize dialog views
        TextView rankText = dialogView.findViewById(R.id.rank);
        CircleImageView thumbnail = dialogView.findViewById(R.id.thumbnail);
        TextView fullNameText = dialogView.findViewById(R.id.Fullname);
        TextView emailText = dialogView.findViewById(R.id.email);
        TextView storyCountText = dialogView.findViewById(R.id.storycount);
        TextView gameCountText = dialogView.findViewById(R.id.gamecount);
        TextView reflectionCountText = dialogView.findViewById(R.id.reflectioncount);
        TextView totalAchievementText = dialogView.findViewById(R.id.totalachievement);

        // Set values
        rankText.setText("RANK# " + record.getRank());
        fullNameText.setText("Fullname: " + record.getName());
        emailText.setText("Email: " + record.getEmail());
        storyCountText.setText("Story Achievements: " + record.getStoryId());
        gameCountText.setText("Game Achievements: " + record.getGameId());
        reflectionCountText.setText("Reflection Achievements: " + record.getKidsreflectionId());
        totalAchievementText.setText("Total Achievements: " +
                (Integer.parseInt(record.getStoryId()) +
                        Integer.parseInt(record.getGameId()) +
                        Integer.parseInt(record.getKidsreflectionId())));

        // Set profile image
        if (record.getImageUrl() != null && !record.getImageUrl().isEmpty()) {
            int drawableResourceId = context.getResources().getIdentifier(
                    record.getImageUrl(),
                    "drawable",
                    context.getPackageName()
            );
            if (drawableResourceId != 0) {
                Glide.with(context)
                        .load(drawableResourceId)
                        .placeholder(R.drawable.userkids)
                        .into(thumbnail);
            }
        }

        builder.setView(dialogView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public void filterList(List<RecordModel> filteredList) {
        // Filter logic if needed
    }

    static class SummaryMyHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImageView;
        TextView emailTextView, rank, totalachievement;


        public SummaryMyHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.imagenon); // Profile image view
            emailTextView = itemView.findViewById(R.id.namep); // Email text view
            rank = itemView.findViewById(R.id.rank);
            totalachievement = itemView.findViewById(R.id.totalachievement);
            // Options menu button
        }
    }
}
