package com.example.myapplication.ui.content.games;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterGames extends RecyclerView.Adapter<AdapterGames.MyHolder> {

    private Context context;
    private List<ModelGames> list;
    private FirebaseAuth firebaseAuth;
    private String id;
    private Uri imageUri;
    private StorageReference storageRef;
    private GamesFragment gamesFragment; // Updated to GamesFragment

    public AdapterGames(Context context, List<ModelGames> list) {
        this.context = context;
        this.list = list;
        this.gamesFragment = gamesFragment; // Initialize with fragment reference
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.id = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_games, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, final int position) {
        final ModelGames content = list.get(position);
        String storyId = content.getId(); // Ensure this returns a valid ID
        String imageUrl = content.getImageUrl1();
        String title = content.getTitle();
        String level = content.getLevel();
        Date timestamp = content.getTimestamp();

        holder.title.setText(title);
        holder.level.setText(level);
        if (timestamp != null) {
            String formattedTimestamp = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp);
            holder.timestamp.setText(formattedTimestamp);
        } else {
            holder.timestamp.setText("N/A");
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).into(holder.profiletv);
        } else {
            holder.profiletv.setImageResource(R.drawable.image);
        }

        holder.optionsMenu.setOnClickListener(v -> showPopupMenu(holder.optionsMenu, position, storyId));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void showPopupMenu(View view, int position, String storyId) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.option_edit_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            ModelGames content = list.get(position);
            int itemId = item.getItemId();

            if (itemId == R.id.Editprof) {
                // Implement view profile logic if needed
                return true;
            }  else if (itemId == R.id.Deleteprof) {
               // deleteStory(content.getId(), position);
                return true;
            } else {
                return false;
            }
        });
        popup.show();
    }



    class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView profiletv;
        TextView title, timestamp, level;
        ImageView optionsMenu;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv = itemView.findViewById(R.id.imagenon);
            title = itemView.findViewById(R.id.titlep);
            level = itemView.findViewById(R.id.level);
            timestamp = itemView.findViewById(R.id.datetimep);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);
        }
    }
}
