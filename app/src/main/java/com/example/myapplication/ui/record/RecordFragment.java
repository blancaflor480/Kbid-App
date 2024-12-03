package com.example.myapplication.ui.record;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.AggregateSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordFragment extends Fragment {
    private TextView notFoundTextView;
    private RecyclerView recyclerView;
    private AdapterRecord adapterRecord;
    private AdapterSummary adapterSummary;
    private List<RecordModel> recordList;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private RadioGroup switchMemoryVerse;
    private RadioButton userachievement;
    private RadioButton summaryreport;

    private ExecutorService executorService;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_admin, container, false);

        db = FirebaseFirestore.getInstance();
        executorService = Executors.newFixedThreadPool(4);

        initializeViews(view);
        setupRadioButtonListeners();
        loadUserAchievements();

        return view;
    }

    private void initializeViews(View view) {
        SearchView searchBar = view.findViewById(R.id.search_bar);
        notFoundTextView = view.findViewById(R.id.not_found_message);
        switchMemoryVerse = view.findViewById(R.id.switch_memoryverse);
        userachievement = view.findViewById(R.id.userachievement);
        summaryreport = view.findViewById(R.id.summaryreport);

        recyclerView = view.findViewById(R.id.recyclep);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recordList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();

        setRadioButtonStyle(userachievement, true);
        setRadioButtonStyle(summaryreport, false);

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
    }

    private void setupRadioButtonListeners() {
        switchMemoryVerse.setOnCheckedChangeListener((group, checkedId) -> {
            resetRadioButtonStyles(userachievement, summaryreport);

            if (checkedId == R.id.userachievement) {
                setRadioButtonStyle(userachievement, true);
                loadUserAchievements();
            } else if (checkedId == R.id.summaryreport) {
                setRadioButtonStyle(summaryreport, true);
                loadUserSummary();
                // Add summary report loading logic if needed
            }
        });
    }

    private void loadUserAchievements() {
        executorService.submit(() -> {
            try {
                List<RecordModel> aggregatedRecords = new ArrayList<>();

                QuerySnapshot usersSnapshot = Tasks.await(db.collection("user")
                        .orderBy("email")
                        .get());

                for (DocumentSnapshot userDoc : usersSnapshot.getDocuments()) {
                    String userEmail = userDoc.getString("email");
                    if (userEmail == null) continue;

                    RecordModel record = new RecordModel();
                    record.setEmail(userEmail);
                    record.setImageUrl(userDoc.getString("avatarName"));

                    long storyCompletedCount = 0;
                    QuerySnapshot storyAchievements = Tasks.await(db.collection("storyachievements")
                            .document(userEmail)
                            .collection("achievements")
                            .whereEqualTo("isCompleted", "completed")  // Add this line
                            .get());

                    storyCompletedCount = storyAchievements.size();  // Or use this simplified approach
                    record.setStoryId(String.valueOf(storyCompletedCount));


                    long gameCompletedCount = 0;
                    QuerySnapshot gameAchievements = Tasks.await(db.collection("gameachievements")
                            .document(userEmail)
                            .collection("gamedata")
                            .whereEqualTo("isCompleted", "completed")  // Add this line
                            .get());

                    gameCompletedCount = gameAchievements.size();  // Or use this simplified approach
                    record.setGameId(String.valueOf(gameCompletedCount));
                    // Count kids reflections (unchanged)
                    long reflectionCount = Tasks.await(db.collection("kidsReflection")
                            .whereEqualTo("email", userEmail)
                            .count()
                            .get(AggregateSource.SERVER)).getCount();
                    record.setKidsreflectionId(String.valueOf(reflectionCount));

                    aggregatedRecords.add(record);
                }

                requireActivity().runOnUiThread(() -> {
                    recordList.clear();
                    recordList.addAll(aggregatedRecords);

                    if (recordList.isEmpty()) {
                        notFoundTextView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        notFoundTextView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapterRecord = new AdapterRecord(requireContext(), recordList);
                        recyclerView.setAdapter(adapterRecord);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    notFoundTextView.setText("Error loading achievements: " + e.getMessage());
                    notFoundTextView.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void loadUserSummary() {
        executorService.submit(() -> {
            try {
                List<RecordModel> aggregatedRecords = new ArrayList<>();

                QuerySnapshot usersSnapshot = Tasks.await(db.collection("user").get());

                for (DocumentSnapshot userDoc : usersSnapshot.getDocuments()) {
                    String userEmail = userDoc.getString("email");
                    if (userEmail == null) continue;

                    RecordModel record = new RecordModel();
                    record.setEmail(userEmail);
                    record.setImageUrl(userDoc.getString("avatarName"));

                    // Count story achievements
                    long storyCompletedCount = Tasks.await(db.collection("storyachievements")
                            .document(userEmail)
                            .collection("achievements")
                            .whereEqualTo("isCompleted", "completed")
                            .count()
                            .get(AggregateSource.SERVER)).getCount();

                    // Count game achievements
                    long gameCompletedCount = Tasks.await(db.collection("gameachievements")
                            .document(userEmail)
                            .collection("gamedata")
                            .whereEqualTo("isCompleted", "completed")
                            .count()
                            .get(AggregateSource.SERVER)).getCount();

                    // Count kids reflections
                    long reflectionCount = Tasks.await(db.collection("kidsReflection")
                            .whereEqualTo("email", userEmail)
                            .count()
                            .get(AggregateSource.SERVER)).getCount();

                    // Compute total achievements
                    long totalAchievements = storyCompletedCount + gameCompletedCount + reflectionCount;
                    record.setStoryId(String.valueOf(storyCompletedCount));
                    record.setGameId(String.valueOf(gameCompletedCount));
                    record.setKidsreflectionId(String.valueOf(reflectionCount));
                    record.setTotalachievements(String.valueOf(totalAchievements));

                    aggregatedRecords.add(record);
                }

                // Sort records by total achievements (descending) to determine rank
                aggregatedRecords.sort((a, b) -> Integer.compare(
                        Integer.parseInt(b.getTotalachievements()),
                        Integer.parseInt(a.getTotalachievements())
                ));

                // Assign ranks
                for (int i = 0; i < aggregatedRecords.size(); i++) {
                    aggregatedRecords.get(i).setRank(String.valueOf(i + 1));
                }

                // Update UI
                requireActivity().runOnUiThread(() -> {
                    recordList.clear();
                    recordList.addAll(aggregatedRecords);

                    if (recordList.isEmpty()) {
                        notFoundTextView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        notFoundTextView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapterSummary = new AdapterSummary(requireContext(), recordList);
                        recyclerView.setAdapter(adapterSummary);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    notFoundTextView.setText("Error loading summary: " + e.getMessage());
                    notFoundTextView.setVisibility(View.VISIBLE);
                });
            }
        });
    }



    private void filterContent(String query) {
        List<RecordModel> filteredList = new ArrayList<>();
        for (RecordModel item : recordList) {
            if (item.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (adapterRecord != null) {
            adapterRecord.filterList(filteredList);
        }
    }

    private void setRadioButtonStyle(RadioButton radioButton, boolean isSelected) {
        if (isSelected) {
            radioButton.setTypeface(null, Typeface.BOLD);
            radioButton.setTextColor(getResources().getColor(android.R.color.black));
            radioButton.setBackgroundResource(R.drawable.bg_selected_achievement);
        } else {
            radioButton.setTypeface(null, Typeface.NORMAL);
            radioButton.setTextColor(getResources().getColor(android.R.color.darker_gray));
            radioButton.setBackgroundResource(R.drawable.bg_unselected_achievement);
        }
    }

    private void resetRadioButtonStyles(RadioButton... buttons) {
        for (RadioButton button : buttons) {
            setRadioButtonStyle(button, false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}