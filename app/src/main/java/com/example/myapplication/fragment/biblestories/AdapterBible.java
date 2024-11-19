package com.example.myapplication.fragment.biblestories;

import android.content.Context;
import android.content.Intent;
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
import com.example.myapplication.fragment.biblestories.playlist.BiblePlay;

import java.util.List;

public class AdapterBible extends RecyclerView.Adapter<AdapterBible.MyHolder> {

    private Context context;
    private List<ModelBible> bibleVerseList;

    public AdapterBible(Context context, List<ModelBible> bibleVerseList) {
        this.context = context;
        this.bibleVerseList = bibleVerseList;
    }

    public void updateStories(List<ModelBible> newStories) {
        this.bibleVerseList = newStories;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_bibleverse, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelBible bibleVerse = bibleVerseList.get(position);

        // Debug: Log the ID and isCompleted status
        String isCompleted = bibleVerse.getIsCompleted();
        Log.d("BibleFragment", "Binding story ID: " + bibleVerse.getId() + ", isCompleted: " + isCompleted);

        // Always load the image into profiletv
        String imageUrl = bibleVerse.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d("BibleFragment", "Loading image for story ID: " + bibleVerse.getId() + ", URL: " + imageUrl);

            // Clear any previous Glide load
            Glide.with(context).clear(holder.profiletv);

            // Load the image asynchronously using Glide
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.image) // Default placeholder while loading
                    .error(R.drawable.image)       // Fallback error image
                    .into(holder.profiletv);
        } else {
            Log.d("BibleFragment", "No image URL for story ID: " + bibleVerse.getId());
            holder.profiletv.setImageResource(R.drawable.image); // Set default image if URL is empty
        }

        // Handle lock state
        if ("locked".equalsIgnoreCase(isCompleted)) {
            holder.lockImage.setVisibility(View.VISIBLE); // Show lock image
            holder.profiletv.setAlpha(0.1f); // Make profiletv semi-transparent
            Log.d("BibleFragment", "Story is locked. Lock image displayed and profiletv transparency set for story ID: " + bibleVerse.getId());
        } else {
            holder.lockImage.setVisibility(View.GONE); // Hide lock image
            holder.profiletv.setAlpha(1.0f); // Make profiletv fully visible
            Log.d("BibleFragment", "Story is unlocked. Lock image hidden and profiletv fully visible for story ID: " + bibleVerse.getId());
        }

        // Set Title and Verse
        holder.title.setText(bibleVerse.getTitle());
        holder.verse.setText(bibleVerse.getVerse());

        // Item Click Listener
        holder.itemView.setOnClickListener(v -> {
            Log.d("BibleFragment", "Story ID " + bibleVerse.getId() + " clicked.");
            Intent intent = new Intent(context, BiblePlay.class);
            intent.putExtra("id", bibleVerse.getId());
            intent.putExtra("title", bibleVerse.getTitle());
            intent.putExtra("verse", bibleVerse.getVerse());
            intent.putExtra("description", bibleVerse.getDescription());
            intent.putExtra("imageUrl", bibleVerse.getImageUrl());
            intent.putExtra("timestamp", bibleVerse.getTimestamp());
            intent.putExtra("audioUrl", bibleVerse.getAudioUrl());
            context.startActivity(intent);
        });
    }







    @Override
    public int getItemCount() {
        return bibleVerseList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView title, verse;
        ImageView profiletv, lockImage;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv = itemView.findViewById(R.id.thumbnailp);
            title = itemView.findViewById(R.id.titlep);
            verse = itemView.findViewById(R.id.versep);
            lockImage = itemView.findViewById(R.id.lock);
        }
    }
}
