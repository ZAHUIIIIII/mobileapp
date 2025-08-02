package com.universalyoga.adminapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
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
import com.universalyoga.adminapp.utils.ActivityLogger;
import java.util.concurrent.Executors;

public class DatabaseManagementActivity extends AppCompatActivity {
    private Button btnResetAll;
    private Button btnClearDatabase;
    private Button btnResetActivity;
    private Button btnResetCourses;
    private Button btnResetInstances;
    private Button btnInsertSample;
    private Button btnCreateSampleActivities;
    private Button btnRefreshStats;
    private ProgressBar progressBar;
    private TextView tvStatus;
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
        btnInsertSample = findViewById(R.id.btnInsertSample);
        btnCreateSampleActivities = findViewById(R.id.btnCreateSampleActivities);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        
        // Add new views
        btnRefreshStats = findViewById(R.id.btnRefreshStats);
        tvDatabaseStats = findViewById(R.id.tvDatabaseStats);
        
        // Set initial status
        tvStatus.setText("Database Management Ready");
    }
    
    private void setupListeners() {
        btnResetAll.setOnClickListener(v -> showResetConfirmation("all", "all data"));
        btnClearDatabase.setOnClickListener(v -> clearDatabase());
        btnResetActivity.setOnClickListener(v -> showResetConfirmation("activity", "activity data"));
        btnResetCourses.setOnClickListener(v -> showResetConfirmation("courses", "course data"));
        btnResetInstances.setOnClickListener(v -> showResetConfirmation("instances", "instance data"));
        btnInsertSample.setOnClickListener(v -> insertSampleData());
        btnCreateSampleActivities.setOnClickListener(v -> createSampleActivities());
        btnRefreshStats.setOnClickListener(v -> loadDatabaseStats());
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
                    tvStatus.setText("Database statistics updated");
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvDatabaseStats.setText("Error loading database statistics: " + e.getMessage());
                    tvStatus.setText("Failed to load statistics");
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
        setLoadingState(true);
        resetUtil.resetAllData(new DatabaseResetUtil.ResetCallback() {
            @Override
            public void onResetStarted() {
                runOnUiThread(() -> {
                    tvStatus.setText("Resetting all data...");
                });
            }
            
            @Override
            public void onResetCompleted() {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    tvStatus.setText("All data reset successfully");
                    ToastHelper.showSuccessToast(DatabaseManagementActivity.this, "Database reset completed");
                    loadDatabaseStats(); // Refresh stats
                });
            }
            
            @Override
            public void onResetFailed(String error) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    tvStatus.setText("Reset failed: " + error);
                    ToastHelper.showErrorToast(DatabaseManagementActivity.this, "Reset failed: " + error);
                });
            }
        });
    }
    
    private void resetSpecificTable(String tableName) {
        setLoadingState(true);
        resetUtil.resetSpecificTable(tableName, new DatabaseResetUtil.ResetCallback() {
            @Override
            public void onResetStarted() {
                runOnUiThread(() -> {
                    tvStatus.setText("Resetting " + tableName + "...");
                });
            }
            
            @Override
            public void onResetCompleted() {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    tvStatus.setText(tableName + " reset successfully");
                    ToastHelper.showSuccessToast(DatabaseManagementActivity.this, tableName + " reset completed");
                    loadDatabaseStats(); // Refresh stats
                });
            }
            
            @Override
            public void onResetFailed(String error) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    tvStatus.setText("Reset failed: " + error);
                    ToastHelper.showErrorToast(DatabaseManagementActivity.this, "Reset failed: " + error);
                });
            }
        });
    }
    
    private void insertSampleData() {
        setLoadingState(true);
        resetUtil.insertSampleData(new DatabaseResetUtil.ResetCallback() {
            @Override
            public void onResetStarted() {
                runOnUiThread(() -> {
                    tvStatus.setText("Inserting sample data...");
                });
            }
            
            @Override
            public void onResetCompleted() {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    tvStatus.setText("Sample data inserted successfully");
                    ToastHelper.showSuccessToast(DatabaseManagementActivity.this, "Sample data inserted");
                    loadDatabaseStats(); // Refresh stats
                });
            }
            
            @Override
            public void onResetFailed(String error) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    tvStatus.setText("Insert failed: " + error);
                    ToastHelper.showErrorToast(DatabaseManagementActivity.this, "Insert failed: " + error);
                });
            }
        });
    }
    
    private void createSampleActivities() {
        setLoadingState(true);
        tvStatus.setText("Creating sample activities...");
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ActivityLogger logger = ActivityLogger.getInstance(DatabaseManagementActivity.this);
                
                // Create sample activities
                logger.logCourseCreated("Yoga Basics");
                Thread.sleep(1000); // Add some delay between activities
                
                logger.logInstanceCreated("Yoga Basics - Monday 9:00 AM");
                Thread.sleep(1000);
                
                logger.logSyncStarted();
                Thread.sleep(1000);
                
                logger.logSyncCompleted(15);
                Thread.sleep(1000);
                
                logger.logCourseCreated("Advanced Yoga");
                Thread.sleep(1000);
                
                logger.logInstanceCreated("Advanced Yoga - Wednesday 6:00 PM");
                Thread.sleep(1000);
                
                logger.logCourseUpdated("Yoga Basics");
                Thread.sleep(1000);
                
                logger.logInstanceDeleted("Old Yoga Session");
                
                runOnUiThread(() -> {
                    setLoadingState(false);
                    tvStatus.setText("Sample activities created successfully");
                    ToastHelper.showSuccessToast(DatabaseManagementActivity.this, "Sample activities created successfully");
                    loadDatabaseStats();
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    tvStatus.setText("Failed to create sample activities: " + e.getMessage());
                    ToastHelper.showErrorToast(DatabaseManagementActivity.this, "Failed to create sample activities: " + e.getMessage());
                });
            }
        });
    }

    private void clearDatabase() {
        new AlertDialog.Builder(this)
            .setTitle("Clear Database")
            .setMessage("Are you sure you want to clear the entire database? This action cannot be undone.")
            .setPositiveButton("Clear", (dialog, which) -> {
                setLoadingState(true);
                resetUtil.clearDatabaseCompletely(new DatabaseResetUtil.ResetCallback() {
                    @Override
                    public void onResetStarted() {
                        runOnUiThread(() -> {
                            tvStatus.setText("Clearing database...");
                        });
                    }

                    @Override
                    public void onResetCompleted() {
                        runOnUiThread(() -> {
                            setLoadingState(false);
                            tvStatus.setText("Database cleared successfully");
                            ToastHelper.showSuccessToast(DatabaseManagementActivity.this, "Database cleared");
                            loadDatabaseStats(); // Refresh stats
                        });
                    }

                    @Override
                    public void onResetFailed(String error) {
                        runOnUiThread(() -> {
                            setLoadingState(false);
                            tvStatus.setText("Clear failed: " + error);
                            ToastHelper.showErrorToast(DatabaseManagementActivity.this, "Clear failed: " + error);
                        });
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnResetAll.setEnabled(!isLoading);
        btnClearDatabase.setEnabled(!isLoading);
        btnResetActivity.setEnabled(!isLoading);
        btnResetCourses.setEnabled(!isLoading);
        btnResetInstances.setEnabled(!isLoading);
        btnInsertSample.setEnabled(!isLoading);
        btnCreateSampleActivities.setEnabled(!isLoading);
        btnRefreshStats.setEnabled(!isLoading);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // DatabaseResetUtil cleanup is handled automatically
    }
} 