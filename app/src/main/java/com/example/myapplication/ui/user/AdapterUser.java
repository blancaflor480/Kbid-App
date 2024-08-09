package com.example.myapplication.ui.user;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {

    Context context;
    List<ModelUser> list;
    FirebaseAuth firebaseAuth;
    String uid;

    public AdapterUser(Context context, List<ModelUser> list) {
        this.context = context;
        this.list = list;
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        final String hisuid = list.get(position).getUid();
        String userImage = list.get(position).getImageUrl();
        String username = list.get(position).getName();
        String usermail = list.get(position).getEmail();
        String role = list.get(position).getRole();

        holder.name.setText(username);
        holder.email.setText(usermail);
        holder.role.setText(role);

        try {
            Glide.with(context).load(userImage).into(holder.profiletv);
        } catch (Exception e) {
            // Handle the exception
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
                ModelUser user = list.get(position);

                if (itemId == R.id.Viewprof) {
                    // Handle view profile action
                    Intent viewIntent = new Intent(context, ViewUserProfileActivity.class);
                    viewIntent.putExtra("USER_ID", user.getUid());
                    viewIntent.putExtra("USER_ROLE", user.getRole());
                    context.startActivity(viewIntent);
                    return true;
                } else if (itemId == R.id.Editprof) {
                    // Handle edit profile action
                    Intent editIntent = new Intent(context, UserFragment.class);
                    editIntent.putExtra("USER_ID", user.getUid());
                    editIntent.putExtra("USER_ROLE", user.getRole());
                    context.startActivity(editIntent);
                    return true;
                } else if (itemId == R.id.Deleteprof) {
                    // Handle delete profile action
                    deleteUser(user.getUid(), position);
                    return true;
                } else {
                    return false;
                }
            }
        });
        popup.show();
    }

    private void deleteUser(String userId, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String collectionName = list.get(position).getRole().equalsIgnoreCase("Admin") ? "admin" : "user";

        db.collection(collectionName).document(userId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show();
                        list.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error deleting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView profiletv;
        TextView name, email, role;
        ImageView optionsMenu;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv = itemView.findViewById(R.id.imagep);
            name = itemView.findViewById(R.id.namep);
            email = itemView.findViewById(R.id.emailp);
            role = itemView.findViewById(R.id.rolep);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);
        }
    }
}
