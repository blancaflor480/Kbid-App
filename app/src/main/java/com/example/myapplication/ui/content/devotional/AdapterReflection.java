package com.example.myapplication.ui.content.devotional;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.ui.admin.ModelAdmin;
import com.example.myapplication.ui.content.stories.ModelStories;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterReflection extends RecyclerView.Adapter<AdapterReflection.MyHolder> {

    private final Context context;
    private final List<ModelReflection> list;

    public AdapterReflection(Context context, List<ModelReflection> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_reflectionkids, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelReflection reflection = list.get(position);

        // Bind data to the views
        holder.emailTextView.setText(reflection.getEmail());
        String id = reflection.getId();

        // Format and display the timestamp
        Date timestamp = reflection.getTimestamp();
        holder.timestampTextView.setText(timestamp != null ?
                new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(timestamp) : "N/A");

        // Load image (if available) or show a placeholder
        String imageUrl = reflection.getImageUrl(); // Assuming `ModelReflection` has `getImageUrl()`.
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.image) // Replace with your placeholder drawable
                    .into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.image);
        }

        holder.optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.optionsMenu, position, id); // Pass the storyId here
            }
        });
    }

    private void showPopupMenu(View view, int position, String storyId) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.option_edit_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ModelReflection reflection = list.get(position);
                int itemId = item.getItemId();

                if (itemId == R.id.Viewprof) {
                    showAnswerDialog(reflection);
                    return true;
                } else if (itemId == R.id.Editprof) {
                    // Handle edit profile action
                   // showEditDialog(content, position); // Call edit dialog
                    return true;
                } else if (itemId == R.id.Deleteprof) {
                    // Handle delete profile action
                  //  deleteStory(content.getId(), position);
                    return true;
                } else {
                    return false;
                }
            }
        });
        popup.show();
    }

    private void showAnswerDialog(ModelReflection reflection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_viewanswer, null);
        builder.setView(dialogView);

        // Initialize views
        CircleImageView profileImageView = dialogView.findViewById(R.id.profileImageView);
        TextView controlId = dialogView.findViewById(R.id.controlid);
        TextView refelectionAnswer = dialogView.findViewById(R.id.reflectionanswer);
        TextView devotionalId = dialogView.findViewById(R.id.devotelid);
        TextView emailTextView = dialogView.findViewById(R.id.emailTextView);

        // Populate user data
        devotionalId.setText("Devotional ID: " + reflection.getId());
        controlId.setText("Control ID user: " + reflection.getControlId());
        emailTextView.setText("Email: " + reflection.getEmail());
        refelectionAnswer.setText("Reflection Answer: " + reflection.getReflectionanswer());

        // Load profile image
        if (reflection.getImageUrl() != null && !reflection.getImageUrl().isEmpty()) {
            Glide.with(context).load(reflection.getImageUrl()).into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.user); // Replace with your default image
        }

        // Show the dialog
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ViewHolder class
    static class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImageView;
        TextView emailTextView, timestampTextView;
        ImageView optionsMenu;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.imagenon); // Profile image view
            emailTextView = itemView.findViewById(R.id.namep); // Email text view
            timestampTextView = itemView.findViewById(R.id.datetimep);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);// Timestamp text view
        }
    }
}
