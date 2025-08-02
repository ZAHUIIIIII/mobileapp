package com.universalyoga.adminapp.utils;

import android.content.Context;
import android.util.Log;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.ActivityDao;
import com.universalyoga.adminapp.models.Activity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityLogger {
    private static final String TAG = "ActivityLogger";
    private static ActivityLogger instance;
    private ActivityDao activityDao;
    private ExecutorService executor;
    
    private ActivityLogger(Context context) {
        try {
            activityDao = AppDatabase.getInstance(context).activityDao();
            executor = Executors.newSingleThreadExecutor();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize ActivityLogger", e);
        }
    }
    
    public static synchronized ActivityLogger getInstance(Context context) {
        if (instance == null) {
            instance = new ActivityLogger(context.getApplicationContext());
        }
        return instance;
    }
    
    public void logActivity(String type, String description) {
        logActivity(type, description, null);
    }
    
    public void logActivity(String type, String description, String relatedId) {
        if (activityDao == null) {
            return;
        }
        
        if (type == null || type.trim().isEmpty()) {
            return;
        }
        
        if (description == null || description.trim().isEmpty()) {
            return;
        }
        
        executor.execute(() -> {
            try {
                String id = UUID.randomUUID().toString();
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                
                Activity activity = new Activity(id, type.trim(), description.trim(), timestamp, relatedId);
                activityDao.insert(activity);
            } catch (Exception e) {
                Log.e(TAG, "Failed to log activity: " + type + " - " + description, e);
            }
        });
    }
    
    public void logCourseCreated(String courseName) {
        if (courseName != null && !courseName.trim().isEmpty()) {
            logActivity("course", "Course '" + courseName.trim() + "' was created");
        }
    }
    
    public void logCourseUpdated(String courseName) {
        if (courseName != null && !courseName.trim().isEmpty()) {
            logActivity("course", "Course '" + courseName.trim() + "' was updated");
        }
    }
    
    public void logCourseDeleted(String courseName) {
        if (courseName != null && !courseName.trim().isEmpty()) {
            logActivity("course", "Course '" + courseName.trim() + "' was deleted");
        }
    }
    
    public void logInstanceCreated(String instanceInfo) {
        if (instanceInfo != null && !instanceInfo.trim().isEmpty()) {
            logActivity("instance", "Instance '" + instanceInfo.trim() + "' was created");
        }
    }
    
    public void logInstanceUpdated(String instanceInfo) {
        if (instanceInfo != null && !instanceInfo.trim().isEmpty()) {
            logActivity("instance", "Instance '" + instanceInfo.trim() + "' was updated");
        }
    }
    
    public void logInstanceDeleted(String instanceInfo) {
        if (instanceInfo != null && !instanceInfo.trim().isEmpty()) {
            logActivity("instance", "Instance '" + instanceInfo.trim() + "' was deleted");
        }
    }
    
    public void logSyncStarted() {
        logActivity("sync", "Cloud sync started");
    }
    
    public void logSyncCompleted(int recordsCount) {
        logActivity("sync", "Cloud sync completed - " + recordsCount + " records synced");
    }
    
    public void logSyncFailed(String error) {
        if (error != null && !error.trim().isEmpty()) {
            logActivity("sync", "Cloud sync failed - " + error.trim());
        } else {
            logActivity("sync", "Cloud sync failed");
        }
    }
    
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
} 