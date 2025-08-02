package com.universalyoga.adminapp.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TimePicker;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import androidx.appcompat.app.AppCompatActivity;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.utils.ValidationUtils;
import com.universalyoga.adminapp.utils.ToastHelper;
import com.universalyoga.adminapp.R;
import java.util.Locale;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import com.universalyoga.adminapp.database.AppDatabase;
import android.content.Intent;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import java.util.HashMap;
import java.util.Map;
import com.universalyoga.adminapp.services.AutoSyncService;

public class AddCourseActivity extends AppCompatActivity {

    // Form fields
    private MaterialAutoCompleteTextView spinnerDayOfWeek, spinnerType, spinnerDifficulty;
    private EditText editTime, editCapacity, editDuration, editPrice, editRoomLocation, editInstructor, editDescription;
    
    // TextInputLayouts for validationf
    private TextInputLayout tilDayOfWeek, tilTime, tilCapacity, tilDuration, tilPrice, tilType, tilRoomLocation, tilInstructor, tilDescription, tilDifficulty;
    
    // UI elements
    private View progressBar;
    private MaterialCardView cardError;
    private TextView tvErrorMessage;
    private MaterialButton btnSave, btnCancel;
    
    // Data
    private CourseDao courseDao;
    private Map<String, String> errors = new HashMap<>();
    private AutoSyncService autoSyncService;
    
    // Constants
    private static final String[] DAYS_OF_WEEK = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private static final String[] CLASS_TYPES = {"Flow Yoga", "Aerial Yoga", "Family Yoga", "Hot Yoga", "Restorative Yoga", "Vinyasa Yoga", "Hatha Yoga", "Yin Yoga"};
    private static final String[] DIFFICULTY_LEVELS = {"Beginner", "Intermediate", "Advanced", "All Levels"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        initializeViews();
        setupToolbar();
        setupDropdowns();
        setupValidation();
        setupClickListeners();
        
        courseDao = AppDatabase.getInstance(this).courseDao();
        autoSyncService = new AutoSyncService(this);
    }
    
    private void initializeViews() {
        // Initialize form fields
        spinnerDayOfWeek = findViewById(R.id.spinnerDayOfWeek);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        editTime = findViewById(R.id.editTime);
        editCapacity = findViewById(R.id.editCapacity);
        editDuration = findViewById(R.id.editDuration);
        editPrice = findViewById(R.id.editPrice);
        editRoomLocation = findViewById(R.id.editRoomLocation);
        editInstructor = findViewById(R.id.editInstructor);
        editDescription = findViewById(R.id.editDescription);
        
        // Initialize TextInputLayouts
        tilDayOfWeek = findViewById(R.id.tilDayOfWeek);
        tilTime = findViewById(R.id.tilTime);
        tilCapacity = findViewById(R.id.tilCapacity);
        tilDuration = findViewById(R.id.tilDuration);
        tilPrice = findViewById(R.id.tilPrice);
        tilType = findViewById(R.id.tilType);
        tilRoomLocation = findViewById(R.id.tilRoomLocation);
        tilInstructor = findViewById(R.id.tilInstructor);
        tilDescription = findViewById(R.id.tilDescription);
        tilDifficulty = findViewById(R.id.tilDifficulty);
        
        // Initialize UI elements
        progressBar = findViewById(R.id.progressBar);
        cardError = findViewById(R.id.cardError);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }
    
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupDropdowns() {
        // Day of week dropdown
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, DAYS_OF_WEEK);
        spinnerDayOfWeek.setAdapter(dayAdapter);
        spinnerDayOfWeek.setOnItemClickListener((parent, view, position, id) -> {
            clearError("dayOfWeek");
        });
        
