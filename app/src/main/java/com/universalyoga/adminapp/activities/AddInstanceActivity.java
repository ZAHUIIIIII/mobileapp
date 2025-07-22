package com.universalyoga.adminapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
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

public class AddInstanceActivity extends AppCompatActivity {
    private EditText etTeacher, etComments, etDate;
    private AutoCompleteTextView autoCourse;
    private Button btnSubmit;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    private View progressBar;
    private List<YogaCourse> allCourses;
    private YogaCourse selectedCourse;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_instance);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        courseDao = AppDatabase.getInstance(this).courseDao();
        instanceDao = AppDatabase.getInstance(this).instanceDao();
        
        etTeacher = findViewById(R.id.etTeacher);
        etComments = findViewById(R.id.etComments);
        autoCourse = findViewById(R.id.autoCourse);
        etDate = findViewById(R.id.etDate);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        
        btnSubmit.setOnClickListener(v -> handleSave());

        etDate.setOnClickListener(v -> showDatePicker());
        etDate.setFocusable(false);
        etDate.setClickable(true);

        loadCourses();
    }

    private void loadCourses() {
        Executors.newSingleThreadExecutor().execute(() -> {
            allCourses = courseDao.getAll();
            runOnUiThread(() -> {
                ArrayAdapter<YogaCourse> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allCourses);
                autoCourse.setAdapter(courseAdapter);
                autoCourse.setOnItemClickListener((parent, view, position, id) -> {
                    selectedCourse = (YogaCourse) parent.getItemAtPosition(position);
                });
            });
        });
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etDate.setText(dateFormat.format(calendar.getTime()));
        });
        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }
    
    private void handleSave() {
        final String selectedDateString = etDate.getText().toString().trim();
        final String teacher = etTeacher.getText().toString().trim();
        final String comments = etComments.getText().toString().trim();

        if (selectedDateString.isEmpty() || teacher.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCourse == null) {
            Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
            return;
        }

        // Date validation
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.setTime(dateFormat.parse(selectedDateString));
            int dayOfWeekInt = selectedCalendar.get(Calendar.DAY_OF_WEEK);
            String selectedDayName = getDayName(dayOfWeekInt);

            // Validate against single day of week from selectedCourse
            if (!selectedDayName.equalsIgnoreCase(selectedCourse.getDaysOfWeek())) {
                Toast.makeText(this, "Selected date's day of the week does not match the course schedule.", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        String summary = "Course: " + selectedCourse.getCourseName() +
                "\nDate: " + selectedDateString +
                "\nTeacher: " + teacher +
                (comments.isEmpty() ? "" : ("\nComments: " + comments));
        new AlertDialog.Builder(this)
            .setTitle("Confirm Session Details")
            .setMessage(summary)
            .setPositiveButton("Confirm", (dialog, which) -> saveInstance(selectedCourse, selectedDateString, teacher, comments))
            .setNegativeButton("Cancel", null)
            .show();
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

    private void saveInstance(YogaCourse selectedCourse, String date, String teacher, String comments) {
        progressBar.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                long result = instanceDao.insert(new YogaInstance(selectedCourse.getId(), date, teacher, comments));
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (result > 0) {
                        Toast.makeText(this, "Instance added!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Insert failed!", Snackbar.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content), "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }
}