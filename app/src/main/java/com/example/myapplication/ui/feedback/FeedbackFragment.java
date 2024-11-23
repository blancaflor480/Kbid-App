package com.example.myapplication.ui.feedback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FeedbackFragment extends Fragment {
    private TextView notFoundTextView;
    private RecyclerView recyclerView;
    private AdapterFeedback adapterFeedback;
    private List<FeedbackViewModel> feedbackList;
    private FirebaseFirestore db;

    public FeedbackFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback_admin, container, false);

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
                    case 0:
                        getAllfeedback();
                        break;
                    case 1:
                        getNewfeedback();
                        break;
                    case 2:
                        getOldfeedback();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        recyclerView = view.findViewById(R.id.recyclep);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        feedbackList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

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

    private void getAllfeedback() {
        loadContentFromCollection("rating_feedback", null, null);
    }

    private void getNewfeedback() {
        Calendar firstDayOfMonth = Calendar.getInstance();
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

        Calendar lastDayOfMonth = Calendar.getInstance();
        lastDayOfMonth.set(Calendar.DAY_OF_MONTH, lastDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

        loadContentFromCollection("rating_feedback", firstDayOfMonth.getTime(), lastDayOfMonth.getTime());
    }

    private void getOldfeedback() {
        Calendar firstDayOfLastMonth = Calendar.getInstance();
        firstDayOfLastMonth.add(Calendar.MONTH, -1);
        firstDayOfLastMonth.set(Calendar.DAY_OF_MONTH, 1);

        Calendar lastDayOfLastMonth = Calendar.getInstance();
        lastDayOfLastMonth.add(Calendar.MONTH, -1);
        lastDayOfLastMonth.set(Calendar.DAY_OF_MONTH, lastDayOfLastMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

        loadContentFromCollection("rating_feedback", firstDayOfLastMonth.getTime(), lastDayOfLastMonth.getTime());
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
            feedbackList.clear();
            for (QueryDocumentSnapshot document : value) {
                FeedbackViewModel content = document.toObject(FeedbackViewModel.class);
                feedbackList.add(content);
            }
            adapterFeedback = new AdapterFeedback(getActivity(), feedbackList);
            recyclerView.setAdapter(adapterFeedback);
        });
    }

    private void filterContent(String query) {
        List<FeedbackViewModel> filteredList = new ArrayList<>();
        for (FeedbackViewModel feedback : feedbackList) {
            if (feedback.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(feedback);
            }
        }
        adapterFeedback = new AdapterFeedback(getActivity(), filteredList);
        recyclerView.setAdapter(adapterFeedback);
    }
}