        // Class type dropdown
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, CLASS_TYPES);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setOnItemClickListener((parent, view, position, id) -> {
            clearError("type");
        });
        
        // Difficulty dropdown
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, DIFFICULTY_LEVELS);
        spinnerDifficulty.setAdapter(difficultyAdapter);
        spinnerDifficulty.setOnItemClickListener((parent, view, position, id) -> {
            clearError("difficulty");
        });
    }
    
    private void setupValidation() {
        // Real-time validation on focus change
        spinnerDayOfWeek.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !spinnerDayOfWeek.getText().toString().trim().isEmpty()) {
                validateDayOfWeek();
            }
        });

        editTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !editTime.getText().toString().trim().isEmpty()) {
                validateTime();
            }
        });

        spinnerType.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !spinnerType.getText().toString().trim().isEmpty()) {
                validateType();
            }
        });

        editCapacity.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !editCapacity.getText().toString().trim().isEmpty()) {
                validateCapacity();
            }
        });

        editDuration.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !editDuration.getText().toString().trim().isEmpty()) {
                validateDuration();
            }
        });

        editPrice.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !editPrice.getText().toString().trim().isEmpty()) {
                validatePrice();
            }
        });

        // Clear errors when user starts typing
        editCapacity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    clearError("capacity");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    clearError("duration");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    clearError("price");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateDayOfWeek() {
        String dayOfWeek = spinnerDayOfWeek.getText().toString().trim();
        if (dayOfWeek.isEmpty()) {
            errors.put("dayOfWeek", "Day of week is required");
            ToastHelper.showErrorToast(this, "Please select a day of the week");
            return false;
        }
        clearError("dayOfWeek");
        return true;
    }

    private boolean validateTime() {
        String time = editTime.getText().toString().trim();
        if (time.isEmpty()) {
            errors.put("time", "Time is required");
            ToastHelper.showErrorToast(this, "Please select a class time");
            return false;
        }
        clearError("time");
        return true;
    }

    private boolean validateType() {
        String type = spinnerType.getText().toString().trim();
        if (type.isEmpty()) {
            errors.put("type", "Class type is required");
            ToastHelper.showErrorToast(this, "Please select a class type");
            return false;
        }
        clearError("type");
        return true;
    }

    private boolean validateCapacity() {
        String capacityStr = editCapacity.getText().toString().trim();
        if (capacityStr.isEmpty()) {
            errors.put("capacity", "Capacity is required");
            ToastHelper.showErrorToast(this, "Please enter a valid capacity (positive number)");
            return false;
        }
        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                errors.put("capacity", "Capacity must be a positive number");
                ToastHelper.showErrorToast(this, "Capacity must be a positive number");
                return false;
            }
        } catch (NumberFormatException e) {
            errors.put("capacity", "Capacity must be a valid number");
            ToastHelper.showErrorToast(this, "Capacity must be a valid number");
            return false;
        }
        clearError("capacity");
        return true;
    }

    private boolean validateDuration() {
        String durationStr = editDuration.getText().toString().trim();
        if (durationStr.isEmpty()) {
            errors.put("duration", "Duration is required");
            ToastHelper.showErrorToast(this, "Please enter a valid duration (positive number)");
            return false;
        }
        try {
            int duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                errors.put("duration", "Duration must be a positive number");
                ToastHelper.showErrorToast(this, "Duration must be a positive number");
                return false;
            }
        } catch (NumberFormatException e) {
            errors.put("duration", "Duration must be a valid number");
            ToastHelper.showErrorToast(this, "Duration must be a valid number");
            return false;
        }
        clearError("duration");
        return true;
    }

    private boolean validatePrice() {
        String priceStr = editPrice.getText().toString().trim();
        if (priceStr.isEmpty()) {
            errors.put("price", "Price is required");
            ToastHelper.showErrorToast(this, "Please enter a valid price (positive number)");
            return false;
        }
        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                errors.put("price", "Price must be a positive number");
                ToastHelper.showErrorToast(this, "Price must be a positive number");
                return false;
            }
        } catch (NumberFormatException e) {
            errors.put("price", "Price must be a valid number");
            ToastHelper.showErrorToast(this, "Price must be a valid number");
            return false;
        }
        clearError("price");
        return true;
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // Validate each field in order
        if (!validateDayOfWeek()) isValid = false;
        if (!validateTime()) isValid = false;
        if (!validateType()) isValid = false;
        if (!validateCapacity()) isValid = false;
        if (!validateDuration()) isValid = false;
        if (!validatePrice()) isValid = false;
        
        return isValid;
    }

    private void showErrors() {
        // Only show error card if there are errors
        if (!errors.isEmpty()) {
            cardError.setVisibility(View.VISIBLE);
            tvErrorMessage.setText("Please fix the errors above before continuing.");
        } else {
            cardError.setVisibility(View.GONE);
        }
    }

    private void clearError(String fieldName) {
        errors.remove(fieldName);
        if (errors.isEmpty()) {
            cardError.setVisibility(View.GONE);
        }
    }

    private void clearAllErrors() {
        errors.clear();
        cardError.setVisibility(View.GONE);
    }
    
    private void setupClickListeners() {
        // Time picker
        editTime.setOnClickListener(v -> showTimePicker());
        editTime.setFocusable(false);
        editTime.setClickable(true);
        
        // Add focus listeners for toast messages
        setupFocusListeners();
        
        // Buttons
        btnSave.setOnClickListener(v -> handleSave());
        btnCancel.setOnClickListener(v -> finish());
    }
    
    private void setupFocusListeners() {
        // Day of week focus - show contextual help only when needed
        spinnerDayOfWeek.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ToastHelper.showContextualHelp(this, "day_of_week", "Select the day of the week for this class");
            }
        });
        
        // Time focus - show contextual help only when needed
        editTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ToastHelper.showContextualHelp(this, "time", "Tap to select the class time");
            }
        });
        
        // Class type focus - show contextual help only when needed
        spinnerType.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ToastHelper.showContextualHelp(this, "class_type", "Choose the type of yoga class");
            }
        });
        
        // Capacity focus - show contextual help only when needed
        editCapacity.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ToastHelper.showContextualHelp(this, "capacity", "Enter the maximum number of students (e.g., 20)");
            }
        });
        
        // Duration focus - show contextual help only when needed
        editDuration.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ToastHelper.showContextualHelp(this, "duration", "Enter class duration in minutes (e.g., 60)");
            }
        });
        
        // Price focus - show contextual help only when needed
        editPrice.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ToastHelper.showContextualHelp(this, "price", "Enter the class price in pounds (e.g., 15.00)");
            }
        });
        
        // Difficulty focus - show contextual help only when needed
        spinnerDifficulty.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ToastHelper.showContextualHelp(this, "difficulty", "Select the difficulty level (optional)");
            }
        });
        
        // Location focus - show contextual help only when needed
        editRoomLocation.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ToastHelper.showContextualHelp(this, "location", "Enter the class location (e.g., Studio A)");
            }
        });
        
        // Instructor focus - show contextual help only when needed
        editInstructor.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ToastHelper.showContextualHelp(this, "instructor", "Enter the instructor name (optional)");
            }
        });
        
        // Description focus - show contextual help only when needed
        editDescription.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ToastHelper.showContextualHelp(this, "description", "Add class description and special notes (optional)");
            }
        });
    }
    
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                String time = String.format(Locale.UK, "%02d:%02d", hourOfDay, minute);
                editTime.setText(time);
                clearError("time");
            },
            9, 0, true
        );
        timePickerDialog.show();
    }
    
    private void handleSave() {
        if (!validateForm()) {
            showErrors();
            return;
        }
        
        showConfirmationDialog();
    }
    
    private void showConfirmationDialog() {
        // Create confirmation dialog with course details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Class Details");
        
        // Create confirmation view
        View confirmView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_course, null);
        
        // Populate confirmation details
        TextView tvConfirmDay = confirmView.findViewById(R.id.tvConfirmDay);
        TextView tvConfirmTime = confirmView.findViewById(R.id.tvConfirmTime);
        TextView tvConfirmType = confirmView.findViewById(R.id.tvConfirmType);
        TextView tvConfirmDuration = confirmView.findViewById(R.id.tvConfirmDuration);
        TextView tvConfirmCapacity = confirmView.findViewById(R.id.tvConfirmCapacity);
        TextView tvConfirmPrice = confirmView.findViewById(R.id.tvConfirmPrice);
        TextView tvConfirmDifficulty = confirmView.findViewById(R.id.tvConfirmDifficulty);
        TextView tvConfirmLocation = confirmView.findViewById(R.id.tvConfirmLocation);
        TextView tvConfirmInstructor = confirmView.findViewById(R.id.tvConfirmInstructor);
        TextView tvConfirmDescription = confirmView.findViewById(R.id.tvConfirmDescription);
        
        tvConfirmDay.setText(spinnerDayOfWeek.getText().toString());
        tvConfirmTime.setText(editTime.getText().toString());
        tvConfirmType.setText(spinnerType.getText().toString());
        tvConfirmDuration.setText(editDuration.getText().toString() + " minutes");
        tvConfirmCapacity.setText(editCapacity.getText().toString() + " people");
        tvConfirmPrice.setText("£" + editPrice.getText().toString());
        
        String difficulty = spinnerDifficulty.getText().toString();
        if (!difficulty.isEmpty()) {
            tvConfirmDifficulty.setVisibility(View.VISIBLE);
            tvConfirmDifficulty.setText(difficulty);
        } else {
            tvConfirmDifficulty.setVisibility(View.GONE);
        }
        
        String location = editRoomLocation.getText().toString();
        if (!location.isEmpty()) {
            tvConfirmLocation.setVisibility(View.VISIBLE);
            tvConfirmLocation.setText(location);
        } else {
            tvConfirmLocation.setVisibility(View.GONE);
        }
        
        String instructor = editInstructor.getText().toString();
        if (!instructor.isEmpty()) {
            tvConfirmInstructor.setVisibility(View.VISIBLE);
            tvConfirmInstructor.setText(instructor);
        } else {
            tvConfirmInstructor.setVisibility(View.GONE);
        }
        
        String description = editDescription.getText().toString();
        if (!description.isEmpty()) {
            tvConfirmDescription.setVisibility(View.VISIBLE);
            tvConfirmDescription.setText(description);
        } else {
            tvConfirmDescription.setVisibility(View.GONE);
        }
        
        builder.setView(confirmView);
        builder.setPositiveButton("Confirm & Save", (dialog, which) -> saveCourse());
        builder.setNegativeButton("Edit Details", null);
        
        builder.show();
    }
    
    private void saveCourse() {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        
        // Create course object
        YogaCourse course = new YogaCourse(
            spinnerDayOfWeek.getText().toString(),
            editTime.getText().toString(),
            Integer.parseInt(editCapacity.getText().toString()),
            Integer.parseInt(editDuration.getText().toString()),
            Double.parseDouble(editPrice.getText().toString()),
            spinnerType.getText().toString(),
            editDescription.getText().toString().trim(),
            editRoomLocation.getText().toString().trim(),
            editInstructor.getText().toString().trim(),
            spinnerDifficulty.getText().toString().trim()
        );
        
        // Save to database
        new Thread(() -> {
            try {
                courseDao.insert(course);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    ToastHelper.showSuccessToast(AddCourseActivity.this, "Class created successfully!");
                    
                    // Trigger auto-sync after successful save
                    autoSyncService.triggerAutoSync();
                    
                    finish();
                });
        } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    ToastHelper.showErrorToast(AddCourseActivity.this, e.getMessage());
                });
            }
        }).start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoSyncService != null) {
            autoSyncService.shutdown();
        }
    }
}