package com.example.myapplication.fragment.biblestories;

import android.content.Context;
import android.content.Intent;
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

    /**
     * Update the list of stories and refresh the RecyclerView.
     */
    public void updateStories(List<ModelBible> newStories) {
        this.bibleVerseList.clear(); // Clear the existing list
        this.bibleVerseList.addAll(newStories); // Add new data
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

        String isCompleted = bibleVerse.getIsCompleted();


        // Set visibility and transparency based on the lock state
        if ("locked".equalsIgnoreCase(isCompleted)) {
            holder.lockImage.setVisibility(View.VISIBLE); // Show lock image
            holder.profiletv.setAlpha(0.3f); // Dim the image
            holder.itemView.setOnClickListener(null); // Disable clicks on locked items
        } else {
            holder.lockImage.setVisibility(View.GONE); // Hide lock image
            holder.profiletv.setAlpha(1.0f); // Fully visible
            holder.itemView.setOnClickListener(v -> {
                // Start the BiblePlay activity with the selected item data
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

        // Set Title and Verse
        holder.title.setText(bibleVerse.getTitle());
        holder.verse.setText(bibleVerse.getVerse());

        // Load the image using Glide
        Glide.with(context)
                .load(bibleVerse.getImageUrl())
                .placeholder(R.drawable.image)
                .error(R.drawable.image)
                .into(holder.profiletv);
    }

    /**
     * Method to update the lock state and refresh the adapter.
     * Call this method when a story is unlocked.
     */
    public void updateLockState(int position, String newStatus) {
        bibleVerseList.get(position).setIsCompleted(newStatus); // Update the status
        notifyItemChanged(position); // Refresh the specific item
    }


    @Override
    public int getItemCount() {
        return bibleVerseList.size();
    }

    /**
     * ViewHolder class for RecyclerView items.
     */
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
