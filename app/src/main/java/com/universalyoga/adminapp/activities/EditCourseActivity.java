package com.universalyoga.adminapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.utils.ValidationUtils;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import android.view.View;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import java.util.Locale;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.widget.NumberPicker;
import android.content.Intent;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.lifecycle.Observer;

public class EditCourseActivity extends AppCompatActivity {
    private int id;
    private CourseDao courseDao;
    private EditText courseName, time, capacity, description, duration, etDaysOfWeek;
    private EditText price;
    private MaterialAutoCompleteTextView autoType, autoRoomLocation;
    private View progressBar;
    private TextInputLayout tilCourseName, tilTime, tilCapacity, tilDuration, tilPrice, tilType, tilDescription, tilRoomLocation, tilDaysOfWeek;
    private String selectedDay = ""; // Changed to single String
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private String originalCourseName, originalTime, originalCapacity, originalDuration, originalPrice, originalType, originalDescription, originalRoomLocation, originalDaysOfWeek;

    @Override
    protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_edit_course);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        id = getIntent().getIntExtra("id", -1);
        courseDao = AppDatabase.getInstance(this).courseDao();
        progressBar = findViewById(R.id.progressBar);
        if (progressBar == null) {
            android.util.Log.e("EditCourseActivity", "progressBar is null after findViewById!");
        }
        progressBar.setVisibility(View.VISIBLE);

        // Use LiveData to observe the course and update UI when loaded
        courseDao.getByIdLive(id).observe(this, new Observer<YogaCourse>() {
            @Override
            public void onChanged(YogaCourse course) {
                if (course != null) {
                    populateFields(course);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        courseName = findViewById(R.id.editCourseName);
        time = findViewById(R.id.editTime);
        capacity = findViewById(R.id.editCapacity);
        duration = findViewById(R.id.editDuration);
        price = findViewById(R.id.editPrice);
        autoType = findViewById(R.id.autoType);
        description = findViewById(R.id.editDescription);
        autoRoomLocation = findViewById(R.id.autoRoomLocation);
        etDaysOfWeek = findViewById(R.id.etDaysOfWeek);

        tilCourseName = findViewById(R.id.tilCourseName);
        tilTime = findViewById(R.id.tilTime);
        tilCapacity = findViewById(R.id.tilCapacity);
        tilDuration = findViewById(R.id.tilDuration);
        tilPrice = findViewById(R.id.tilPrice);
        tilType = findViewById(R.id.tilType);
        tilDescription = findViewById(R.id.tilDescription);
        tilRoomLocation = findViewById(R.id.tilRoomLocation);
        tilDaysOfWeek = findViewById(R.id.tilDaysOfWeek);

        addClearErrorOnInput(courseName, tilCourseName);
        addClearErrorOnInput(time, tilTime);
        addClearErrorOnInput(capacity, tilCapacity);
        addClearErrorOnInput(duration, tilDuration);
        addClearErrorOnInput(price, tilPrice);
        addClearErrorOnInput(description, tilDescription);
        addClearErrorOnInput(etDaysOfWeek, tilDaysOfWeek);

        autoType.setOnItemClickListener((parent, view, position, id) -> tilType.setError(null));
        autoRoomLocation.setOnItemClickListener((parent, view, position, id) -> tilRoomLocation.setError(null));

        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);
        MaterialButton btnViewInstances = findViewById(R.id.btnViewInstances);
        btnSave.setOnClickListener(v -> handleUpdate());
        btnCancel.setOnClickListener(v -> finish());
        btnViewInstances.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClassInstanceActivity.class);
            intent.putExtra("courseId", id);
            startActivity(intent);
        });

        duration.setOnClickListener(v -> showDurationPicker());
        duration.setFocusable(false);
        duration.setClickable(true);
        time.setOnClickListener(v -> showTimePicker(time));
        time.setFocusable(false);
        time.setClickable(true);

        etDaysOfWeek.setOnClickListener(v -> showDaysOfWeekDialog());
        etDaysOfWeek.setFocusable(false);
        etDaysOfWeek.setClickable(true);

        String[] types = {"Flow Yoga", "Aerial Yoga", "Family Yoga", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types);
        autoType.setAdapter(typeAdapter);
        String[] rooms = {"Room 1", "Room 2", "Room 3", "Room 4", "Room 5"};
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rooms);
        autoRoomLocation.setAdapter(roomAdapter);
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

    private void showTimePicker(final EditText timeField) {
        int hour = 10;
        int minute = 0;
        String current = timeField.getText().toString();
        if (!current.isEmpty() && current.contains(":")) {
            String[] parts = current.split(":");
            try {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
        }
        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, m) -> {
            String ampm = h >= 12 ? "PM" : "AM";
            int hour12 = h % 12;
            if (hour12 == 0) hour12 = 12;
            String formatted = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, m, ampm);
            timeField.setText(formatted);
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
                    String chipText = chip.getText().toString();
                    String abbr = chipText.length() > 3 ? chipText.substring(0, 1).toUpperCase() + chipText.substring(1, 3).toLowerCase() : chipText;
                    selectedDay = abbr;
                    etDaysOfWeek.setText(abbr);
                    dialog.dismiss(); // Dismiss dialog after selection
                });
            }
        }

        dialog.show();
    }

    private void populateFields(YogaCourse course) {
        courseName.setText(course.getCourseName());
        time.setText(course.getTime());
        capacity.setText(String.valueOf(course.getCapacity()));
        duration.setText(course.getDuration() + " min");
        price.setText(String.valueOf(course.getPrice()));
        description.setText(course.getDescription());
        autoType.setText(course.getType(), false);
        autoRoomLocation.setText(course.getRoomLocation(), false);
        if (course.getDaysOfWeek() != null && !course.getDaysOfWeek().isEmpty()) {
            selectedDay = course.getDaysOfWeek();
            etDaysOfWeek.setText(selectedDay);
        }

        originalCourseName = course.getCourseName();
        originalTime = course.getTime();
        originalCapacity = String.valueOf(course.getCapacity());
        originalDuration = String.valueOf(course.getDuration()) + " min";
        originalPrice = String.valueOf(course.getPrice());
        originalType = course.getType();
        originalDescription = course.getDescription();
        originalRoomLocation = course.getRoomLocation();
        originalDaysOfWeek = course.getDaysOfWeek();
    }

    private void handleUpdate() {
        if (!validateInputs()) return;
        progressBar.setVisibility(View.VISIBLE);
        final String courseNameStr = courseName.getText().toString().trim();
        final String timeStr = time.getText().toString().trim();
        final String capacityStr = capacity.getText().toString().trim();
        final String durationStr = duration.getText().toString().trim();
        final String priceStr = price.getText().toString().trim();
        final String typeStr = autoType.getText().toString().trim();
        final String descStr = description.getText().toString().trim();
        final String roomStr = autoRoomLocation.getText().toString().trim();
        final String daysOfWeekStr = etDaysOfWeek.getText().toString().trim();
        final int courseId = id;
        new Thread(() -> {
            try {
                int durationValue = 1;
                if (durationStr.contains(" ")) {
                    durationValue = Integer.parseInt(durationStr.split(" ")[0]);
                } else {
                    try { durationValue = Integer.parseInt(durationStr); } catch (Exception ignored) {}
                }
                final YogaCourse oldCourse = courseDao.getById(courseId);
                YogaCourse updatedCourse = new YogaCourse(
                    courseId,
                    courseNameStr,
                    daysOfWeekStr,
                    timeStr,
                    Integer.parseInt(capacityStr),
                    durationValue,
                    Double.parseDouble(priceStr),
                    typeStr,
                    descStr,
                    roomStr,
                    0 // syncStatus
                );
                StringBuilder changes = new StringBuilder();
                if (!oldCourse.getCourseName().equals(courseNameStr))
                    changes.append("Course Name: \n  Old: ").append(oldCourse.getCourseName()).append("\n  New: ").append(courseNameStr).append("\n\n");
                if (!oldCourse.getDaysOfWeek().equals(daysOfWeekStr))
                    changes.append("Days of Week: \n  Old: ").append(oldCourse.getDaysOfWeek()).append("\n  New: ").append(daysOfWeekStr).append("\n\n");
                if (!oldCourse.getTime().equals(timeStr))
                    changes.append("Time: \n  Old: ").append(oldCourse.getTime()).append("\n  New: ").append(timeStr).append("\n\n");
                if (oldCourse.getCapacity() != Integer.parseInt(capacityStr))
                    changes.append("Capacity: \n  Old: ").append(oldCourse.getCapacity()).append("\n  New: ").append(capacityStr).append("\n\n");
                if (oldCourse.getDuration() != durationValue)
                    changes.append("Duration: \n  Old: ").append(oldCourse.getDuration()).append("\n  New: ").append(durationValue).append("\n\n");
                if (Double.compare(oldCourse.getPrice(), Double.parseDouble(priceStr)) != 0)
                    changes.append("Price: \n  Old: £").append(String.format("%.2f", oldCourse.getPrice())).append("\n  New: £").append(priceStr).append("\n\n");
                if (!oldCourse.getType().equals(typeStr))
                    changes.append("Type: \n  Old: ").append(oldCourse.getType()).append("\n  New: ").append(typeStr).append("\n\n");
                if (!oldCourse.getDescription().equals(descStr))
                    changes.append("Description: \n  Old: ").append(oldCourse.getDescription()).append("\n  New: ").append(descStr).append("\n\n");
                if (!oldCourse.getRoomLocation().equals(roomStr))
                    changes.append("Room Location: \n  Old: ").append(oldCourse.getRoomLocation()).append("\n  New: ").append(roomStr).append("\n\n");
                if (changes.length() == 0) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(EditCourseActivity.this, "No changes detected.", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    new AlertDialog.Builder(EditCourseActivity.this)
                        .setTitle("Confirm Changes")
                        .setMessage("You are about to update the following fields:\n\n" + changes.toString() + "Proceed?")
                        .setPositiveButton("Update", (dialog, which) -> actuallyUpdateCourse(updatedCourse, oldCourse))
                        .setNegativeButton("Cancel", null)
                        .show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content), "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void actuallyUpdateCourse(YogaCourse updatedCourse, YogaCourse oldCourse) {
        if (updatedCourse.getId() == 0) {
            android.util.Log.e("EditCourseActivity", "Attempting to update a course with id=0! This will not update the correct row.");
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(findViewById(android.R.id.content), "Error: Course ID is 0. Update aborted.", Snackbar.LENGTH_LONG).show();
            });
            return;
        }
        android.util.Log.i("EditCourseActivity", "Updating course with id=" + updatedCourse.getId());
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                int result = courseDao.update(updatedCourse);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (result > 0) {
                        com.universalyoga.adminapp.utils.ToastHelper.pendingToastMessage = "Course updated successfully!";
                        finish();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Update failed!", Snackbar.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content), "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    protected boolean validateInputs() {
        if (ValidationUtils.isEmpty(courseName)) {
            Toast.makeText(this, "Course name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ValidationUtils.isEmpty(time)) {
            Toast.makeText(this, "Time is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ValidationUtils.isEmpty(capacity)) {
            Toast.makeText(this, "Capacity is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ValidationUtils.isEmpty(duration)) {
            Toast.makeText(this, "Duration is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ValidationUtils.isEmpty(price)) {
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (autoType.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Type is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (autoRoomLocation.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Room location is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!ValidationUtils.isValidNumber(capacity)) {
            Toast.makeText(this, "Capacity must be a valid number", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isValidDuration(duration)) {
            Toast.makeText(this, "Duration must be 1-60", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!ValidationUtils.isValidNumber(price)) {
            Toast.makeText(this, "Price must be a valid number", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etDaysOfWeek.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select a day of the week.", Toast.LENGTH_SHORT).show();
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

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}