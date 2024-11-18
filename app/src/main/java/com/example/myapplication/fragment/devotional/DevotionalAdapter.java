package com.example.myapplication.fragment.devotional;

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

public class DevotionalAdapter extends RecyclerView.Adapter<DevotionalAdapter.DevotionalViewHolder> {

    private Context context;
    private List<DevotionalModel> devotionalList;

    public DevotionalAdapter(Context context, List<DevotionalModel> devotionalList) {
        this.context = context;
        this.devotionalList = devotionalList;
    }

    @NonNull
    @Override
    public DevotionalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.kidsdevotional, parent, false);
        return new DevotionalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DevotionalViewHolder holder, int position) {
        DevotionalModel devotional = devotionalList.get(position);
        holder.memoryverse.setText(devotional.getMemoryverse());
        holder.verse.setText(devotional.getVerse());

        // Load the thumbnail using Glide (or any image loading library)
        if (devotional.getImageUrl() != null && !devotional.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(devotional.getImageUrl())
                    .placeholder(R.drawable.image)  // Placeholder while loading
                    .into(holder.thumbnail);
        } else {
            holder.thumbnail.setImageResource(R.drawable.image);
        }
    }

    @Override
    public int getItemCount() {
        return devotionalList.size();
    }

    public static class DevotionalViewHolder extends RecyclerView.ViewHolder {
        TextView memoryverse, verse;
        ImageView thumbnail;

        public DevotionalViewHolder(@NonNull View itemView) {
            super(itemView);
            memoryverse = itemView.findViewById(R.id.memoryverse);
            verse = itemView.findViewById(R.id.verse);
            thumbnail = itemView.findViewById(R.id.thumbnail);
        }
    }
}
