package com.example.myapplication.ui.content.stories;

import android.content.Context;
import android.net.Uri;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterStories extends RecyclerView.Adapter<AdapterStories.MyHolder> {

    Context context;
    List<ModelStories> list;
    FirebaseAuth firebaseAuth;
    String id;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView ivProfileImage;
    private int RESULT_OK;

    public AdapterStories(Context context, List<ModelStories> list) {
        this.context = context;
        this.list = list;
        firebaseAuth = FirebaseAuth.getInstance();
        id = firebaseAuth.getUid();
    }

    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_content, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, final int position) {
        final String hisuid = list.get(position).getId();
        String imageUrl = list.get(position).getImageUrl();
        String title = list.get(position).getTitle();
        Date timestamp = list.get(position).getTimestamp(); // Use Timestamp from Firestore

        holder.title.setText(title);

        // Convert the Firestore Timestamp to a Date object
        if (timestamp != null) {
            Date date = timestamp; // Convert Timestamp to Date
            String formattedTimestamp = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
            holder.timestamp.setText(formattedTimestamp);
        } else {
            holder.timestamp.setText("N/A");
        }

        // Check if the user image URL is null or empty
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Glide.with(context).load(imageUrl).into(holder.profiletv);
            } catch (Exception e) {
                // Handle the exception (optional: log the error)
                holder.profiletv.setImageResource(R.drawable.image); // Set default image in case of error
            }
        } else {
            // Set default image if no image URL is provided
            holder.profiletv.setImageResource(R.drawable.image);
        }

        holder.optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.optionsMenu, position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.option_edit_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                ModelStories content = list.get(position);

                if (itemId == R.id.Viewprof) {
                    // Handle view profile action
                    //showUserProfileDialog(content);
                    return true;
                } else if (itemId == R.id.Editprof) {
                    // Handle edit profile action
                    //showEditUserDialog(content);
                    return true;
                } else if (itemId == R.id.Deleteprof) {
                    // Handle delete profile action
                    //deleteUser(content.getId(), position);
                    return true;
                } else {
                    return false;
                }
            }
        });
        popup.show();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView profiletv;
        TextView title, timestamp;
        ImageView optionsMenu;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv = itemView.findViewById(R.id.imagenon);
            title = itemView.findViewById(R.id.titlep);
            timestamp = itemView.findViewById(R.id.datetimep);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);
        }
    }
}
