package com.universalyoga.adminapp.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.models.YogaCourse;
import android.content.Intent;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import com.google.android.material.appbar.MaterialToolbar;

public class ConfirmCourseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_course);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Get data from intent
        String courseName = getIntent().getStringExtra("courseName");
        String daysOfWeek = getIntent().getStringExtra("daysOfWeek");
        String time = getIntent().getStringExtra("time");
        int capacity = Integer.parseInt(getIntent().getStringExtra("capacity"));
        int duration = getIntent().getIntExtra("duration", 1);
        double price = Double.parseDouble(getIntent().getStringExtra("price"));
        String type = getIntent().getStringExtra("type");
        String description = getIntent().getStringExtra("description");
        String roomLocation = getIntent().getStringExtra("roomLocation");

        // Set summary (update to show courseName and daysOfWeek)
        ((TextView)findViewById(R.id.tvSummaryCourseName)).setText(courseName);
        ((TextView)findViewById(R.id.tvSummaryDate)).setText(daysOfWeek);
        ((TextView)findViewById(R.id.tvSummaryTime)).setText(time);
        ((TextView)findViewById(R.id.tvSummaryCapacity)).setText(String.valueOf(capacity));
        ((TextView)findViewById(R.id.tvSummaryDuration)).setText(duration + " min");
        ((TextView)findViewById(R.id.tvSummaryPrice)).setText("£" + String.format("%.2f", price));
        ((TextView)findViewById(R.id.tvSummaryType)).setText(type);
        ((TextView)findViewById(R.id.tvSummaryDescription)).setText(description);
        ((TextView)findViewById(R.id.tvSummaryRoomLocation)).setText(roomLocation);

        Button btnConfirm = findViewById(R.id.btnConfirmCourse);
        Button btnCancel = findViewById(R.id.btnCancelConfirm);
        final View progressBar = findViewById(R.id.progressBar); // Add a ProgressBar to your layout if not present
        btnConfirm.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            YogaCourse course = new YogaCourse(courseName, daysOfWeek, time, capacity, duration, price, type, description, roomLocation);
            new Thread(() -> {
                try {
                    AppDatabase.getInstance(this).courseDao().insert(course);
                    com.universalyoga.adminapp.utils.ToastHelper.pendingToastMessage = "Course added successfully!";
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(ConfirmCourseActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(findViewById(android.R.id.content), "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    });
                }
            }).start();
        });
        btnCancel.setOnClickListener(v -> finish());
    }
} 