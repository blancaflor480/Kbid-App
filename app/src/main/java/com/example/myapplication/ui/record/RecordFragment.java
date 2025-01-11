package com.example.myapplication.ui.record;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordFragment extends Fragment {
    private TextView notFoundTextView,not_found_message;
    private RecyclerView recyclerView;
    private AdapterRecord adapterRecord;
    private AdapterSummary adapterSummary;
    private List<RecordModel> recordList;
    private boolean isHighSort = true;
    private ImageButton calendarButton;
    private TextView dateTextView;
    private Date selectedDate;
    private ImageButton filterButton;
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
        filterButton = view.findViewById(R.id.filter);
        filterButton.setOnClickListener(v -> toggleSort());
        initializeViews(view);
        setupRadioButtonListeners();
        loadUserAchievements();
        scheduleWeeklySaving();

        calendarButton = view.findViewById(R.id.calendar);
        dateTextView = view.findViewById(R.id.date);

        calendarButton.setOnClickListener(v -> showDatePicker());

        // Set default to current date
        selectedDate = new Date();
        updateDateDisplay();
        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDate = selectedCalendar.getTime();
                    updateDateDisplay();
                    loadUserSummary(); // Reload summary with selected date
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        dateTextView.setText(dateFormat.format(selectedDate));
    }

    private void toggleSort() {
        isHighSort = !isHighSort;
        filterButton.setBackgroundResource(isHighSort ? R.drawable.highsort : R.drawable.lowsort);

        if (recordList != null && !recordList.isEmpty()) {
            recordList.sort((a, b) -> {
                int totalA = calculateTotal(a);
                int totalB = calculateTotal(b);
                return isHighSort ? Integer.compare(totalB, totalA) : Integer.compare(totalA, totalB);
            });
            updateRanks();
            updateAdapter();
        }
    }

    private int calculateTotal(RecordModel record) {
        try {
            int story = Integer.parseInt(record.getStoryId());
            int game = Integer.parseInt(record.getGameId());
            int reflection = Integer.parseInt(record.getKidsreflectionId());
            return story + game + reflection;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    private void updateRanks() {
        // Update ranks based on the current list order
        for (int i = 0; i < recordList.size(); i++) {
            recordList.get(i).setRank(String.valueOf(i + 1));
        }
    }

    private void updateAdapter() {
        // Notify the correct adapter based on the checked state
        if (summaryreport.isChecked() && adapterSummary != null) {
            adapterSummary.notifyDataSetChanged();
        } else if (adapterRecord != null) {
            adapterRecord.notifyDataSetChanged();
        }
    }


    private void initializeViews(View view) {
        SearchView searchBar = view.findViewById(R.id.search_bar);
        not_found_message = view.findViewById(R.id.not_found_message);
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
                aggregatedRecords.sort((a, b) -> Integer.compare(
                        calculateTotal(b), calculateTotal(a)
                ));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = dateFormat.format(new Date());

                QuerySnapshot usersSnapshot = Tasks.await(db.collection("user")
                        .orderBy("email")
                        .get());


                for (int i = 0; i < aggregatedRecords.size(); i++) {
                    RecordModel record = aggregatedRecords.get(i);
                    record.setRank(String.valueOf(i + 1));
                    record.setTotalachievements(String.valueOf(calculateTotal(record)));
                }
                for (DocumentSnapshot userDoc : usersSnapshot.getDocuments()) {
                    String userEmail = userDoc.getString("email");
                    if (userEmail == null) continue;

                    QuerySnapshot reportSnapshot = Tasks.await(
                            db.collection("reports")
                                    .document(formattedDate)
                                    .collection("userReports")
                                    .whereEqualTo("email", userEmail)
                                    .get()
                    );

                    RecordModel record = new RecordModel();
                    record.setEmail(userEmail);
                    record.setImageUrl(userDoc.getString("avatarName"));
                    if (!reportSnapshot.isEmpty()) {
                        DocumentSnapshot reportDoc = reportSnapshot.getDocuments().get(0);
                        Timestamp timestamp = reportDoc.getTimestamp("timestamp");
                        if (timestamp != null) {
                            record.setTimestamp(timestamp.toDate());
                        }
                    }


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
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedDate);

                // Determine date range for filtering
                Calendar startOfDay = (Calendar) calendar.clone();
                startOfDay.set(Calendar.HOUR_OF_DAY, 0);
                startOfDay.set(Calendar.MINUTE, 0);
                startOfDay.set(Calendar.SECOND, 0);

                Calendar endOfDay = (Calendar) calendar.clone();
                endOfDay.set(Calendar.HOUR_OF_DAY, 23);
                endOfDay.set(Calendar.MINUTE, 59);
                endOfDay.set(Calendar.SECOND, 59);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = dateFormat.format(selectedDate);

                QuerySnapshot usersSnapshot = Tasks.await(db.collection("user").get());

                for (DocumentSnapshot userDoc : usersSnapshot.getDocuments()) {
                    String userEmail = userDoc.getString("email");
                    if (userEmail == null) continue;

                    QuerySnapshot reportSnapshot = Tasks.await(
                            db.collection("reports")
                                    .document(formattedDate)
                                    .collection("userReports")
                                    .whereEqualTo("email", userEmail)
                                    .get()
                    );
                    if (reportSnapshot.isEmpty()) continue;
                    DocumentSnapshot reportDoc = reportSnapshot.getDocuments().get(0);

                    RecordModel record = new RecordModel();
                    record.setEmail(userEmail);
                    record.setImageUrl(userDoc.getString("avatarName"));
                    record.setName(userDoc.getString("name"));
                    Timestamp timestamp = reportDoc.getTimestamp("timestamp");
                    if (timestamp != null) {
                        record.setTimestamp(timestamp.toDate());
                    }

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

                    record.setStoryId(String.valueOf(reportDoc.getLong("storyCount")));
                    record.setGameId(String.valueOf(reportDoc.getLong("gameCount")));
                    record.setKidsreflectionId(String.valueOf(reportDoc.getLong("reflectionCount")));
                    record.setTotalachievements(String.valueOf(reportDoc.getLong("totalCount")));
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
                        not_found_message.setText("No reports found for " + dateTextView.getText());
                        not_found_message.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        not_found_message.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapterSummary = new AdapterSummary(requireContext(), recordList);
                        recyclerView.setAdapter(adapterSummary);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    not_found_message.setText("Error loading summary: " + e.getMessage());
                    not_found_message.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void triggerWeeklyReportSave() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String weekStartDate = weekFormat.format(calendar.getTime());

        executorService.submit(() -> {
            try {
                List<Map<String, Object>> weeklyReports = new ArrayList<>();
                QuerySnapshot usersSnapshot = Tasks.await(db.collection("user").get());

                for (DocumentSnapshot userDoc : usersSnapshot.getDocuments()) {
                    String userEmail = userDoc.getString("email");
                    if (userEmail == null) continue;

                    // Real-time achievement counting
                    long storyCount = Tasks.await(db.collection("storyachievements")
                            .document(userEmail)
                            .collection("achievements")
                            .whereEqualTo("isCompleted", "completed")
                            .count()
                            .get(AggregateSource.SERVER)).getCount();

                    long gameCount = Tasks.await(db.collection("gameachievements")
                            .document(userEmail)
                            .collection("gamedata")
                            .whereEqualTo("isCompleted", "completed")
                            .count()
                            .get(AggregateSource.SERVER)).getCount();

                    long reflectionCount = Tasks.await(db.collection("kidsReflection")
                            .whereEqualTo("email", userEmail)
                            .count()
                            .get(AggregateSource.SERVER)).getCount();

                    Map<String, Object> reportData = new HashMap<>();
                    reportData.put("email", userEmail);
                    reportData.put("storyCount", storyCount);
                    reportData.put("gameCount", gameCount);
                    reportData.put("reflectionCount", reflectionCount);
                    reportData.put("totalCount", storyCount + gameCount + reflectionCount);
                    reportData.put("timestamp", new Date());

                    weeklyReports.add(reportData);
                }

                // Batch write all reports for the week
                WriteBatch batch = db.batch();
                CollectionReference weekReportRef = db.collection("reports").document(weekStartDate).collection("userReports");

                for (Map<String, Object> reportData : weeklyReports) {
                    DocumentReference docRef = weekReportRef.document((String) reportData.get("email"));
                    batch.set(docRef, reportData);
                }

                // Commit the batch
                Tasks.await(batch.commit());

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("WeeklyReport", "Error saving weekly report: " + e.getMessage());
            }
        });
    }

    // Method to schedule weekly reporting
    private void scheduleWeeklySaving() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        triggerWeeklyReportSave();
        // If today is past Sunday, move to next week
        if (calendar.getTime().before(new Date())) {
            calendar.add(Calendar.DATE, 7);
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                triggerWeeklyReportSave();
            }
        }, calendar.getTime(), 7 * 24 * 60 * 60 * 1000); // Weekly interval
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
            radioButton.setTextColor(getResources().getColor(R.color.black));
            radioButton.setBackgroundResource(R.drawable.bg_selected_devotional);
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