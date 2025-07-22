package com.universalyoga.adminapp.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TimePicker;
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
    private boolean[] selectedDays = new boolean[7];
    private static final String[] DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private CourseDao courseDao;
    protected TextInputLayout tilCourseName, tilTime, tilCapacity, tilDuration, tilPrice, tilType, tilDescription, tilRoomLocation;
    private MaterialAutoCompleteTextView autoType, autoRoomLocation;
    private View progressBar;
    private EditText editDaysOfWeek;

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
        editDaysOfWeek = findViewById(R.id.editDaysOfWeek);
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
        addClearErrorOnInput(editDaysOfWeek, null); // No specific TextInputLayout for days of week

        // Add error-clearing for dropdowns
        autoType.setOnItemClickListener((parent, view, position, id) -> tilType.setError(null));
        autoRoomLocation.setOnItemClickListener((parent, view, position, id) -> tilRoomLocation.setError(null));

        courseDao = AppDatabase.getInstance(this).courseDao();

        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);
        btnSave.setOnClickListener(v -> handleSave());
        btnCancel.setOnClickListener(v -> finish());

        // Date, time, duration pickers (unchanged)
        time.setOnClickListener(v -> showTimePicker());
        time.setFocusable(false);
        time.setClickable(true);
        duration.setOnClickListener(v -> showDurationPicker());
        duration.setFocusable(false);
        duration.setClickable(true);
        editDaysOfWeek.setOnClickListener(v -> showDaysOfWeekDialog());
        editDaysOfWeek.setFocusable(false);
        editDaysOfWeek.setClickable(true);

        // Set up dropdowns
        String[] types = {"Hatha", "Vinyasa", "Yin", "Restorative", "Ashtanga", "Kundalini", "Power", "Other"};
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
            // fullSelectedDate = fullDate; // Removed
            // editDate.setText(fullDate); // Removed
        });
        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    private void showDaysOfWeekDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Days of Week");
        builder.setMultiChoiceItems(DAYS, selectedDays, (dialog, which, isChecked) -> selectedDays[which] = isChecked);
        builder.setPositiveButton("OK", (dialog, which) -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < DAYS.length; i++) {
                if (selectedDays[i]) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(DAYS[i]);
                }
            }
            editDaysOfWeek.setText(sb.toString());
            Toast.makeText(this, "Selected: " + sb.toString(), Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        android.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
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
            // intent.putExtra("date", fullSelectedDate); // Removed
            intent.putExtra("time", time.getText().toString().trim());
            intent.putExtra("capacity", capacity.getText().toString().trim());
            intent.putExtra("duration", durationValue);
            intent.putExtra("price", price.getText().toString().trim());
            intent.putExtra("type", autoType.getText().toString().trim());
            intent.putExtra("description", description.getText().toString().trim());
            intent.putExtra("roomLocation", autoRoomLocation.getText().toString().trim());
            intent.putExtra("daysOfWeek", editDaysOfWeek.getText().toString().trim());
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
            Toast.makeText(this, "Course name is required", Toast.LENGTH_SHORT).show();
            courseName.requestFocus();
            return false;
        }
        if (editDaysOfWeek.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select at least one day of the week.", Toast.LENGTH_SHORT).show();
            editDaysOfWeek.requestFocus();
            return false;
        }
        if (ValidationUtils.isEmpty(time)) {
            Toast.makeText(this, "Time is required", Toast.LENGTH_SHORT).show();
            time.requestFocus();
            return false;
        }
        if (ValidationUtils.isEmpty(duration)) {
            Toast.makeText(this, "Duration is required", Toast.LENGTH_SHORT).show();
            duration.requestFocus();
            return false;
        }
        if (autoType.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Type is required", Toast.LENGTH_SHORT).show();
            autoType.requestFocus();
            return false;
        }
        if (ValidationUtils.isEmpty(capacity)) {
            Toast.makeText(this, "Capacity is required", Toast.LENGTH_SHORT).show();
            capacity.requestFocus();
            return false;
        }
        if (autoRoomLocation.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Room location is required", Toast.LENGTH_SHORT).show();
            autoRoomLocation.requestFocus();
            return false;
        }
        if (ValidationUtils.isEmpty(price)) {
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
            price.requestFocus();
            return false;
        }
        if (!ValidationUtils.isValidNumber(capacity)) {
            Toast.makeText(this, "Capacity must be a valid number", Toast.LENGTH_SHORT).show();
            capacity.requestFocus();
            return false;
        }
        if (!isValidDuration(duration)) {
            Toast.makeText(this, "Duration must be 1-60", Toast.LENGTH_SHORT).show();
            duration.requestFocus();
            return false;
        }
        if (!ValidationUtils.isValidNumber(price)) {
            Toast.makeText(this, "Price must be a valid number", Toast.LENGTH_SHORT).show();
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
