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
    private com.google.android.material.textfield.TextInputEditText etDate, etStartTime, etEndTime;
    private EditText etTeacher, etComments;
    private TextView etEnrolled, etCapacity, etCourseDaysOfWeek, etCourseStartTime, etCourseCapacity, etCourseDuration;
    private AutoCompleteTextView autoCourse;
    private Button btnSubmit;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    private View progressBar;
    private List<YogaCourse> allCourses;
    private YogaCourse selectedCourse;
    private com.google.android.material.textfield.TextInputLayout tilStartTime, tilEndTime;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_instance);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        courseDao = AppDatabase.getInstance(this).courseDao();
        instanceDao = AppDatabase.getInstance(this).instanceDao();
        
        etDate = (com.google.android.material.textfield.TextInputEditText) findViewById(R.id.etDate);
        etTeacher = findViewById(R.id.etTeacher);
        etComments = findViewById(R.id.etComments);
        autoCourse = findViewById(R.id.autoCourse);
        etCourseDaysOfWeek = findViewById(R.id.etCourseDaysOfWeek);
        etCourseStartTime = findViewById(R.id.etCourseStartTime);
        etCourseCapacity = findViewById(R.id.etCourseCapacity);
        etCourseDuration = findViewById(R.id.etCourseDuration);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        etDate.setOnClickListener(v -> showDatePicker());

        btnSubmit.setOnClickListener(v -> handleSave());

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
                    etCourseDaysOfWeek.setText(selectedCourse.getDaysOfWeek());
                    etCourseStartTime.setText(selectedCourse.getTime());
                    etCourseCapacity.setText(String.valueOf(selectedCourse.getCapacity()));
                    etCourseDuration.setText(String.valueOf(selectedCourse.getDuration()));
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(calendar.getTime());
            etDate.setText(formattedDate);
            Toast.makeText(AddInstanceActivity.this, "Selected Date: " + formattedDate, Toast.LENGTH_LONG).show();
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

        // Get values from selected course
        final String startTime = selectedCourse.getTime();
        // Calculate endTime based on startTime and duration
        final String endTime;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(startTime));
            calendar.add(Calendar.MINUTE, selectedCourse.getDuration());
            endTime = sdf.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error calculating end time", Toast.LENGTH_SHORT).show();
            return;
        }
        final int enrolled = 0; // New instances start with 0 enrolled
        final int capacity = selectedCourse.getCapacity();

        // Date validation
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.setTime(dateFormat.parse(selectedDateString));
            String selectedDayAbbr = new SimpleDateFormat("EEE", Locale.getDefault()).format(selectedCalendar.getTime());

            // Validate against daysOfWeek (comma-separated abbreviations)
            String courseDays = selectedCourse.getDaysOfWeek();
            boolean match = false;
            if (courseDays != null) {
                for (String day : courseDays.split(",")) {
                    if (selectedDayAbbr.equalsIgnoreCase(day.trim())) {
                        match = true;
                        break;
                    }
                }
            }
            if (!match) {
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
                "\nTime: " + startTime + " - " + endTime +
                "\nEnrolled: " + enrolled + " / " + capacity +
                (comments.isEmpty() ? "" : ("\nComments: " + comments));
        new AlertDialog.Builder(this)
            .setTitle("Confirm Session Details")
            .setMessage(summary)
            .setPositiveButton("Confirm", (dialog, which) -> saveInstance(selectedCourse, selectedDateString, teacher, comments, startTime, endTime, enrolled, capacity))
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

    private void saveInstance(YogaCourse selectedCourse, String date, String teacher, String comments, String startTime, String endTime, int enrolled, int capacity) {
        progressBar.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                long result = instanceDao.insert(new YogaInstance(0, selectedCourse.getId(), date, teacher, comments, 0, startTime, endTime, enrolled, capacity));
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