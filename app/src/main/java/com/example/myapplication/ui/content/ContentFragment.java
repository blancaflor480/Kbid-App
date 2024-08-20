package com.example.myapplication.ui.content;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.user.AdapterUser;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ContentFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageReference storageRef;
    private TextView notFoundTextView;
    private RecyclerView recyclerView;
    private AdapterContent adapterContent;
    private List<ModelContent> contentList;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private ImageView ivImagePreview;
    private ImageView ivProfileImage;

    public ContentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_admin, container, false);

        Spinner spinner = view.findViewById(R.id.user_dropdown);
        SearchView searchBar = view.findViewById(R.id.search_bar);
        notFoundTextView = view.findViewById(R.id.not_found_message);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.option_content, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        getStories();
                        break;
                    case 1:
                        getVideo();
                        break;
                    case 2:
                        getSong();
                        break;
                    case 3:
                        getQuiz();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        recyclerView = view.findViewById(R.id.recyclep);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        contentList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();


        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddContentDialog());

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterContent(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterContent(newText);
                return false;
            }
        });

        return view;
    }

    private void getStories() {
        loadContentFromCollection("stories");
    }

    private void getVideo() {
        loadContentFromCollection("video");
    }

    private void getSong() {
        loadContentFromCollection("song");
    }

    private void getQuiz() {
        loadContentFromCollection("quiz");
    }

    private void loadContentFromCollection(String collectionName) {
        CollectionReference collectionRef = db.collection(collectionName);
        collectionRef.addSnapshotListener((value, error) -> {
            if (error != null) return;
            contentList.clear();
            for (QueryDocumentSnapshot document : value) {
                ModelContent content = document.toObject(ModelContent.class);
                contentList.add(content);
            }
            adapterContent = new AdapterContent(getActivity(), contentList);
            recyclerView.setAdapter(adapterContent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        notFoundTextView.setVisibility(View.GONE);
        adapterContent = new AdapterContent(getContext(), contentList);
        recyclerView.setAdapter(adapterContent);
    }

    private void filterContent(String query) {
        List<ModelContent> filteredList = new ArrayList<>();
        for (ModelContent content : contentList) {
            String title = content.getTitle() != null ? content.getTitle().toLowerCase() : "";
            if (title.contains(query.toLowerCase())) {
                filteredList.add(content);
            }
        }

        if (filteredList.isEmpty()) {
            notFoundTextView.setVisibility(View.VISIBLE);
        } else {
            notFoundTextView.setVisibility(View.GONE);
        }

        adapterContent = new AdapterContent(getContext(), filteredList);
        recyclerView.setAdapter(adapterContent);
    }

    private void showAddContentDialog() {
        View addView;
        int selectedPosition = ((Spinner) getView().findViewById(R.id.user_dropdown)).getSelectedItemPosition();

        switch (selectedPosition) {
            case 0: // Stories
                addView = getLayoutInflater().inflate(R.layout.dialog_add_stories, null);
                handleAddStories(addView);
                break;
            case 1: // Videos
                addView = getLayoutInflater().inflate(R.layout.dialog_add_video, null);
                handleAddVideo(addView);
                break;
            case 2: // Songs
                addView = getLayoutInflater().inflate(R.layout.dialog_add_song, null);
                handleAddSong(addView);
                break;
            default:
                return;
        }

        // Create and show the dialog
        new AlertDialog.Builder(getContext())
                .setView(addView)
                .setPositiveButton("Add", (dialog, which) -> {
                    // Handle dialog positive button click
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void handleAddStories(View view) {
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etVerse = view.findViewById(R.id.etVerse);
        EditText etDescription = view.findViewById(R.id.etDescription);
        ivImagePreview = view.findViewById(R.id.ivImagePreview);
        Button btnUploadImage = view.findViewById(R.id.btnUploadImage);

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        new AlertDialog.Builder(getContext())
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTitle.getText().toString();
                    String verse = etVerse.getText().toString();
                    String description = etDescription.getText().toString();

                    if (imageUri != null) {
                        uploadImageToFirebase(imageUri, title, verse, description);
                    } else {
                        uploadStoryToFirestore(null, title, verse, description);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivImagePreview.setImageURI(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String title, String verse, String description) {
        StorageReference imageRef = storageRef.child("Content/stories/" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    uploadStoryToFirestore(downloadUri.toString(), title, verse, description);
                }))
                .addOnFailureListener(e -> {
                    Snackbar.make(getView(), "Image upload failed.", Snackbar.LENGTH_SHORT).show();
                });
    }


    // Method to upload story to Firestore
    private void uploadStoryToFirestore(String imageUrl, String title, String verse, String description) {
        ModelContent story = new ModelContent(title, verse, description, imageUrl);

        db.collection("stories").add(story)
                .addOnSuccessListener(documentReference -> {
                    Snackbar.make(getView(), "Story added successfully!", Snackbar.LENGTH_LONG).show();
                    getStories();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(getView(), "Failed to add story.", Snackbar.LENGTH_SHORT).show();
                });
    }


    private void handleAddVideo(View view) {
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etDescription = view.findViewById(R.id.etDescription);
        Button btnUploadImage = view.findViewById(R.id.btnUploadImage);

        btnUploadImage.setOnClickListener(v -> {
            // Handle video upload logic
        });

        // Add the logic to upload the video to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String title = etTitle.getText().toString();
        String description = etDescription.getText().toString();

        // Create a ModelContent object for videos
        ModelContent video = new ModelContent(title, description);
        db.collection("video").add(video).addOnSuccessListener(documentReference -> {
            // Handle success
        }).addOnFailureListener(e -> {
            // Handle failure
        });
    }

    private void handleAddSong(View view) {
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etDescription = view.findViewById(R.id.etDescription);
        Button btnUploadImage = view.findViewById(R.id.btnUploadImage);

        btnUploadImage.setOnClickListener(v -> {
            // Handle song upload logic
        });

        // Add the logic to upload the song to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String title = etTitle.getText().toString();
        String description = etDescription.getText().toString();

        // Create a ModelContent object for songs
        ModelContent song = new ModelContent(title, description);
        db.collection("song").add(song).addOnSuccessListener(documentReference -> {
            // Handle success
        }).addOnFailureListener(e -> {
            // Handle failure
        });
    }


}
