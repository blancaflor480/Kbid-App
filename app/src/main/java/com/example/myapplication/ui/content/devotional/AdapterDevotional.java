package com.example.myapplication.ui.content.devotional;

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
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class AdapterDevotional extends RecyclerView.Adapter<AdapterDevotional.MyHolder> {
    private final Context context;
    private final List<ModelDevotional> list;
    private final FirebaseAuth firebaseAuth;

    public AdapterDevotional(Context context, List<ModelDevotional> list) {
        this.context = context;
        this.list = list;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public AdapterDevotional.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_devotional, parent, false);
        return new AdapterDevotional.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterDevotional.MyHolder holder, int position) {
        ModelDevotional content = list.get(position);
        String imageUrl = content.getImageUrl();
        String title = content.getTitle();
        Date timestamp = content.getTimestamp();

        holder.title.setText(title);
        holder.timestamp.setText(timestamp != null ?
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp) : "N/A");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.image);
        }

        holder.optionsMenu.setOnClickListener(v -> showPopupMenu(holder.optionsMenu, position, content.getId()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void filterList(List<ModelDevotional> filteredList) {
        list.clear();
        list.addAll(filteredList);
        notifyDataSetChanged();
    }



    private void showPopupMenu(View view, int position, String contentId) {
        // Your logic to show popup menu options for the item
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView title, timestamp;
        ImageView optionsMenu;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.imagenon);
            title = itemView.findViewById(R.id.titlep);
            timestamp = itemView.findViewById(R.id.datetimep);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);
        }
    }
}
