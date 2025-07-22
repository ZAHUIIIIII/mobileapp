package com.universalyoga.adminapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.app.AlertDialog;

public class AddInstanceActivity extends AppCompatActivity {
    private EditText etTeacher, etComments;
    private Spinner spinnerCourse;
    private Button btnSubmit;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    private View progressBar;
    private MaterialAutoCompleteTextView autoDayOfWeek;
    private String[] allowedDays = new String[0];
    
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
        spinnerCourse = findViewById(R.id.spinnerCourse);
        spinnerCourse.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                YogaCourse selectedCourse = (YogaCourse) spinnerCourse.getSelectedItem();
                if (selectedCourse != null && selectedCourse.getDaysOfWeek() != null) {
                    allowedDays = selectedCourse.getDaysOfWeek().split(",");
                    for (int i = 0; i < allowedDays.length; i++) allowedDays[i] = allowedDays[i].trim();
                    ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(AddInstanceActivity.this, android.R.layout.simple_dropdown_item_1line, allowedDays);
                    autoDayOfWeek.setAdapter(dayAdapter);
                    autoDayOfWeek.setText(allowedDays.length > 0 ? allowedDays[0] : "", false);
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        autoDayOfWeek = findViewById(R.id.autoDayOfWeek);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar); // Add a ProgressBar to your layout if not present
        
        btnSubmit.setOnClickListener(v -> handleSave());
    }
    
    private void handleSave() {
        String dayOfWeek = autoDayOfWeek.getText().toString().trim();
        String teacher = etTeacher.getText().toString().trim();
        String comments = etComments.getText().toString().trim();
        if (dayOfWeek.isEmpty() || teacher.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        YogaCourse selectedCourse = (YogaCourse) spinnerCourse.getSelectedItem();
        if (selectedCourse == null) {
            Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean validDay = false;
        for (String d : allowedDays) if (d.equals(dayOfWeek)) validDay = true;
        if (!validDay) {
            Toast.makeText(this, "Selected day is not allowed for this course", Toast.LENGTH_SHORT).show();
            return;
        }
        // Show confirmation dialog
        String summary = "Course: " + selectedCourse.getCourseName() +
                "\nDay: " + dayOfWeek +
                "\nTeacher: " + teacher +
                (comments.isEmpty() ? "" : ("\nComments: " + comments));
        new AlertDialog.Builder(this)
            .setTitle("Confirm Session Details")
            .setMessage(summary)
            .setPositiveButton("Confirm", (dialog, which) -> saveInstance(selectedCourse, dayOfWeek, teacher, comments))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void saveInstance(YogaCourse selectedCourse, String dayOfWeek, String teacher, String comments) {
        progressBar.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                long result = instanceDao.insert(new YogaInstance(selectedCourse.getId(), dayOfWeek, teacher, comments));
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