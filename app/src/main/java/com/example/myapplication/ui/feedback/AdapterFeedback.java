package com.example.myapplication.ui.feedback;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.ui.content.devotional.ModelReflection;
import com.example.myapplication.ui.content.song.AdapterSong;
import com.example.myapplication.ui.content.song.ModelSong;
import com.example.myapplication.ui.content.song.SongFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class AdapterFeedback extends RecyclerView.Adapter<AdapterFeedback.MyHolder>{
    private Context context;
    private List<FeedbackViewModel> list;
    private FeedbackFragment feedbackFragment;

    public AdapterFeedback(Context context, List<FeedbackViewModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AdapterFeedback.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_feedback, parent, false);
        return new AdapterFeedback.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterFeedback.MyHolder holder, final int position) {
        final FeedbackViewModel content = list.get(position);
        String email = content.getEmail();
        int rating = content.getRating();
        Date timestamp = content.getTimestamp();

        holder.namep.setText(email);
        // Safely set the rating
        try {
            holder.rate.setText(String.valueOf(rating));
        } catch (Exception e) {
            holder.rate.setText("N/A"); // Default value in case of an error
        } // Convert int to String
        holder.timestamp.setText(timestamp != null ?
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp) : "N/A");

        holder.optionsMenu.setOnClickListener(v -> showPopupMenu(holder.optionsMenu, position, email));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void showPopupMenu(View view, int position, String songId) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.option_view_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            FeedbackViewModel content = list.get(position);
            int itemId = item.getItemId();

            if (itemId == R.id.Viewprof) {
                // Add view profile functionality
                showViewDialog(content, position);
                return true;
            } else if (itemId == R.id.Editprof) {
               // showEditDialog(content, position);
               // return true;
            } else if (itemId == R.id.Deleteprof) {
               // deleteSong(content.getId(), position);
               // return true;
            } else {
                return false;
            }
            return false;
        });
        popup.show();
    }

    private void showViewDialog(FeedbackViewModel view, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_viewrate, null);
        builder.setView(dialogView);

        // Initialize views
        TextView email = dialogView.findViewById(R.id.email);
        TextView rating = dialogView.findViewById(R.id.rating);
        TextView comment = dialogView.findViewById(R.id.comment);

        // Populate user data (ensure null safety)
        email.setText("Email: " + (view.getEmail() != null ? view.getEmail() : "N/A"));
        rating.setText("Rating: " + view.getRating());
        comment.setText("Comment: " + (view.getComment() != null ? view.getComment() : "No Comment"));

        // Show the dialog
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    static class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView profiletv;
        TextView namep, timestamp, rate;
        ImageView optionsMenu;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv = itemView.findViewById(R.id.imagenon);
            namep = itemView.findViewById(R.id.namep);
            rate = itemView.findViewById(R.id.rate);
            timestamp = itemView.findViewById(R.id.datetimep);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);
        }
    }

}
