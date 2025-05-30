package com.example.myapplication.fragment.biblegames;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterGames extends RecyclerView.Adapter<AdapterGames.MyHolder> {

    private List<ModelGames> bibleVerseList;

    public AdapterGames(List<ModelGames> bibleVerseList) {
        this.bibleVerseList = bibleVerseList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_gamesv1, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelGames bibleVerse = bibleVerseList.get(position);
        holder.name.setText(bibleVerse.getVerseName());

        // Log binding details
        Log.d("AdapterBible", "Binding item at position " + position + ": " + bibleVerse.getVerseName());

        // Optionally set other data, e.g., image, if available
        // Example: holder.profiletv.setImageResource(R.drawable.some_image);
    }

    @Override
    public int getItemCount() {
        return bibleVerseList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView profiletv;
        TextView name;
        ImageView play;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv = itemView.findViewById(R.id.thumbnailp);
            name = itemView.findViewById(R.id.titlep);

        }
    }
}
