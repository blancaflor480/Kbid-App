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

    // Constructor to initialize context and list
    public AdapterBible(Context context, List<ModelBible> bibleVerseList) {
        this.context = context;
        this.bibleVerseList = bibleVerseList;
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

        // Set the title text
        holder.title.setText(bibleVerse.getTitle());
        holder.verse.setText(bibleVerse.getVerse());

        // Get the image URL
        String imageUrl = bibleVerse.getImageUrl();

        // Load image using Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.image) // Default image while loading
                    .error(R.drawable.image) // Default image in case of error
                    .into(holder.profiletv);
        } else {
            // Set a default image if imageUrl is empty or null
            holder.profiletv.setImageResource(R.drawable.image);
        }

        // Set an OnClickListener on the CardView or the itemView itself
        holder.itemView.setOnClickListener(v -> {
            // Create an Intent to navigate to BiblePlayActivity
            Intent intent = new Intent(context, BiblePlay.class);

            // Pass the Bible story data to the activity
            intent.putExtra("id", bibleVerse.getId());
            intent.putExtra("title", bibleVerse.getTitle());
            intent.putExtra("verse", bibleVerse.getVerse());
            intent.putExtra("description", bibleVerse.getDescription());
            intent.putExtra("imageUrl", bibleVerse.getImageUrl());
            intent.putExtra("timestamp", bibleVerse.getTimestamp());
            intent.putExtra("audioUrl", bibleVerse.getAudioUrl());

            // Start the activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bibleVerseList.size();
    }

    // ViewHolder class to hold the view references
    class MyHolder extends RecyclerView.ViewHolder {

        TextView title, verse;
        ImageView profiletv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv = itemView.findViewById(R.id.thumbnailp);
            title = itemView.findViewById(R.id.titlep);
            verse = itemView.findViewById(R.id.versep);
        }
    }
}
