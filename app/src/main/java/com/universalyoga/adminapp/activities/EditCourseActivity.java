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

public class EditCourseActivity extends AppCompatActivity {
    private int id;
    private CourseDao courseDao;
    private EditText courseName, time, capacity, description, duration;
    private EditText price;
    private MaterialAutoCompleteTextView autoType, autoRoomLocation;
    private View progressBar;
    private TextInputLayout tilCourseName, tilTime, tilCapacity, tilDuration, tilPrice, tilType, tilDescription, tilRoomLocation;
    private String fullSelectedDate = null;
    private String selectedDay = ""; // Changed to single String
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private TextView selectedDaysText;

    @Override
    protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_edit_course);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        id = getIntent().getIntExtra("id", -1);
        courseDao = AppDatabase.getInstance(this).courseDao();

        courseName = findViewById(R.id.editCourseName);
        time = findViewById(R.id.editTime);
        capacity = findViewById(R.id.editCapacity);
        duration = findViewById(R.id.editDuration);
        price = findViewById(R.id.editPrice);
        autoType = findViewById(R.id.autoType);
        description = findViewById(R.id.editDescription);
        autoRoomLocation = findViewById(R.id.autoRoomLocation);
        progressBar = findViewById(R.id.progressBar);
        selectedDaysText = findViewById(R.id.selectedDaysText);

        tilCourseName = findViewById(R.id.tilCourseName);
        tilTime = findViewById(R.id.tilTime);
        tilCapacity = findViewById(R.id.tilCapacity);
        tilDuration = findViewById(R.id.tilDuration);
        tilPrice = findViewById(R.id.tilPrice);
        tilType = findViewById(R.id.tilType);
        tilDescription = findViewById(R.id.tilDescription);
        tilRoomLocation = findViewById(R.id.tilRoomLocation);

        

        MaterialButton btnUpdate = findViewById(R.id.btnUpdate);
        MaterialButton btnDelete = findViewById(R.id.btnDelete);
        MaterialButton btnViewInstances = findViewById(R.id.btnViewInstances);
        btnUpdate.setOnClickListener(v -> handleUpdate());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        btnViewInstances.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClassInstanceActivity.class);
            intent.putExtra("courseId", id);
            startActivity(intent);
        });

        duration.setOnClickListener(v -> showDurationPicker());
        duration.setFocusable(false);
        duration.setClickable(true);
        time.setOnClickListener(v -> showTimePicker());
        time.setFocusable(false);
        time.setClickable(true);

        MaterialButton btnSelectDays = findViewById(R.id.btnSelectDays);
        btnSelectDays.setOnClickListener(v -> showDaysOfWeekDialog());

        String[] types = {"Flow Yoga", "Aerial Yoga", "Family Yoga", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types);
        autoType.setAdapter(typeAdapter);
        String[] rooms = {"Room 1", "Room 2", "Room 3", "Room 4", "Room 5"};
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rooms);
        autoRoomLocation.setAdapter(roomAdapter);

        new Thread(() -> {
            YogaCourse course = courseDao.getById(id);
            if (course != null) {
                runOnUiThread(() -> populateFields(course));
            }
        }).start();
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
        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, m) -> {
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
            fullSelectedDate = fullDate;
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
        }
        selectedDaysText.setText(selectedDay);
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
        final String daysOfWeekStr = selectedDay; // Use selectedDay
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
                        Snackbar.make(findViewById(android.R.id.content), "No changes detected.", Snackbar.LENGTH_LONG).show();
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
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                int result = courseDao.update(updatedCourse);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (result > 0) {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Course updated!", Snackbar.LENGTH_SHORT)
                            .setAction("Undo", v -> {
                                new Thread(() -> {
                                    courseDao.update(oldCourse);
                                }).start();
                            });
                        snackbar.addCallback(new com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                finish();
                            }
                        });
                        snackbar.show();
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
            tilCourseName.setError("Course name is required");
            courseName.requestFocus();
            return false;
        }
        if (ValidationUtils.isEmpty(time)) {
            tilTime.setError("Time is required");
            time.requestFocus();
            return false;
        }
        if (ValidationUtils.isEmpty(capacity)) {
            tilCapacity.setError("Capacity is required");
            capacity.requestFocus();
            return false;
        }
        if (ValidationUtils.isEmpty(duration)) {
            tilDuration.setError("Duration is required");
            duration.requestFocus();
            return false;
        }
        if (ValidationUtils.isEmpty(price)) {
            tilPrice.setError("Price is required");
            price.requestFocus();
            return false;
        }
        if (autoType.getText().toString().trim().isEmpty()) {
            tilType.setError("Type is required");
            autoType.requestFocus();
            return false;
        }
        if (autoRoomLocation.getText().toString().trim().isEmpty()) {
            tilRoomLocation.setError("Room location is required");
            autoRoomLocation.requestFocus();
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
        if (selectedDay.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please select a day of the week.", Snackbar.LENGTH_SHORT).show();
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

    

    private void showDeleteConfirmation() {
        progressBar.setVisibility(View.VISIBLE);
        new AlertDialog.Builder(this)
            .setTitle("Delete Course")
            .setMessage("Are you sure you want to delete this course?")
            .setPositiveButton("Delete", (dialog, which) -> {
                new Thread(() -> {
                    try {
                        final YogaCourse course = courseDao.getById(id);
                        int result = courseDao.delete(course);
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (result > 0) {
                                final YogaCourse backup = course;
                                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Course deleted!", Snackbar.LENGTH_SHORT)
                                    .setAction("View", v2 -> {
                                        finish();
                                    })
                                    .setAction("Undo", v -> {
                                        new Thread(() -> {
                                            courseDao.insert(backup);
                                        }).start();
                                    });
                                snackbar.addCallback(new com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                    @Override
                                    public void onDismissed(Snackbar transientBottomBar, int event) {
                                        finish();
                                    }
                                });
                                snackbar.show();
                            } else {
                                Snackbar.make(findViewById(android.R.id.content), "Delete failed!", Snackbar.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Snackbar.make(findViewById(android.R.id.content), "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        });
                    }
                }).start();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    
}