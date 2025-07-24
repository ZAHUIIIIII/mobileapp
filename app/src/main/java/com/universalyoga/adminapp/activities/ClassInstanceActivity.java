package com.universalyoga.adminapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.Observer;
import com.google.android.material.appbar.MaterialToolbar;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.adapters.InstanceAdapter;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.List;
import java.util.concurrent.Executors;
import android.app.AlertDialog;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import com.universalyoga.adminapp.activities.EditInstanceActivity;

public class ClassInstanceActivity extends AppCompatActivity implements InstanceAdapter.OnInstanceActionListener {
    private RecyclerView rvInstances;
    private InstanceDao instanceDao;
    private CourseDao courseDao;
    private InstanceAdapter adapter;
    private int courseId;
    private TextView tvCourseName;
    
    @Override protected void onCreate(Bundle s){
        super.onCreate(s); 
        setContentView(R.layout.activity_instance);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        courseId = getIntent().getIntExtra("courseId", -1);
        if (courseId == -1) {
            finish(); // No course ID provided, close activity
            return;
        }

        instanceDao = AppDatabase.getInstance(this).instanceDao();
        courseDao = AppDatabase.getInstance(this).courseDao();
        rvInstances = findViewById(R.id.instanceRecycler);
        rvInstances.setLayoutManager(new LinearLayoutManager(this));
        tvCourseName = findViewById(R.id.tvCourseName);
        
        // Add Instance button
        Button btnAddInstance = findViewById(R.id.btnAddInstance);
        btnAddInstance.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddInstanceActivity.class);
            intent.putExtra("courseId", courseId);
            startActivity(intent);
        });
        
        loadCourseDetails();
        observeInstances();
    }

    private void loadCourseDetails() {
        Executors.newSingleThreadExecutor().execute(() -> {
            YogaCourse course = courseDao.getById(courseId);
            runOnUiThread(() -> {
                if (course != null) {
                    tvCourseName.setText(course.getCourseName());
                } else {
                    tvCourseName.setText("Course Not Found");
                }
            });
        });
    }
    
    private void observeInstances() {
        adapter = new InstanceAdapter(java.util.Collections.emptyList(), this);
        rvInstances.setAdapter(adapter);
        instanceDao.getByCourse(courseId).observe(this, new Observer<List<YogaInstance>>() {
            @Override
            public void onChanged(List<YogaInstance> instances) {
                adapter.updateInstances(instances);
            }
        });
    }

    public void onInstanceClick(YogaInstance instance) {
        // Not implemented for now, as per requirement
    }

    public void onEditClick(YogaInstance instance) {
        Intent intent = new Intent(this, EditInstanceActivity.class);
        intent.putExtra("instanceId", instance.getId());
        startActivity(intent);
    }

    public void onDeleteClick(YogaInstance instance) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Instance")
            .setMessage("Are you sure you want to delete this instance?")
            .setPositiveButton("Delete", (dialog, which) -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    int result = instanceDao.delete(instance);
                    runOnUiThread(() -> {
                        if (result > 0) {
                            Snackbar.make(findViewById(android.R.id.content), "Instance deleted!", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "Delete failed!", Snackbar.LENGTH_LONG).show();
                        }
                    });
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}