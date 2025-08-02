package com.universalyoga.adminapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.database.ActivityDao;
import com.universalyoga.adminapp.database.SyncHistoryDao;
import com.universalyoga.adminapp.utils.DatabaseResetUtil;
import com.universalyoga.adminapp.utils.ToastHelper;
import java.util.concurrent.Executors;

public class DatabaseManagementActivity extends AppCompatActivity {
    private Button btnResetAll;
    private Button btnClearDatabase;
    private Button btnResetActivity;
    private Button btnResetCourses;
    private Button btnResetInstances;
    private TextView tvDatabaseStats;
    
    private DatabaseResetUtil resetUtil;
    private AppDatabase database;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_reset);
        
        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Database Management");
        }
        
        initializeViews();
        setupListeners();
        
        resetUtil = new DatabaseResetUtil(this);
        database = AppDatabase.getInstance(this);
        
        // Load initial stats
        loadDatabaseStats();
    }
    
    private void initializeViews() {
        btnResetAll = findViewById(R.id.btnResetAll);
        btnClearDatabase = findViewById(R.id.btnClearDatabase);
        btnResetActivity = findViewById(R.id.btnResetActivity);
        btnResetCourses = findViewById(R.id.btnResetCourses);
        btnResetInstances = findViewById(R.id.btnResetInstances);
        tvDatabaseStats = findViewById(R.id.tvDatabaseStats);
    }
    
    private void setupListeners() {
        btnResetAll.setOnClickListener(v -> showResetConfirmation("all", "all data"));
        btnClearDatabase.setOnClickListener(v -> clearDatabase());
        btnResetActivity.setOnClickListener(v -> showResetConfirmation("activity", "activity data"));
        btnResetCourses.setOnClickListener(v -> showResetConfirmation("courses", "course data"));
        btnResetInstances.setOnClickListener(v -> showResetConfirmation("instances", "instance data"));
    }
    
    private void loadDatabaseStats() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                CourseDao courseDao = database.courseDao();
                InstanceDao instanceDao = database.instanceDao();
                ActivityDao activityDao = database.activityDao();
                SyncHistoryDao syncHistoryDao = database.syncHistoryDao();
                
                int courseCount = courseDao.getCourseCount();
                int instanceCount = instanceDao.getInstanceCount();
                int activityCount = activityDao.getActivityCount();
                int syncHistoryCount = syncHistoryDao.getSyncHistoryCount();
                
                String stats = String.format(
                    "📊 Database Statistics\n\n" +
                    "📚 Courses: %d\n" +
                    "📅 Instances: %d\n" +
                    "📝 Activities: %d\n" +
                    "☁️ Sync History: %d\n\n" +
                    "Total Records: %d",
                    courseCount, instanceCount, activityCount, syncHistoryCount,
                    courseCount + instanceCount + activityCount + syncHistoryCount
                );
                
                runOnUiThread(() -> {
                    tvDatabaseStats.setText(stats);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvDatabaseStats.setText("Error loading database statistics: " + e.getMessage());
                });
            }
        });
    }
    
    private void showResetConfirmation(String tableName, String displayName) {
        new AlertDialog.Builder(this)
            .setTitle("Reset " + displayName)
            .setMessage("Are you sure you want to reset " + displayName + "? This action cannot be undone.")
            .setPositiveButton("Reset", (dialog, which) -> {
                if ("all".equals(tableName)) {
                    resetAllData();
                } else {
                    resetSpecificTable(tableName);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void resetAllData() {
        resetUtil.resetAllData(new DatabaseResetUtil.ResetCallback() {
            @Override
            public void onResetStarted() {
                // Operation started
            }
            
            @Override
            public void onResetCompleted() {
                runOnUiThread(() -> {
                    ToastHelper.showSuccessToast(DatabaseManagementActivity.this, "Database reset completed");
                    loadDatabaseStats();
                });
            }
            
            @Override
            public void onResetFailed(String error) {
                runOnUiThread(() -> {
                    ToastHelper.showErrorToast(DatabaseManagementActivity.this, "Reset failed: " + error);
                });
            }
        });
    }
    
    private void resetSpecificTable(String tableName) {
        resetUtil.resetSpecificTable(tableName, new DatabaseResetUtil.ResetCallback() {
            @Override
            public void onResetStarted() {
                // Operation started
            }
            
            @Override
            public void onResetCompleted() {
                runOnUiThread(() -> {
                    ToastHelper.showSuccessToast(DatabaseManagementActivity.this, tableName + " reset completed");
                    loadDatabaseStats();
                });
            }
            
            @Override
            public void onResetFailed(String error) {
                runOnUiThread(() -> {
                    ToastHelper.showErrorToast(DatabaseManagementActivity.this, "Reset failed: " + error);
                });
            }
        });
    }
    
    private void clearDatabase() {
        new AlertDialog.Builder(this)
            .setTitle("Clear Database")
            .setMessage("Are you sure you want to clear the entire database? This action cannot be undone.")
            .setPositiveButton("Clear", (dialog, which) -> {
                resetUtil.clearDatabaseCompletely(new DatabaseResetUtil.ResetCallback() {
                    @Override
                    public void onResetStarted() {
                        // Operation started
                    }

                    @Override
                    public void onResetCompleted() {
                        runOnUiThread(() -> {
                            ToastHelper.showSuccessToast(DatabaseManagementActivity.this, "Database cleared");
                            loadDatabaseStats();
                        });
                    }

                    @Override
                    public void onResetFailed(String error) {
                        runOnUiThread(() -> {
                            ToastHelper.showErrorToast(DatabaseManagementActivity.this, "Clear failed: " + error);
                        });
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // DatabaseResetUtil cleanup is handled automatically
    }
} 