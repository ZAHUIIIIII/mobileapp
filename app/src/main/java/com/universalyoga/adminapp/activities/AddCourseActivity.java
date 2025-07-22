package com.universalyoga.adminapp.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TimePicker;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import androidx.appcompat.app.AppCompatActivity;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.utils.ValidationUtils;
import com.universalyoga.adminapp.R;
import java.util.Locale;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import com.universalyoga.adminapp.database.AppDatabase;
import android.content.Intent;
import android.widget.Spinner;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.appbar.MaterialToolbar;

public class AddCourseActivity extends AppCompatActivity {

    private EditText courseName, price, capacity, description, duration;
    private EditText time;
    private String selectedDay = ""; // Changed to single String
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private CourseDao courseDao;
    protected TextInputLayout tilCourseName, tilTime, tilCapacity, tilDuration, tilPrice, tilType, tilDescription, tilRoomLocation;
    private MaterialAutoCompleteTextView autoType, autoRoomLocation;
    private View progressBar;
    private TextView selectedDaysText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        courseName = findViewById(R.id.editCourseName);
        time = findViewById(R.id.editTime);
        capacity = findViewById(R.id.editCapacity);
        duration = findViewById(R.id.editDuration);
        price = findViewById(R.id.editPrice);
        autoType = findViewById(R.id.autoType);
        description = findViewById(R.id.editDescription);
        autoRoomLocation = findViewById(R.id.autoRoomLocation);
        selectedDaysText = findViewById(R.id.selectedDaysText);
        progressBar = findViewById(R.id.progressBar);

        tilCourseName = findViewById(R.id.tilCourseName);
        tilTime = findViewById(R.id.tilTime);
        tilCapacity = findViewById(R.id.tilCapacity);
        tilDuration = findViewById(R.id.tilDuration);
        tilPrice = findViewById(R.id.tilPrice);
        tilType = findViewById(R.id.tilType);
        tilDescription = findViewById(R.id.tilDescription);
        tilRoomLocation = findViewById(R.id.tilRoomLocation);

        addClearErrorOnInput(courseName, tilCourseName);
        addClearErrorOnInput(time, tilTime);
        addClearErrorOnInput(capacity, tilCapacity);
        addClearErrorOnInput(duration, tilDuration);
        addClearErrorOnInput(price, tilPrice);
        addClearErrorOnInput(description, tilDescription);

        autoType.setOnItemClickListener((parent, view, position, id) -> tilType.setError(null));
        autoRoomLocation.setOnItemClickListener((parent, view, position, id) -> tilRoomLocation.setError(null));

