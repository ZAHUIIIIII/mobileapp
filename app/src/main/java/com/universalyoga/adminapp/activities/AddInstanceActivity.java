package com.universalyoga.adminapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import com.universalyoga.adminapp.repository.InstanceRepository;
import java.util.List;
import java.util.concurrent.Executors;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import com.google.android.material.appbar.MaterialToolbar;
import android.widget.ArrayAdapter;
import android.app.AlertDialog;
import android.widget.AutoCompleteTextView;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import java.util.Date;

public class AddInstanceActivity extends AppCompatActivity {
    private TextInputEditText etDate, etTeacher, etComments, editAttendees;
    private AutoCompleteTextView autoCourse;
    private com.google.android.material.textfield.TextInputLayout tilAttendees;
    private MaterialButton btnSubmit, btnCancel;
    private CourseDao courseDao;
    private InstanceRepository instanceRepository;
    private View progressBar;
    private List<YogaCourse> allCourses;
    private YogaCourse selectedCourse;
    private Map<String, String> errors = new HashMap<>();
    private boolean isSubmitting = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_instance);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (!isSubmitting) {
                finish();
            }
        });
        
        courseDao = AppDatabase.getInstance(this).courseDao();
        instanceRepository = new InstanceRepository(this);
        
        initializeViews();
        setupValidation();
        loadCourses();
        observeRepository();
    }

    private void initializeViews() {
        etDate = findViewById(R.id.etDate);
        etTeacher = findViewById(R.id.etTeacher);
        etComments = findViewById(R.id.etComments);
        editAttendees = findViewById(R.id.editAttendees);
        tilAttendees = findViewById(R.id.tilAttendees);
        autoCourse = findViewById(R.id.autoCourse);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);

        // Set up click listeners
        etDate.setOnClickListener(v -> showDatePicker());
        btnSubmit.setOnClickListener(v -> handleSave());
        btnCancel.setOnClickListener(v -> {
            if (!isSubmitting) {
                finish();
            }
        });

        // Set default values
        editAttendees.setText("0");
    }

    private void observeRepository() {
        // Observe loading state
        instanceRepository.getIsLoading().observe(this, isLoading -> {
            showLoading(isLoading);
        });

        // Observe error messages
        instanceRepository.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showSnackbar(error);
            }
        });
    }

    private void setupValidation() {
        // Real-time validation on focus change
        autoCourse.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !autoCourse.getText().toString().trim().isEmpty()) {
                validateCourse();
            }
        });

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

        editAttendees.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !editAttendees.getText().toString().trim().isEmpty()) {
                validateAttendees();
            }
        });

        // Clear errors when user starts typing
        etTeacher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearFieldError("teacher");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editAttendees.addTextChangedListener(new TextWatcher() {
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

    private boolean validateCourse() {
        if (selectedCourse == null) {
            showFieldError("course", "Course selection is required");
            return false;
        }
        clearFieldError("course");
        return true;
    }

    private boolean validateDate() {
        String date = etDate.getText().toString().trim();
        if (date.isEmpty()) {
            showFieldError("date", "Date is required");
            return false;
        }
        
        // Validate date format and day match
        try {
            // Try to parse the date in different formats for backward compatibility
            SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.ENGLISH),
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            };
            
            Calendar selectedCalendar = Calendar.getInstance();
            boolean dateParsed = false;
            
            for (SimpleDateFormat dateFormat : dateFormats) {
                try {
                    selectedCalendar.setTime(dateFormat.parse(date));
                    dateParsed = true;
                    break;
                } catch (Exception e) {
                    // Continue to next format
                }
            }
            
            if (!dateParsed) {
                throw new Exception("Unable to parse date in any supported format");
            }
            
            // Check if date is in the past
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            if (selectedCalendar.before(today)) {
                showFieldError("date", "Cannot schedule instances in the past");
                return false;
            }
            
            // Validate date matches course schedule
            if (selectedCourse != null) {
                if (!validateDateMatchesCourseSchedule(selectedCalendar, selectedCourse)) {
                    showFieldError("date", "Selected date's day does not match course schedule");
                    return false;
                }
            }
        } catch (Exception e) {
            showFieldError("date", "Invalid date format");
            return false;
        }
        
        clearFieldError("date");
        return true;
    }
    
    private boolean validateDateMatchesCourseSchedule(Calendar selectedCalendar, YogaCourse course) {
        if (course.getDaysOfWeek() == null || course.getDaysOfWeek().isEmpty()) {
            return false; // No days set for course
        }
        
        // Get the day of week from the selected date
        String selectedDay = getDayOfWeekName(selectedCalendar.get(Calendar.DAY_OF_WEEK));
        
        // Get course days and split them
        String[] courseDays = course.getDaysOfWeek().split(",");
        
        // Check if selected day matches any of the course days
        for (String courseDay : courseDays) {
            if (selectedDay.equalsIgnoreCase(courseDay.trim())) {
                return true; // Day matches
            }
        }
        
        return false; // No match found
    }
    
    private String getDayOfWeekName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            default:
                return "";
        }
    }

    private boolean validateTeacher() {
        String teacher = etTeacher.getText().toString().trim();
        if (teacher.isEmpty()) {
            showFieldError("teacher", "Teacher is required");
            return false;
        }
        if (teacher.length() < 2) {
            showFieldError("teacher", "Teacher name must be at least 2 characters");
            return false;
        }
        clearFieldError("teacher");
        return true;
    }

    private boolean validateAttendees() {
        String attendees = editAttendees.getText().toString().trim();
        if (attendees.isEmpty()) {
            tilAttendees.setError("Number of attendees is required");
            return false;
        }
        
        try {
            int attendeeCount = Integer.parseInt(attendees);
            if (attendeeCount < 0) {
                tilAttendees.setError("Attendees cannot be negative");
                return false;
            }
            if (selectedCourse != null && attendeeCount > selectedCourse.getCapacity()) {
                tilAttendees.setError("Attendees cannot exceed course capacity (" + selectedCourse.getCapacity() + ")");
                return false;
            }
            tilAttendees.setError(null);
        } catch (NumberFormatException e) {
            tilAttendees.setError("Please enter a valid number");
            return false;
        }
        
        return true;
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // Validate each field in order
        if (!validateCourse()) isValid = false;
        if (!validateDate()) isValid = false;
        if (!validateTeacher()) isValid = false;
        if (!validateAttendees()) isValid = false;
        
        return isValid;
    }

    private void showFieldError(String fieldName, String errorMessage) {
        errors.put(fieldName, errorMessage);
        showSnackbar(errorMessage);
    }

    private void clearFieldError(String fieldName) {
        errors.remove(fieldName);
    }

    private void clearAllErrors() {
        errors.clear();
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void loadCourses() {
        showLoading(true);
        Executors.newSingleThreadExecutor().execute(() -> {
            allCourses = courseDao.getAll();
            runOnUiThread(() -> {
                showLoading(false);
                if (allCourses.isEmpty()) {
                    showSnackbar("No courses available. Please add courses first.");
                    btnSubmit.setEnabled(false);
                    return;
                }
                
                // Create custom adapter with formatted display text
                ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line) {
                    @Override
                    public int getCount() {
                        return allCourses.size();
                    }
                    
                    @Override
                    public String getItem(int position) {
                        YogaCourse course = allCourses.get(position);
                        return formatCourseDisplayText(course);
                    }
                    
                    @Override
                    public long getItemId(int position) {
                        return position;
                    }
                };
                
                autoCourse.setAdapter(courseAdapter);
                autoCourse.setOnItemClickListener((parent, view, position, id) -> {
                    selectedCourse = allCourses.get(position);
                    updateCourseInfo();
                    clearFieldError("course");
                });
            });
        });
    }
    
    private String formatCourseDisplayText(YogaCourse course) {
        StringBuilder displayText = new StringBuilder();
        
        // Add class type
        if (course.getType() != null && !course.getType().isEmpty()) {
            displayText.append(course.getType());
        }
        
        // Add days of week
        if (course.getDaysOfWeek() != null && !course.getDaysOfWeek().isEmpty()) {
            String days = course.getDaysOfWeek();
            String[] dayArray = days.split(",");
            StringBuilder compactDays = new StringBuilder();
            for (int i = 0; i < dayArray.length; i++) {
                String day = dayArray[i].trim();
                if (day.length() >= 3) {
                    compactDays.append(day.substring(0, 3));
                } else {
                    compactDays.append(day);
                }
                if (i < dayArray.length - 1) {
                    compactDays.append(", ");
                }
            }
            displayText.append(" - ").append(compactDays.toString());
        }
        
        // Add time
        if (course.getTime() != null && !course.getTime().isEmpty()) {
            displayText.append(" at ").append(course.getTime());
        }
        
        return displayText.toString();
    }

    private void updateCourseInfo() {
        if (selectedCourse != null) {
            updateCapacityHint();
        }
    }

    private void updateCapacityHint() {
        if (selectedCourse != null && tilAttendees != null) {
            tilAttendees.setHelperText("Max capacity: " + selectedCourse.getCapacity());
        }
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Instance Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            
            // Use EEEE, dd/MM/yyyy format
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.ENGLISH);
            String formattedDate = dateFormat.format(calendar.getTime());
            etDate.setText(formattedDate);
            
            // Auto-validate date when selected
            validateDate();
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void handleSave() {
        if (isSubmitting) return;
        
        if (!validateForm()) {
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Instance Creation")
                .setMessage("Are you sure you want to create this class instance?")
                .setPositiveButton("Create", (dialog, which) -> {
                    saveInstance();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveInstance() {
        String date = etDate.getText().toString().trim();
        String teacher = etTeacher.getText().toString().trim();
        String comments = etComments.getText().toString().trim();
        String attendees = editAttendees.getText().toString().trim();

        // Create instance object
        YogaInstance instance = new YogaInstance();
        instance.setCourseId(selectedCourse.getId());
        instance.setDate(date);
        instance.setTeacher(teacher);
        instance.setComments(comments.isEmpty() ? null : comments);
        instance.setEnrolled(Integer.parseInt(attendees));
        instance.setCapacity(selectedCourse.getCapacity());
        instance.setStartTime(selectedCourse.getTime());
        instance.setEndTime(calculateEndTime(selectedCourse.getTime(), selectedCourse.getDuration()));

        // Use repository to save
        instanceRepository.addInstance(instance, new InstanceRepository.OnInstanceOperationCallback() {
            @Override
            public void onSuccess(YogaInstance savedInstance) {
                runOnUiThread(() -> {
                    showSnackbar("Instance created successfully!");
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showSnackbar("Error creating instance: " + error);
                });
            }
        });
    }

    private String calculateEndTime(String startTime, int durationMinutes) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date start = timeFormat.parse(startTime);
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            calendar.add(Calendar.MINUTE, durationMinutes);
            
            return timeFormat.format(calendar.getTime());
        } catch (Exception e) {
            return startTime; // Fallback to start time if parsing fails
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
        btnCancel.setEnabled(!show);
        isSubmitting = show;
    }

    @Override
    public void onBackPressed() {
        if (!isSubmitting) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (instanceRepository != null) {
            instanceRepository.cleanup();
        }
    }
}