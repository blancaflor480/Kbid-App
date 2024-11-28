package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.userdb.User;
import com.example.myapplication.database.userdb.UserDao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
public class ChildAgeActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerDay, spinnerYear;
    private Button buttonContinue;
    private AppDatabase db;
    private TextView validationAge;
    private UserDao userDao;
    private User currentUser;

    private ArrayList<String> months = new ArrayList<>();
    private ArrayList<String> days = new ArrayList<>();
    private ArrayList<String> years = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_age);

        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerDay = findViewById(R.id.spinner_day);
        spinnerYear = findViewById(R.id.spinner_year);
        buttonContinue = findViewById(R.id.buttonContinue);
        validationAge = findViewById(R.id.validationage);

        // Initialize the database and DAO
        db = AppDatabase.getDatabase(this);
        userDao = db.userDao();

        // Set up spinners
        setupSpinners();

        // Set initial state of the button
        buttonContinue.setEnabled(false);
        buttonContinue.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_disbaled));
        buttonContinue.setTextColor(getResources().getColor(R.color.black));

        // Load user data
        loadSavedUser();

        // Set OnClickListener to the button
        buttonContinue.setOnClickListener(v -> {
            String childBirthday = spinnerMonth.getSelectedItem() + "-" + spinnerDay.getSelectedItem() + "-" + spinnerYear.getSelectedItem();
            saveOrUpdateUser(childBirthday);
            proceedToNextActivity();
        });

        // Add OnItemSelectedListener to each spinner to enable the button when all selections are made
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                validateSpinners(); // Validate spinners and enable the button
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        };

        spinnerMonth.setOnItemSelectedListener(itemSelectedListener);
        spinnerDay.setOnItemSelectedListener(itemSelectedListener);
        spinnerYear.setOnItemSelectedListener(itemSelectedListener);
    }

    private void setupSpinners() {
        // Setup Months
        String[] monthArray = {"Select", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, monthArray);
        monthAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerMonth.setAdapter(monthAdapter);

        // Setup Days
        String[] dayArray = {"Select "};
        for (int i = 1; i <= 31; i++) {
            dayArray = addElement(dayArray, String.valueOf(i));
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, dayArray);
        dayAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerDay.setAdapter(dayAdapter);

        // Setup Years
        List<String> years = new ArrayList<>();
        years.add("Select");
        for (int i = 2012; i <= 2018; i++) {
            years.add(String.valueOf(i));
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, years);
        yearAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerYear.setAdapter(yearAdapter);
    }

    private String[] addElement(String[] array, String newElement) {
        String[] newArray = new String[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = newElement;
        return newArray;
    }

    private void validateSpinners() {
        // Ensure all spinners have valid selections
        if (spinnerMonth.getSelectedItemPosition() > 0 &&
                spinnerDay.getSelectedItemPosition() > 0 &&
                spinnerYear.getSelectedItemPosition() > 0) {

            // Calculate age based on selected date
            int selectedYear = Integer.parseInt(spinnerYear.getSelectedItem().toString());
            int selectedMonth = spinnerMonth.getSelectedItemPosition() - 1; // Adjusting to zero-based month index
            int selectedDay = Integer.parseInt(spinnerDay.getSelectedItem().toString());

            int age = calculateAge(selectedYear, selectedMonth, selectedDay);

            // Update validation message and enable button if the age is 12 or under
            if (age <= 12) {
                validationAge.setText("Age " + age + " is valid.");
                buttonContinue.setEnabled(true);
                buttonContinue.setBackgroundResource(R.drawable.btn_getstarted);
                buttonContinue.setTextColor(Color.WHITE);
            } else {
                validationAge.setText("Age " + age + " is invalid.");
                buttonContinue.setEnabled(false);
                buttonContinue.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_disbaled));
                buttonContinue.setTextColor(getResources().getColor(R.color.black));
            }
            validationAge.setVisibility(View.VISIBLE);

        } else {
            // Reset the button and validation message if selections are incomplete

        }
    }

    private int calculateAge(int year, int month, int day) {
        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();
        birthDate.set(year, month, day);

        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    private void saveOrUpdateUser(String childBirthday) {
        AsyncTask.execute(() -> {
            if (currentUser != null) {
                // User already exists, update with new birthday
                currentUser.setChildBirthday(childBirthday);
                userDao.updateUser(currentUser);
            } else {
                // No user exists, create a new one with default name and provided birthday
                byte[] defaultAvatarImage = null; // Replace with actual byte array if you have an image
                String email = "No Bind";
                String controlid = "";
                currentUser = new User("Default Name", childBirthday, "Default Avatar", R.drawable.lion, defaultAvatarImage, email, controlid);
                userDao.insert(currentUser);
            }
        });
    }

    private void loadSavedUser() {
        AsyncTask.execute(() -> {
            currentUser = userDao.getFirstUser();
            if (currentUser != null) {
                runOnUiThread(() -> {
                    // Pre-select birthday values if user data is available
                    String[] birthdayParts = currentUser.getChildBirthday().split("-");
                    if (birthdayParts.length == 3) {
                        // Assuming the spinner arrays are in the same order as the birthday string
                        String month = birthdayParts[0]; // Get month
                        String day = birthdayParts[1];   // Get day
                        String year = birthdayParts[2];  // Get year
                        setSelectedItem(spinnerMonth, month);
                        setSelectedItem(spinnerDay, day);
                        setSelectedItem(spinnerYear, year);

                    }
                });
            }
        });
    }

    private void setSelectedItem(Spinner spinner, String item) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int position = adapter.getPosition(item);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

    private void proceedToNextActivity() {
        // Check if user birthday is not set and return to this activity if not
        if (currentUser == null || currentUser.getChildBirthday() == null || currentUser.getChildBirthday().isEmpty()) {
            return; // Stay in ChildAgeActivity
        }
        Intent intent = new Intent(ChildAgeActivity.this, AvatarActivity.class);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(intent);
    }
}