        courseDao = AppDatabase.getInstance(this).courseDao();

        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);
        btnSave.setOnClickListener(v -> handleSave());
        btnCancel.setOnClickListener(v -> finish());

        time.setOnClickListener(v -> showTimePicker());
        time.setFocusable(false);
        time.setClickable(true);
        duration.setOnClickListener(v -> showDurationPicker());
        duration.setFocusable(false);
        duration.setClickable(true);

        MaterialButton btnSelectDays = findViewById(R.id.btnSelectDays);
        btnSelectDays.setOnClickListener(v -> showDaysOfWeekDialog());

        String[] types = {"Flow Yoga", "Aerial Yoga", "Family Yoga", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types);
        autoType.setAdapter(typeAdapter);

        String[] rooms = {"Room 1", "Room 2", "Room 3", "Room 4", "Room 5"};
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rooms);
        autoRoomLocation.setAdapter(roomAdapter);
    }

    private void showTimePicker() {
        int hour = 10;
        int minute = 0;
        String current = time.getText().toString();
        if (!current.isEmpty() && current.contains(":")) {
            String[] parts = current.split(":");
            try {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
        }
        TimePickerDialog dialog = new TimePickerDialog(this, (TimePicker view, int h, int m) -> {
            String ampm = h >= 12 ? "PM" : "AM";
            int hour12 = h % 12;
            if (hour12 == 0) hour12 = 12;
            String formatted = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, m, ampm);
            time.setText(formatted);
        }, hour, minute, false);
        dialog.show();
    }

    private void showDurationPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Duration (minutes)");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_number_picker, null);
        NumberPicker numberPicker = dialogView.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(60);
        numberPicker.setValue(30);
        builder.setView(dialogView);
        builder.setPositiveButton("OK", (dialog, which) -> {
            int value = numberPicker.getValue();
            duration.setText(value + " min");
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a date")
            .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat fullFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
            String fullDate = fullFormat.format(calendar.getTime());
        });
        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    private void showDaysOfWeekDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_days_of_week, null);
        builder.setView(view);

        ChipGroup chipGroup = view.findViewById(R.id.chipGroupDays);

        final AlertDialog dialog = builder.create();

        // Pre-select the current day if it exists and set click listeners
        for (int i = 0; i < DAYS.length; i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip != null) {
                if (DAYS[i].equals(selectedDay)) {
                    chip.setChecked(true);
                }
                chip.setOnClickListener(v -> {
                    // Uncheck all other chips
                    for (int j = 0; j < chipGroup.getChildCount(); j++) {
                        Chip otherChip = (Chip) chipGroup.getChildAt(j);
                        if (otherChip != chip) {
                            otherChip.setChecked(false);
                        }
                    }
                    // Set the clicked chip as checked
                    chip.setChecked(true);
                    selectedDay = chip.getText().toString();
                    selectedDaysText.setText(selectedDay);
                    dialog.dismiss(); // Dismiss dialog after selection
                });
            }
        }

        dialog.show();
    }

    private void handleSave() {
        if (!validateInputs()) return;
        progressBar.setVisibility(View.VISIBLE);
        try {
            int durationValue = 1;
            String durationText = duration.getText().toString().trim();
            if (durationText.contains(" ")) {
                durationValue = Integer.parseInt(durationText.split(" ")[0]);
            } else {
                try { durationValue = Integer.parseInt(durationText); } catch (Exception ignored) {}
            }
            Intent intent = new Intent(this, ConfirmCourseActivity.class);
            intent.putExtra("courseName", courseName.getText().toString().trim());
            intent.putExtra("time", time.getText().toString().trim());
            intent.putExtra("capacity", capacity.getText().toString().trim());
            intent.putExtra("duration", durationValue);
            intent.putExtra("price", price.getText().toString().trim());
            intent.putExtra("type", autoType.getText().toString().trim());
            intent.putExtra("description", description.getText().toString().trim());
            intent.putExtra("roomLocation", autoRoomLocation.getText().toString().trim());
            intent.putExtra("daysOfWeek", selectedDay);
            progressBar.postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
                startActivity(intent);
            }, 500);
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Snackbar.make(findViewById(android.R.id.content), "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    protected boolean validateInputs() {
        if (ValidationUtils.isEmpty(courseName)) {
            tilCourseName.setError("Course name is required");
            courseName.requestFocus();
            return false;
        }
        if (selectedDay.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please select a day of the week.", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (ValidationUtils.isEmpty(time)) {
            tilTime.setError("Time is required");
            time.requestFocus();
            return false;
        }
        if (ValidationUtils.isEmpty(duration)) {
            tilDuration.setError("Duration is required");
            duration.requestFocus();
            return false;
        }
        if (autoType.getText().toString().trim().isEmpty()) {
            tilType.setError("Type is required");
            autoType.requestFocus();
            return false;
        }
        if (ValidationUtils.isEmpty(capacity)) {
            tilCapacity.setError("Capacity is required");
            capacity.requestFocus();
            return false;
        }
        if (autoRoomLocation.getText().toString().trim().isEmpty()) {
            tilRoomLocation.setError("Room location is required");
            autoRoomLocation.requestFocus();
            return false;
        }
        if (ValidationUtils.isEmpty(price)) {
            tilPrice.setError("Price is required");
            price.requestFocus();
            return false;
        }
        if (!ValidationUtils.isValidNumber(capacity)) {
            tilCapacity.setError("Capacity must be a valid number");
            capacity.requestFocus();
            return false;
        }
        if (!isValidDuration(duration)) {
            tilDuration.setError("Duration must be 1-60");
            duration.requestFocus();
            return false;
        }
        if (!ValidationUtils.isValidNumber(price)) {
            tilPrice.setError("Price must be a valid number");
            price.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isValidDuration(EditText durationField) {
        String durationText = durationField.getText().toString().trim();
        if (durationText.contains(" ")) {
            durationText = durationText.split(" ")[0];
        }
        try {
            int value = Integer.parseInt(durationText);
            return value >= 1 && value <= 60;
        } catch (Exception e) {
            return false;
        }
    }

    private void addClearErrorOnInput(EditText field, TextInputLayout til) {
        field.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (til != null) til.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}