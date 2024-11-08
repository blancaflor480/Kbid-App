package com.example.myapplication.ui.content.song;

import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.ui.content.stories.ImageFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterSong extends RecyclerView.Adapter<AdapterSong.MyHolder> {

    private Context context;
    private List<ModelSong> list;
    private FirebaseAuth firebaseAuth;
    private String id;
    private Uri imageUri;
    private StorageReference storageRef;
    private SongFragment songFragment;

    public AdapterSong(Context context, List<ModelSong> list) {
        this.context = context;
        this.list = list;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.id = firebaseAuth.getUid();
    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_content, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, final int position) {
        final ModelSong content = list.get(position);
        String songId = content.getId();
        String imageUrl = content.getImageUrl();
        String title = content.getTitle();
        Date timestamp = content.getTimestamp();

        holder.title.setText(title);
        holder.timestamp.setText(timestamp != null ?
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp) : "N/A");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).into(holder.profiletv);
        } else {
            holder.profiletv.setImageResource(R.drawable.image);
        }

        holder.optionsMenu.setOnClickListener(v -> showPopupMenu(holder.optionsMenu, position, songId));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void showPopupMenu(View view, int position, String songId) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.option_edit_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            ModelSong content = list.get(position);
            int itemId = item.getItemId();

            if (itemId == R.id.Viewprof) {
                // Add view profile functionality
                return true;
            } else if (itemId == R.id.Editprof) {
                showEditDialog(content, position);
                return true;
            } else if (itemId == R.id.Deleteprof) {
                deleteSong(content.getId(), position);
                return true;
            } else {
                return false;
            }
        });
        popup.show();
    }

    private void showEditDialog(ModelSong content, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_song, null);
        builder.setView(dialogView);

        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        ImageFragment imageFragment = (ImageFragment) fragmentManager.findFragmentByTag("IMAGE_FRAGMENT");

        if (imageFragment == null) {
            imageFragment = new ImageFragment();
            fragmentManager.beginTransaction().add(imageFragment, "IMAGE_FRAGMENT").commit();
            fragmentManager.executePendingTransactions();
        }

        ImageView imagePreview = dialogView.findViewById(R.id.ivImagePreview);
        EditText titleInput = dialogView.findViewById(R.id.etTitle);
        EditText descriptionInput = dialogView.findViewById(R.id.etDescription);
        Button btnUploadImage = dialogView.findViewById(R.id.btnUploadImage);
        Button btnUploadSong = dialogView.findViewById(R.id.btnUploadSong);

        titleInput.setText(content.getTitle());
        descriptionInput.setText(content.getDescription());

        if (content.getImageUrl() != null) {
            Glide.with(context).load(content.getImageUrl()).into(imagePreview);
        } else {
            imagePreview.setImageResource(R.drawable.image);
        }

        imageFragment.setOnImageSelectedListener(imageUri -> updateImageUri(imageUri, imagePreview));

        ImageFragment finalImageFragment = imageFragment;
        btnUploadImage.setOnClickListener(v -> {
            if (finalImageFragment.isAdded() && !finalImageFragment.isDetached()) {
                finalImageFragment.openFileChooser();
            } else {
                Toast.makeText(context, "Image picker not available. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("Update", (dialog, which) -> {
            String updatedTitle = titleInput.getText().toString().trim();
            String updatedDescription = descriptionInput.getText().toString().trim();

            if (updatedTitle.isEmpty() || updatedDescription.isEmpty()) {
                Toast.makeText(context, "Please fill out all required fields.", Toast.LENGTH_SHORT).show();
            } else {
                updateSong(content.getId(), updatedTitle, updatedDescription, position);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateImageUri(Uri imageUri, ImageView imagePreview) {
        this.imageUri = imageUri;
        if (imagePreview != null) {
            imagePreview.setImageURI(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String songId, String updatedTitle, String updatedDescription, int position) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("songs/" + songId + ".jpg");

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference songRef = db.collection("songs").document(songId);

                    songRef.update("title", updatedTitle, "description", updatedDescription, "imageUrl", downloadUrl.toString())
                            .addOnSuccessListener(aVoid -> {
                                list.get(position).setTitle(updatedTitle);
                                list.get(position).setRole(updatedDescription);
                                list.get(position).setImageUrl(downloadUrl.toString());
                                notifyItemChanged(position);
                                Toast.makeText(context, "Song updated successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to update song: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }).addOnFailureListener(e -> Toast.makeText(context, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateSong(String songId, String updatedTitle, String updatedDescription, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference songRef = db.collection("songs").document(songId);

        if (imageUri != null) {
            uploadImageToFirebase(imageUri, songId, updatedTitle, updatedDescription, position);
        } else {
            songRef.update("title", updatedTitle, "description", updatedDescription)
                    .addOnSuccessListener(aVoid -> {
                        list.get(position).setTitle(updatedTitle);
                        list.get(position).setRole(updatedDescription);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Song updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update song", Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteSong(String songId, int position) {
        if (songId == null) {
            Toast.makeText(context, "Error: Song ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        new AlertDialog.Builder(context)
                .setTitle("Delete Song")
                .setMessage("Are you sure you want to delete this song?")
                .setPositiveButton("Yes", (dialog, which) -> db.collection("songs").document(songId).delete()
                        .addOnSuccessListener(aVoid -> {
                            list.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Song deleted successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete song", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
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
