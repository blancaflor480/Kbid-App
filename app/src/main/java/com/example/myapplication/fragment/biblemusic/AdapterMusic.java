package com.example.myapplication.fragment.biblemusic;

import android.app.MediaRouteButton;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.fragment.biblestories.playlist.BiblePlay;

import java.util.List;

public class AdapterMusic extends RecyclerView.Adapter<AdapterMusic.MyHolder> {
    private final Context context;
    private final List<ModelMusic> bibleMusicList;

    // Constructor to initialize context and list
    public AdapterMusic(Context context, List<ModelMusic> bibleMusicList) {
        this.context = context;
        this.bibleMusicList = bibleMusicList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_gridmusic, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelMusic bibleMusic = bibleMusicList.get(position);

        // Set the title text
        holder.title.setText(bibleMusic.getTitle());
        // Load image using Glide
        Glide.with(context)
                .load(bibleMusic.getImageUrl())
                .placeholder(R.drawable.image)
                .error(R.drawable.image)
                .into(holder.profileImage);

        holder.playButton.setOnClickListener(v -> {
            String videoUrl = bibleMusicList.get(position).getVideoUrl();
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("videoUrl", videoUrl);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Video URL not available", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return bibleMusicList.size();
    }

    // ViewHolder class to hold the view references
    static class MyHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView profileImage;
        ImageButton playButton;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.thumbnailp);
            title = itemView.findViewById(R.id.titlep);
            playButton = itemView.findViewById(R.id.videoView);
        }
    }
}
