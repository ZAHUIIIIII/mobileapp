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
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.NumberPicker;
import android.content.Intent;
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
    private EditText editDaysOfWeek;
    private boolean[] selectedDays = new boolean[7];
    private static final String[] DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

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
        editDaysOfWeek = findViewById(R.id.editDaysOfWeek);
        editDaysOfWeek.setOnClickListener(v -> showDaysOfWeekDialog());
        editDaysOfWeek.setFocusable(false);
        editDaysOfWeek.setClickable(true);

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

        MaterialButton btnUpdate = findViewById(R.id.btnUpdate);
        MaterialButton btnDelete = findViewById(R.id.btnDelete);
        btnUpdate.setOnClickListener(v -> handleUpdate());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        // Date, time, duration pickers
        duration.setOnClickListener(v -> showDurationPicker());
        duration.setFocusable(false);
        duration.setClickable(true);
        time.setOnClickListener(v -> showTimePicker());
        time.setFocusable(false);
        time.setClickable(true);
        // Remove: date.setOnClickListener(v -> showDatePicker());
        // Remove: date.setFocusable(false);
        // Remove: date.setClickable(true);

        // Set up dropdowns
        String[] types = {"Hatha", "Vinyasa", "Yin", "Restorative", "Ashtanga", "Kundalini", "Power", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types);
        autoType.setAdapter(typeAdapter);
        String[] rooms = {"Room 1", "Room 2", "Room 3", "Room 4", "Room 5"};
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rooms);
        autoRoomLocation.setAdapter(roomAdapter);

        // Load course data
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
            // date.setText(fullDate); // This line is removed as per the edit hint
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
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void populateFields(YogaCourse course) {
        courseName.setText(course.getCourseName());
        // date.setText(course.getDate()); // This line is removed as per the edit hint
        time.setText(course.getTime());
        capacity.setText(String.valueOf(course.getCapacity()));
        duration.setText(course.getDuration() + " min");
        price.setText(String.valueOf(course.getPrice()));
        description.setText(course.getDescription());
        autoType.setText(course.getType(), false);
        autoRoomLocation.setText(course.getRoomLocation(), false);
        if (course.getDaysOfWeek() != null && !course.getDaysOfWeek().isEmpty()) {
            String[] days = course.getDaysOfWeek().split(",");
            for (String day : days) {
                for (int i = 0; i < DAYS.length; i++) {
                    if (DAYS[i].equals(day)) {
                        selectedDays[i] = true;
                        break;
                    }
                }
            }
        }
        editDaysOfWeek.setText(course.getDaysOfWeek());
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
        final String daysOfWeekStr = editDaysOfWeek.getText().toString().trim();
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
                // Compare fields and build change summary
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
                    changes.append("Price: \n  Old: $").append(String.format("%.2f", oldCourse.getPrice())).append("\n  New: $").append(priceStr).append("\n\n");
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
                    new AlertDialog.Builder(this)
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
        // if (ValidationUtils.isEmpty(date)) { // This line is removed as per the edit hint
        //     tilCourseName.setError("Date is required");
        //     date.requestFocus();
        //     return false;
        // }
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
        if (editDaysOfWeek.getText().toString().trim().isEmpty()) {
            tilCourseName.setError("At least one day of the week is required");
            editDaysOfWeek.requestFocus();
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
                til.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
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

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
} 