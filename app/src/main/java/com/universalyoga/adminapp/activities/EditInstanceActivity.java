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

public class EditInstanceActivity extends AppCompatActivity {
    private EditText etTeacher, etComments, etDate;
    private TextView tvCourseName;
    private Button btnUpdateInstance, btnDeleteInstance;
    private InstanceDao instanceDao;
    private CourseDao courseDao;
    private View progressBar;
    private int instanceId;
    private YogaCourse associatedCourse;

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

        tvCourseName = findViewById(R.id.tvCourseName);
        etDate = findViewById(R.id.etDate);
        etTeacher = findViewById(R.id.etTeacher);
        etComments = findViewById(R.id.etComments);
        btnUpdateInstance = findViewById(R.id.btnUpdateInstance);
        btnDeleteInstance = findViewById(R.id.btnDeleteInstance);
        progressBar = findViewById(R.id.progressBar);

        etDate.setOnClickListener(v -> showDatePicker());
        etDate.setFocusable(false);
        etDate.setClickable(true);

        btnUpdateInstance.setOnClickListener(v -> handleUpdate());
        btnDeleteInstance.setOnClickListener(v -> handleDelete());

        loadInstanceData();
    }

    private void handleDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Instance")
                .setMessage("Are you sure you want to delete this instance?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    Executors.newSingleThreadExecutor().execute(() -> {
                        YogaInstance instanceToDelete = instanceDao.getById(instanceId);
                        if (instanceToDelete != null) {
                            int result = instanceDao.delete(instanceToDelete);
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                if (result > 0) {
                                    Toast.makeText(this, "Instance deleted!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Snackbar.make(findViewById(android.R.id.content), "Delete failed!", Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadInstanceData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            YogaInstance instance = instanceDao.getById(instanceId);
            if (instance != null) {
                associatedCourse = courseDao.getById(instance.getCourseId());
                runOnUiThread(() -> {
                    if (associatedCourse != null) {
                        tvCourseName.setText(associatedCourse.getCourseName());
                    }
                    etDate.setText(instance.getDate());
                    etTeacher.setText(instance.getTeacher());
                    etComments.setText(instance.getComments());
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etDate.setText(dateFormat.format(calendar.getTime()));
        });
        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    private void handleUpdate() {
        final String selectedDateString = etDate.getText().toString().trim();
        final String teacher = etTeacher.getText().toString().trim();
        final String comments = etComments.getText().toString().trim();

        if (selectedDateString.isEmpty() || teacher.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (associatedCourse == null) {
            Toast.makeText(this, "Associated course not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Date validation
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.setTime(dateFormat.parse(selectedDateString));
            int dayOfWeekInt = selectedCalendar.get(Calendar.DAY_OF_WEEK);
            String selectedDayName = getDayName(dayOfWeekInt);

            // Validate against single day of week from associatedCourse
            if (!selectedDayName.equalsIgnoreCase(associatedCourse.getDaysOfWeek())) {
                Toast.makeText(this, "Selected date's day of the week does not match the course schedule.", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                YogaInstance updatedInstance = new YogaInstance(instanceId, associatedCourse.getId(), selectedDateString, teacher, comments, 0);
                int result = instanceDao.update(updatedInstance);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (result > 0) {
                        Toast.makeText(this, "Instance updated!", Toast.LENGTH_SHORT).show();
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
        });
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