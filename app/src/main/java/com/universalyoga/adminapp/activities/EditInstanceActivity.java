package com.universalyoga.adminapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import android.view.View;
import com.google.android.material.appbar.MaterialToolbar;
import android.app.TimePickerDialog;
import android.widget.TimePicker;
import java.util.HashMap;
import java.util.Map;
import android.text.Editable;
import android.text.TextWatcher;

public class EditInstanceActivity extends AppCompatActivity {
    private EditText etTeacher, etComments, etAttendees;
    private com.google.android.material.textfield.TextInputEditText etDate;
    private TextView tvCourseName;
    private com.google.android.material.textfield.MaterialAutoCompleteTextView autoCourse;
    private com.google.android.material.textfield.TextInputLayout tilAttendees;
    private Button btnSave, btnCancel;
    private InstanceDao instanceDao;
    private CourseDao courseDao;
    private View progressBar;
    private int instanceId;
    private YogaCourse associatedCourse;
    private String originalDate, originalTeacher, originalComments;
    private int originalAttendees;
    private Map<String, String> errors = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_instance);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        instanceId = getIntent().getIntExtra("instanceId", -1);
        if (instanceId == -1) {
            finish();
            return;
        }

        instanceDao = AppDatabase.getInstance(this).instanceDao();
        courseDao = AppDatabase.getInstance(this).courseDao();

        autoCourse = findViewById(R.id.autoCourse);
        etDate = findViewById(R.id.etDate);
        etTeacher = findViewById(R.id.etTeacher);
        etComments = findViewById(R.id.etComments);
        etAttendees = findViewById(R.id.editAttendees);
        tilAttendees = findViewById(R.id.tilAttendees);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);

        etDate.setOnClickListener(v -> showDatePicker());
        etDate.setFocusable(false);
        etDate.setClickable(true);

        btnSave.setOnClickListener(v -> handleUpdate());
        btnCancel.setOnClickListener(v -> finish());

        setupValidation();
        loadInstanceData();
    }

    private void setupValidation() {
        // Real-time validation on focus change
        etDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !etDate.getText().toString().trim().isEmpty()) {
                validateDate();
            }
        });

        etTeacher.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !etTeacher.getText().toString().trim().isEmpty()) {
                validateTeacher();
            }
        });

        etAttendees.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !etAttendees.getText().toString().trim().isEmpty()) {
                // Validate attendees on focus loss
                String attendeesString = etAttendees.getText().toString().trim();
                if (!attendeesString.isEmpty()) {
                    try {
                        int attendees = Integer.parseInt(attendeesString);
                        if (attendees < 0) {
                            tilAttendees.setError("Attendees cannot be negative");
                        } else if (associatedCourse != null && attendees > associatedCourse.getCapacity()) {
                            tilAttendees.setError("Attendees cannot exceed capacity (" + associatedCourse.getCapacity() + ")");
                        } else {
                            tilAttendees.setError(null);
                        }
                    } catch (NumberFormatException e) {
                        tilAttendees.setError("Invalid attendees number");
                    }
                }
            }
        });

        // Clear errors when user starts typing
        etTeacher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    clearError("teacher");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etAttendees.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    tilAttendees.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateDate() {
        String date = etDate.getText().toString().trim();
        if (date.isEmpty()) {
            // Show error on the date field
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validate date format and day match
        try {
            Calendar selectedCalendar = Calendar.getInstance();
            
            // Try to parse the date in different formats for backward compatibility
            SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.ENGLISH),
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            };
            
            boolean dateParsed = false;
            for (SimpleDateFormat dateFormat : dateFormats) {
                try {
                    selectedCalendar.setTime(dateFormat.parse(date));
                    dateParsed = true;
                    android.util.Log.d("EditInstanceActivity", "Successfully parsed date with format: " + dateFormat.toPattern());
                    break;
                } catch (Exception e) {
                    // Continue to next format
                }
            }
            
            if (!dateParsed) {
                throw new Exception("Unable to parse date in any supported format");
            }
            
            // Get the day name from the selected date
            String selectedDayFull = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(selectedCalendar.getTime());

            android.util.Log.d("EditInstanceActivity", "Validating date: " + date);
            android.util.Log.d("EditInstanceActivity", "Selected day (full): " + selectedDayFull);

            if (associatedCourse != null) {
                String courseDays = associatedCourse.getDaysOfWeek();
                android.util.Log.d("EditInstanceActivity", "Course days: " + courseDays);
                
                boolean match = false;
                if (courseDays != null) {
                    // Split by comma and check each day
                    for (String day : courseDays.split(",")) {
                        String trimmedDay = day.trim();
                        android.util.Log.d("EditInstanceActivity", "Checking course day: '" + trimmedDay + "'");
                        
                        // Check if the selected day matches the course day (both should be full day names)
                        if (selectedDayFull.equalsIgnoreCase(trimmedDay)) {
                            android.util.Log.d("EditInstanceActivity", "Day match found!");
                            match = true;
                            break;
                        }
                    }
                }
                if (!match) {
                    android.util.Log.e("EditInstanceActivity", "No day match found. Course days: " + courseDays + ", Selected: " + selectedDayFull);
                    Toast.makeText(this, "Selected date's day does not match course schedule", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("EditInstanceActivity", "Error validating date: " + e.getMessage(), e);
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }

    private boolean validateTeacher() {
        String teacher = etTeacher.getText().toString().trim();
        if (teacher.isEmpty()) {
            Toast.makeText(this, "Please enter a teacher name", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateForm() {
        // Clear all previous errors first
        clearAllErrors();
        
        boolean isValid = true;
        
        // Validate date
        if (!validateDate()) {
            isValid = false;
        }
        
        // Validate teacher
        if (!validateTeacher()) {
            isValid = false;
        }
        
        // Validate attendees
        String attendeesString = etAttendees.getText().toString().trim();
        if (!attendeesString.isEmpty()) {
            try {
                int attendees = Integer.parseInt(attendeesString);
                if (attendees < 0) {
                    tilAttendees.setError("Attendees cannot be negative");
                    isValid = false;
                } else if (associatedCourse != null && attendees > associatedCourse.getCapacity()) {
                    tilAttendees.setError("Attendees cannot exceed capacity (" + associatedCourse.getCapacity() + ")");
                    isValid = false;
                } else {
                    tilAttendees.setError(null);
                }
            } catch (NumberFormatException e) {
                tilAttendees.setError("Invalid attendees number");
                isValid = false;
            }
        } else {
            tilAttendees.setError(null);
        }
        
        return isValid;
    }

    private void clearError(String fieldName) {
        errors.remove(fieldName);
    }

    private void clearAllErrors() {
        errors.clear();
        // Clear all TextInputLayout errors
        tilAttendees.setError(null);
    }



    private void loadInstanceData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            YogaInstance instance = instanceDao.getById(instanceId);
            if (instance != null) {
                associatedCourse = courseDao.getById(instance.getCourseId());
                runOnUiThread(() -> {
                    if (associatedCourse != null) {
                        // Set the course selection dropdown
                        String courseDisplayText = formatCourseDisplayText(associatedCourse);
                        autoCourse.setText(courseDisplayText, false);
                        
                        // Set the capacity hint
                        updateCapacityHint();
                    }
                    etDate.setText(instance.getDate());
                    etTeacher.setText(instance.getTeacher());
                    etComments.setText(instance.getComments());
                    etAttendees.setText(String.valueOf(instance.getEnrolled()));

                    originalDate = instance.getDate();
                    originalTeacher = instance.getTeacher();
                    originalComments = instance.getComments();
                    originalAttendees = instance.getEnrolled();
                });
            }
        });
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.ENGLISH);
            etDate.setText(dateFormat.format(calendar.getTime()));
        });
        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    private void updateCapacityHint() {
        if (associatedCourse != null && tilAttendees != null) {
            tilAttendees.setHelperText("Max capacity: " + associatedCourse.getCapacity());
        }
    }

    private String formatCourseDisplayText(YogaCourse course) {
        if (course == null) return "";
        
        StringBuilder displayText = new StringBuilder();
        displayText.append(course.getCourseName());
        
        if (course.getDaysOfWeek() != null && !course.getDaysOfWeek().isEmpty()) {
            displayText.append(" - ").append(course.getDaysOfWeek());
        }
        
        if (course.getTime() != null && !course.getTime().isEmpty()) {
            displayText.append(" at ").append(course.getTime());
        }
        
        return displayText.toString();
    }

    

    private void handleUpdate() {
        // Validate the form first
        if (!validateForm()) {
            android.util.Log.e("EditInstanceActivity", "Validation failed - showing errors to user");
            // Show error message but don't close the form
            Toast.makeText(this, "Please fix the errors above before saving", Toast.LENGTH_LONG).show();
            return;
        }

        final String selectedDateString = etDate.getText().toString().trim();
        final String teacher = etTeacher.getText().toString().trim();
        final String comments = etComments.getText().toString().trim();
        final String attendeesString = etAttendees.getText().toString().trim();

        android.util.Log.d("EditInstanceActivity", "handleUpdate - Date: " + selectedDateString + ", Teacher: " + teacher + ", Comments: " + comments + ", Attendees: " + attendeesString);

        // Parse attendees for comparison
        int currentAttendees;
        try {
            currentAttendees = attendeesString.isEmpty() ? 0 : Integer.parseInt(attendeesString);
        } catch (NumberFormatException e) {
            currentAttendees = 0;
        }

        android.util.Log.d("EditInstanceActivity", "Change detection - Original attendees: " + originalAttendees + ", Current attendees: " + currentAttendees);

        if (selectedDateString.equals(originalDate) &&
            teacher.equals(originalTeacher) &&
            comments.equals(originalComments) &&
            currentAttendees == originalAttendees) {
            android.util.Log.d("EditInstanceActivity", "No changes detected - all fields match original values");
            Toast.makeText(this, "No changes detected.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.util.Log.d("EditInstanceActivity", "Changes detected - proceeding with update");
        
        if (associatedCourse == null) {
            android.util.Log.e("EditInstanceActivity", "Associated course is null");
            Toast.makeText(this, "Course information not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Parse attendees
        final int attendees;
        try {
            if (!attendeesString.isEmpty()) {
                attendees = Integer.parseInt(attendeesString);
                if (attendees < 0) {
                    tilAttendees.setError("Attendees cannot be negative");
                    Toast.makeText(this, "Attendees cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (attendees > associatedCourse.getCapacity()) {
                    tilAttendees.setError("Attendees cannot exceed capacity (" + associatedCourse.getCapacity() + ")");
                    Toast.makeText(this, "Attendees cannot exceed capacity (" + associatedCourse.getCapacity() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Clear any previous errors
                tilAttendees.setError(null);
            } else {
                attendees = 0;
            }
        } catch (NumberFormatException e) {
            tilAttendees.setError("Invalid attendees number");
            Toast.makeText(this, "Invalid attendees number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        final String startTime = associatedCourse.getTime();
        final String endTime;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(startTime));
            calendar.add(Calendar.MINUTE, associatedCourse.getDuration());
            endTime = sdf.format(calendar.getTime());
            android.util.Log.d("EditInstanceActivity", "Calculated end time: " + endTime);
        } catch (Exception e) {
            android.util.Log.e("EditInstanceActivity", "Error calculating end time: " + e.getMessage(), e);
            Toast.makeText(this, "Error calculating end time", Toast.LENGTH_SHORT).show();
            return;
        }
        
        final int capacity = associatedCourse.getCapacity();

        android.util.Log.d("EditInstanceActivity", "Current instance - Attendees: " + attendees + ", Capacity: " + capacity);

        // Format date for saving - use EEEE, dd/MM/yyyy format
        try {
            // Parse the date that's already in EEEE, dd/MM/yyyy format
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.ENGLISH);
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.setTime(dateFormat.parse(selectedDateString));
            // Keep the same format for storage
            String formattedDate = selectedDateString; // Already in correct format

            android.util.Log.d("EditInstanceActivity", "Formatted date for saving: " + formattedDate);

            // Use formattedDate for saving
            progressBar.setVisibility(View.VISIBLE);
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    android.util.Log.d("EditInstanceActivity", "Starting database update in background thread");
                    
                    // Get the current instance data on background thread
                    YogaInstance currentInstance = instanceDao.getById(instanceId);
                    if (currentInstance == null) {
                        android.util.Log.e("EditInstanceActivity", "Current instance not found for ID: " + instanceId);
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Instance not found", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                    
                    YogaInstance updatedInstance = new YogaInstance(instanceId, associatedCourse.getId(), formattedDate, teacher, comments, 0, startTime, endTime, attendees, capacity);
                    android.util.Log.d("EditInstanceActivity", "Created updated instance with ID: " + updatedInstance.getId());
                    android.util.Log.d("EditInstanceActivity", "Instance data - CourseID: " + updatedInstance.getCourseId() + 
                                     ", Date: " + updatedInstance.getDate() + 
                                     ", Teacher: " + updatedInstance.getTeacher() + 
                                     ", Attendees: " + updatedInstance.getEnrolled() + 
                                     ", Capacity: " + updatedInstance.getCapacity());
                    
                    int result = instanceDao.update(updatedInstance);
                    android.util.Log.d("EditInstanceActivity", "Database update result: " + result);
                    
                    // Verify the update by reading back the instance
                    YogaInstance verifyInstance = instanceDao.getById(instanceId);
                    if (verifyInstance != null) {
                        android.util.Log.d("EditInstanceActivity", "Verification - Updated instance data: " +
                                         "Date: " + verifyInstance.getDate() + 
                                         ", Teacher: " + verifyInstance.getTeacher() + 
                                         ", Attendees: " + verifyInstance.getEnrolled());
                    } else {
                        android.util.Log.e("EditInstanceActivity", "Verification failed - Could not read back updated instance");
                    }
                    
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (result > 0) {
                            Toast.makeText(this, "Instance updated successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            android.util.Log.e("EditInstanceActivity", "Update returned 0 rows affected");
                            Snackbar.make(findViewById(android.R.id.content), "Update failed! No rows were updated.", Snackbar.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e("EditInstanceActivity", "Error updating instance: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(findViewById(android.R.id.content), "Database error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    });
                }
            });
            return;
        } catch (Exception e) {
            android.util.Log.e("EditInstanceActivity", "Error parsing date: " + e.getMessage(), e);
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
            case Calendar.SUNDAY: return "Sunday";
            default: return "";
        }
    }
}