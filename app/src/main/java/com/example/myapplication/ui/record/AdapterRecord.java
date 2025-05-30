package com.example.myapplication.ui.record;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.ui.admin.ModelAdmin;
import com.example.myapplication.ui.content.stories.ModelStories;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
public class AdapterRecord extends RecyclerView.Adapter<AdapterRecord.RecordMyHolder> {
    private final Context context;
    private final List<RecordModel> list;

    public AdapterRecord(Context context, List<RecordModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecordMyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_userachievement, parent, false);
        return new RecordMyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordMyHolder holder, int position) {
        RecordModel record = list.get(position);

        // Set the email
        holder.emailTextView.setText(record.getEmail());

        if (record.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            String formattedTimestamp = sdf.format(record.getTimestamp());
            holder.datetimep.setText(formattedTimestamp);
            holder.datetimep.setVisibility(View.VISIBLE);
        } else {
            holder.datetimep.setVisibility(View.GONE);
        }

        // Set the profile image if available
        if (record.getImageUrl() != null && !record.getImageUrl().isEmpty()) {
            int drawableResourceId = context.getResources().getIdentifier(
                    record.getImageUrl(),
                    "drawable",
                    context.getPackageName()
            );

            if (drawableResourceId != 0) {
                Glide.with(context)
                        .load(drawableResourceId)
                        .placeholder(R.drawable.userkids)
                        .into(holder.profileImageView);
            } else {
                // Fallback to default image if drawable not found
                Glide.with(context)
                        .load(R.drawable.userkids)
                        .into(holder.profileImageView);
            }
        }

        // Set the counts for story achievements, game achievements, and kids reflections
        holder.storyachievments.setText("Story Achievements: " + record.getStoryId());
        holder.gameachievements.setText("Game Achievements: " + record.getGameId());
        holder.KidsReflection.setText("Kids Reflections: " + record.getKidsreflectionId());
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public void filterList(List<RecordModel> filteredList) {
        // Filter logic if needed
    }

    static class RecordMyHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImageView;
        TextView emailTextView, storyachievments, KidsReflection, gameachievements,datetimep;


        public RecordMyHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.imagenon); // Profile image view
            emailTextView = itemView.findViewById(R.id.namep); // Email text view
            storyachievments = itemView.findViewById(R.id.storyachievments);
            KidsReflection = itemView.findViewById(R.id.KidsReflection);
            gameachievements = itemView.findViewById(R.id.gameachievements);
            datetimep = itemView.findViewById(R.id.datetimep);
             // Options menu button
        }
    }
}

