    package com.example.myapplication.ui.content.games;

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

    import android.widget.ImageView;
    import android.widget.SearchView;
    import android.widget.Spinner;
    import android.widget.TextView;

    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AlertDialog;
    import androidx.fragment.app.Fragment;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.myapplication.R;
    import com.example.myapplication.database.AppDatabase;
    import com.example.myapplication.database.gamesdb.Games;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.android.material.floatingactionbutton.FloatingActionButton;
    import com.google.android.material.snackbar.Snackbar;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.CollectionReference;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.Query;
    import com.google.firebase.firestore.QueryDocumentSnapshot;
    import com.google.firebase.storage.FirebaseStorage;
    import com.google.firebase.storage.StorageReference;
    import com.google.firebase.storage.StorageTask;
    import com.google.firebase.storage.UploadTask;

    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.List;

    public class GamesFragment extends Fragment {

        private static final int PICK_IMAGE_REQUEST1 = 1;
        private static final int PICK_IMAGE_REQUEST2 = 2;
        private static final int PICK_IMAGE_REQUEST3 = 3;
        private static final int PICK_IMAGE_REQUEST4 = 4;

        private Uri imageUri1, imageUri2, imageUri3, imageUri4;
        private StorageReference storageRef;
        private TextView notFoundTextView;
        private RecyclerView recyclerView;
        private AdapterGames adapterContent;
        private List<ModelGames> contentList;
        private FirebaseAuth firebaseAuth;
        private FirebaseFirestore db;
        private ImageView ivImagePreview1, ivImagePreview2,ivImagePreview3,ivImagePreview4;
        

        public GamesFragment() {
            // Required empty public constructor
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_games_fourpics, container, false);

            Spinner spinner = view.findViewById(R.id.user_dropdown);
            SearchView searchBar = view.findViewById(R.id.search_bar);
            notFoundTextView = view.findViewById(R.id.not_found_message);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.option_stories, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0: // All
                            getAll();
                            break;
                        case 1: // New
                            getNew();
                            break;
                        case 2: // Old
                            getOld();
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

        private void getAll() {
            loadContentFromCollection("games", null, null);
        }

        private void getNew() {
            // Get the first day of the current month
            Calendar firstDayOfMonth = Calendar.getInstance();
            firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

            // Get the last day of the current month
            Calendar lastDayOfMonth = Calendar.getInstance();
            lastDayOfMonth.set(Calendar.DAY_OF_MONTH, lastDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

            loadContentFromCollection("games", firstDayOfMonth.getTime(), lastDayOfMonth.getTime());
        }

        private void getOld() {
            // Get the first day of the previous month
            Calendar firstDayOfLastMonth = Calendar.getInstance();
            firstDayOfLastMonth.add(Calendar.MONTH, -1);
            firstDayOfLastMonth.set(Calendar.DAY_OF_MONTH, 1);

            // Get the last day of the previous month
            Calendar lastDayOfLastMonth = Calendar.getInstance();
            lastDayOfLastMonth.add(Calendar.MONTH, -1);
            lastDayOfLastMonth.set(Calendar.DAY_OF_MONTH, lastDayOfLastMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

            loadContentFromCollection("games", firstDayOfLastMonth.getTime(), lastDayOfLastMonth.getTime());
        }

        private void loadContentFromCollection(String collectionName, Date startDate, Date endDate) {
            CollectionReference collectionRef = db.collection(collectionName);
            Query query = collectionRef;

            if (startDate != null && endDate != null) {
                query = query.whereGreaterThanOrEqualTo("timestamp", startDate)
                        .whereLessThanOrEqualTo("timestamp", endDate);
            }

            query.addSnapshotListener((value, error) -> {
                if (error != null) return;
                contentList.clear();
                for (QueryDocumentSnapshot document : value) {
                    ModelGames content = document.toObject(ModelGames.class);
                    contentList.add(content);
                }
                adapterContent = new AdapterGames(getActivity(), contentList);
                recyclerView.setAdapter(adapterContent);
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            notFoundTextView.setVisibility(View.GONE);
            adapterContent = new AdapterGames(getContext(), contentList);
            recyclerView.setAdapter(adapterContent);
        }

        private void filterContent(String query) {
            List<ModelGames> filteredList = new ArrayList<>();
            for (ModelGames content : contentList) {
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

            adapterContent = new AdapterGames(getContext(), filteredList);
            recyclerView.setAdapter(adapterContent);
        }

        private void showAddContentDialog() {
            View addView = getLayoutInflater().inflate(R.layout.dialog_add_fourpics, null);

            EditText etTitle = addView.findViewById(R.id.etTitle);
            EditText etAnswer = addView.findViewById(R.id.etAnswer);
            ivImagePreview1 = addView.findViewById(R.id.ivImagePreview1);
            ivImagePreview2 = addView.findViewById(R.id.ivImagePreview2);
            ivImagePreview3 = addView.findViewById(R.id.ivImagePreview3);
            ivImagePreview4 = addView.findViewById(R.id.ivImagePreview4);

            Button btnUploadImage1 = addView.findViewById(R.id.btnUploadImage1);
            Button btnUploadImage2 = addView.findViewById(R.id.btnUploadImage2);
            Button btnUploadImage3 = addView.findViewById(R.id.btnUploadImage3);
            Button btnUploadImage4 = addView.findViewById(R.id.btnUploadImage4);

            // Set up listeners for image upload buttons
            btnUploadImage1.setOnClickListener(v -> selectImage(PICK_IMAGE_REQUEST1));
            btnUploadImage2.setOnClickListener(v -> selectImage(PICK_IMAGE_REQUEST2));
            btnUploadImage3.setOnClickListener(v -> selectImage(PICK_IMAGE_REQUEST3));
            btnUploadImage4.setOnClickListener(v -> selectImage(PICK_IMAGE_REQUEST4));

            new AlertDialog.Builder(getContext())
                    .setView(addView)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String title = etTitle.getText().toString();
                        String answer = etAnswer.getText().toString();

                        uploadImagesToFirebase(title, answer); // Upload images first and then add story
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }


        private void selectImage(int requestCode) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, requestCode);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();

                switch (requestCode) {
                    case PICK_IMAGE_REQUEST1:
                        imageUri1 = selectedImageUri;
                        ivImagePreview1.setImageURI(imageUri1);
                        break;
                    case PICK_IMAGE_REQUEST2:
                        imageUri2 = selectedImageUri;
                        ivImagePreview2.setImageURI(imageUri2);
                        break;
                    case PICK_IMAGE_REQUEST3:
                        imageUri3 = selectedImageUri;
                        ivImagePreview3.setImageURI(imageUri3);
                        break;
                    case PICK_IMAGE_REQUEST4:
                        imageUri4 = selectedImageUri;
                        ivImagePreview4.setImageURI(imageUri4);
                        break;
                }
            }
        }


        private void uploadImagesToFirebase(String title, String answer) {
            // Upload all four images and get their URLs
            if (imageUri1 != null && imageUri2 != null && imageUri3 != null && imageUri4 != null) {
                uploadSingleImage(imageUri1, uri1 -> {
                    String url1 = uri1.toString();
                    uploadSingleImage(imageUri2, uri2 -> {
                        String url2 = uri2.toString();
                        uploadSingleImage(imageUri3, uri3 -> {
                            String url3 = uri3.toString();
                            uploadSingleImage(imageUri4, uri4 -> {
                                String url4 = uri4.toString();
                                uploadGamesToFirestore(url1, url2, url3, url4, title, answer);
                            });
                        });
                    });
                });
            } else {
                Snackbar.make(getView(), "Please upload all images.", Snackbar.LENGTH_SHORT).show();
            }
        }

        private void uploadSingleImage(Uri imageUri, OnSuccessListener<Uri> onSuccess) {
            StorageReference imageRef = storageRef.child("Content/games/fourpicsoneword" + System.currentTimeMillis() + ".jpg");

            StorageTask<UploadTask.TaskSnapshot> uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(e -> Snackbar.make(getView(), "Failed to get image URL.", Snackbar.LENGTH_SHORT).show()))
                    .addOnFailureListener(e -> Snackbar.make(getView(), "Image upload failed.", Snackbar.LENGTH_SHORT).show());
        }

        private void uploadGamesToFirestore(String imageUrl1, String imageUrl2, String imageUrl3, String imageUrl4, String title, String answer) {
            // First, get the current highest level
            getHighestLevelFromFirestore(highestLevel -> {
                // Set the next level as highestLevel + 1
                String level = String.valueOf(highestLevel + 1);
                String id = db.collection("games").document().getId(); // Auto-generate an ID for the game
                Date currentDate = new Date();

                // Create a new ModelGames object with the next level and without userEmail
                ModelGames newGame = new ModelGames(
                        title, answer, level, imageUrl1, imageUrl2, imageUrl3, imageUrl4, id, currentDate
                );

                // Save the game to Firestore
                db.collection("games")
                        .document(id)
                        .set(newGame)
                        .addOnSuccessListener(aVoid -> {
                            Snackbar.make(getView(), "Game added successfully.", Snackbar.LENGTH_SHORT).show();

                            // Insert the game into SQLite
                            insertGameIntoSQLite(newGame);
                        })
                        .addOnFailureListener(e -> Snackbar.make(getView(), "Failed to add game.", Snackbar.LENGTH_SHORT).show());
            });
        }

        private void insertGameIntoSQLite(ModelGames modelGames) {
            // Create a new Game object for SQLite
            Games game = new Games();
            game.setTitle(modelGames.getTitle());
            game.setAnswer(modelGames.getAnswer());
            game.setLevel(modelGames.getLevel());
            game.setImageUrl1(modelGames.getImageUrl1());
            game.setImageUrl2(modelGames.getImageUrl2());
            game.setImageUrl3(modelGames.getImageUrl3());
            game.setImageUrl4(modelGames.getImageUrl4());
            game.setTimestamp(modelGames.getTimestamp());

            // Insert the game into SQLite
            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(getContext());
                db.gamesDao().insert(game);
            }).start();
        }


        private void getHighestLevelFromFirestore(OnSuccessListener<Integer> onSuccessListener) {
            // Query Firestore to get the highest level
            db.collection("games")
                    .orderBy("level", Query.Direction.DESCENDING) // Sort by level in descending order
                    .limit(1) // Only get the top document
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // If there are existing levels, get the highest one
                            String highestLevelStr = queryDocumentSnapshots.getDocuments().get(0).getString("level");
                            int highestLevel = Integer.parseInt(highestLevelStr);
                            onSuccessListener.onSuccess(highestLevel);
                        } else {
                            // If no levels exist, start from 0
                            onSuccessListener.onSuccess(0);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors
                        Snackbar.make(getView(), "Failed to fetch highest level.", Snackbar.LENGTH_SHORT).show();
                        onSuccessListener.onSuccess(0); // Default to 0 if failed
                    });
        }


    }
