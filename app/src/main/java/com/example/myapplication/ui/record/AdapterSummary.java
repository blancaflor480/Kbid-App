package com.example.myapplication.ui.record;

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
            Glide.with(context)
                    .load(record.getImageUrl())
                    .placeholder(R.drawable.userkids)
                    .into(holder.profileImageView);
        }
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
        ImageView optionsMenu;

        public SummaryMyHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.imagenon); // Profile image view
            emailTextView = itemView.findViewById(R.id.namep); // Email text view
            rank = itemView.findViewById(R.id.rank);
            totalachievement = itemView.findViewById(R.id.totalachievement);
            optionsMenu = itemView.findViewById(R.id.optionsMenu); // Options menu button
        }
    }
}
