package com.universalyoga.adminapp.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.ActivityDao;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.database.SyncHistoryDao;
import com.universalyoga.adminapp.models.Activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DatabaseResetUtil {
    private static final String TAG = "DatabaseResetUtil";
    private final Context context;
    private final AppDatabase database;
    private final Executor executor;
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseFirestore firestore;

    public interface ResetCallback {
        void onResetStarted();
        void onResetCompleted();
        void onResetFailed(String error);
    }

    public DatabaseResetUtil(Context context) {
        this.context = context;
        this.database = AppDatabase.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    public void resetAllData(ResetCallback callback) {
        if (database == null) {
            if (callback != null) {
                callback.onResetFailed("Database not initialized");
            }
            return;
        }
        
        if (callback != null) {
            callback.onResetStarted();
        }
        
        executor.execute(() -> {
            try {
                // Get all DAOs
                CourseDao courseDao = database.courseDao();
                InstanceDao instanceDao = database.instanceDao();
                ActivityDao activityDao = database.activityDao();
                SyncHistoryDao syncHistoryDao = database.syncHistoryDao();
                
                // Clear all tables
                courseDao.deleteAll();
                instanceDao.deleteAll();
                activityDao.deleteAll();
                syncHistoryDao.deleteAll();
                
                // Clear Firebase Realtime Database
                clearFirebaseRealtimeData();
                
                // Clear Firebase Firestore
                clearFirebaseFirestoreData();
                
                // Log the reset activity
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                Activity resetActivity = new Activity(
                    UUID.randomUUID().toString(),
                    "system",
                    "All database data reset completed (including Firebase)",
                    timestamp,
                    null
                );
                activityDao.insert(resetActivity);
                
                if (callback != null) {
                    callback.onResetCompleted();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to reset database", e);
                if (callback != null) {
                    callback.onResetFailed("Reset failed: " + e.getMessage());
                }
            }
        });
    }
    
    private void clearFirebaseRealtimeData() {
        try {
            DatabaseReference customerBookingsRef = firebaseDatabase.getReference("customer_bookings");
            customerBookingsRef.removeValue();
            Log.d(TAG, "Firebase Realtime Database cleared");
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear Firebase Realtime Database", e);
        }
    }
    
    private void clearFirebaseFirestoreData() {
        try {
            firestore.collection("bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    Log.d(TAG, "Firebase Firestore cleared");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to clear Firebase Firestore", e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear Firebase Firestore", e);
        }
    }
    
    public void clearDatabaseCompletely(ResetCallback callback) {
        if (database == null) {
            if (callback != null) {
                callback.onResetFailed("Database not initialized");
            }
            return;
        }
        
        if (callback != null) {
            callback.onResetStarted();
        }
        
        executor.execute(() -> {
            try {
                // Close the database connection
                database.close();
                
                // Delete the database file
                if (context != null) {
                    boolean deleted = context.deleteDatabase("yoga_courses.db");
                    Log.d(TAG, "Database file deleted: " + deleted);
                }
                
                // Recreate the database instance
                AppDatabase newDatabase = AppDatabase.getInstance(context);
                
                // Clear Firebase data
                clearFirebaseRealtimeData();
                clearFirebaseFirestoreData();
                
                Log.d(TAG, "Database completely cleared and recreated");
                
                if (callback != null) {
                    callback.onResetCompleted();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to clear database completely", e);
                if (callback != null) {
                    callback.onResetFailed(e.getMessage());
                }
            }
        });
    }
    
    public void resetSpecificTable(String tableName, ResetCallback callback) {
        if (database == null) {
            if (callback != null) {
                callback.onResetFailed("Database not initialized");
            }
            return;
        }
        
        if (callback != null) {
            callback.onResetStarted();
        }
        
        executor.execute(() -> {
            try {
                switch (tableName.toLowerCase()) {
                    case "courses":
                        database.courseDao().deleteAll();
                        break;
                    case "instances":
                        database.instanceDao().deleteAll();
                        break;
                    case "activity":
                        database.activityDao().deleteAll();
                        break;
                    case "synchistory":
                        database.syncHistoryDao().deleteAll();
                        break;
                    case "firebase":
                        clearFirebaseRealtimeData();
                        clearFirebaseFirestoreData();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown table: " + tableName);
                }
                
                // Log the reset activity
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                Activity resetActivity = new Activity(
                    UUID.randomUUID().toString(),
                    "system",
                    "Table " + tableName + " reset completed",
                    timestamp,
                    null
                );
                database.activityDao().insert(resetActivity);
                
                Log.d(TAG, "Table " + tableName + " reset completed successfully");
                
                if (callback != null) {
                    callback.onResetCompleted();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to reset table " + tableName, e);
                if (callback != null) {
                    callback.onResetFailed(e.getMessage());
                }
            }
        });
    }
    
    public void insertSampleData(ResetCallback callback) {
        if (database == null) {
            if (callback != null) {
                callback.onResetFailed("Database not initialized");
            }
            return;
        }
        
        if (callback != null) {
            callback.onResetStarted();
        }
        
        executor.execute(() -> {
            try {
                // Clear existing data first
                resetAllData(null);
                
                // Insert sample courses
                insertSampleCourses();
                
                // Insert sample instances
                insertSampleInstances();
                
                // Insert sample activities
                insertSampleActivities();
                
                // Insert sample sync history
                insertSampleSyncHistory();
                
                Log.d(TAG, "Sample data insertion completed successfully");
                
                if (callback != null) {
                    callback.onResetCompleted();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to insert sample data", e);
                if (callback != null) {
                    callback.onResetFailed(e.getMessage());
                }
            }
        });
    }
    
    private void insertSampleCourses() {
        try {
            CourseDao courseDao = database.courseDao();
            
            // Sample Course 1: Morning Hatha Yoga
            com.universalyoga.adminapp.models.YogaCourse course1 = new com.universalyoga.adminapp.models.YogaCourse(
                "Mon,Wed,Fri",
                "08:00",
                20,
                60,
                25.0,
                "Morning Hatha Yoga",
                "A gentle morning practice focusing on basic postures and breathing techniques.",
                "Studio A",
                "Sarah Johnson",
                "Beginner"
            );
            courseDao.insert(course1);
            
            // Sample Course 2: Power Vinyasa Flow
            com.universalyoga.adminapp.models.YogaCourse course2 = new com.universalyoga.adminapp.models.YogaCourse(
                "Tue,Thu",
                "18:00",
                15,
                75,
                30.0,
                "Power Vinyasa Flow",
                "Dynamic flowing sequences to build strength and flexibility.",
                "Studio B",
                "Mike Chen",
                "Advanced"
            );
            courseDao.insert(course2);
            
            // Sample Course 3: Restorative Yin Yoga
            com.universalyoga.adminapp.models.YogaCourse course3 = new com.universalyoga.adminapp.models.YogaCourse(
                "Wed,Sat",
                "19:30",
                12,
                90,
                28.0,
                "Restorative Yin Yoga",
                "Slow-paced practice with longer holds to promote deep relaxation.",
                "Studio C",
                "Lisa Wang",
                "Beginner"
            );
            courseDao.insert(course3);
            
            // Sample Course 4: Ashtanga Primary Series
            com.universalyoga.adminapp.models.YogaCourse course4 = new com.universalyoga.adminapp.models.YogaCourse(
                "Mon,Wed,Fri",
                "07:00",
                10,
                90,
                35.0,
                "Ashtanga Primary Series",
                "Traditional Ashtanga practice with set sequence of poses.",
                "Studio A",
                "David Kumar",
                "Advanced"
            );
            courseDao.insert(course4);
            
            // Sample Course 5: Gentle Senior Yoga
            com.universalyoga.adminapp.models.YogaCourse course5 = new com.universalyoga.adminapp.models.YogaCourse(
                "Tue,Thu,Sat",
                "10:00",
                15,
                45,
                20.0,
                "Gentle Senior Yoga",
                "Chair-supported yoga perfect for seniors and those with mobility issues.",
                "Studio C",
                "Mary Thompson",
                "Beginner"
            );
            courseDao.insert(course5);
            
            Log.d(TAG, "Sample courses insertion completed");
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert sample courses", e);
        }
    }
    
    private void insertSampleInstances() {
        try {
            InstanceDao instanceDao = database.instanceDao();
            
            // Get course IDs (assuming they were inserted in order)
            int course1Id = 1;
            int course2Id = 2;
            int course3Id = 3;
            int course4Id = 4;
            int course5Id = 5;
            
            // Sample instances for Course 1 (Morning Hatha Yoga)
            com.universalyoga.adminapp.models.YogaInstance instance1 = new com.universalyoga.adminapp.models.YogaInstance();
            instance1.setCourseId(course1Id);
            instance1.setDate("2025-01-27");
            instance1.setStartTime("08:00");
            instance1.setCapacity(20);
            instance1.setEnrolled(15);
            instance1.setTeacher("Sarah Johnson");
            instanceDao.insert(instance1);
            
            com.universalyoga.adminapp.models.YogaInstance instance2 = new com.universalyoga.adminapp.models.YogaInstance();
            instance2.setCourseId(course1Id);
            instance2.setDate("2025-01-29");
            instance2.setStartTime("08:00");
            instance2.setCapacity(20);
            instance2.setEnrolled(12);
            instance2.setTeacher("Sarah Johnson");
            instanceDao.insert(instance2);
            
            // Sample instances for Course 2 (Power Vinyasa Flow)
            com.universalyoga.adminapp.models.YogaInstance instance3 = new com.universalyoga.adminapp.models.YogaInstance();
            instance3.setCourseId(course2Id);
            instance3.setDate("2025-01-28");
            instance3.setStartTime("18:00");
            instance3.setCapacity(15);
            instance3.setEnrolled(10);
            instance3.setTeacher("Mike Chen");
            instanceDao.insert(instance3);
            
            com.universalyoga.adminapp.models.YogaInstance instance4 = new com.universalyoga.adminapp.models.YogaInstance();
            instance4.setCourseId(course2Id);
            instance4.setDate("2025-01-30");
            instance4.setStartTime("18:00");
            instance4.setCapacity(15);
            instance4.setEnrolled(8);
            instance4.setTeacher("Mike Chen");
            instanceDao.insert(instance4);
            
            // Sample instances for Course 3 (Restorative Yin Yoga)
            com.universalyoga.adminapp.models.YogaInstance instance5 = new com.universalyoga.adminapp.models.YogaInstance();
            instance5.setCourseId(course3Id);
            instance5.setDate("2025-01-29");
            instance5.setStartTime("19:30");
            instance5.setCapacity(12);
            instance5.setEnrolled(6);
            instance5.setTeacher("Lisa Wang");
            instanceDao.insert(instance5);
            
            Log.d(TAG, "Sample instances insertion completed");
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert sample instances", e);
        }
    }
    
    private void insertSampleActivities() {
        try {
            ActivityDao activityDao = database.activityDao();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            
            // Create sample activities
            Activity resetActivity = new Activity(
                UUID.randomUUID().toString(),
                "system",
                "Sample data inserted - 5 courses and 5 instances created",
                timestamp,
                null
            );
            activityDao.insert(resetActivity);
            
            Activity courseActivity = new Activity(
                UUID.randomUUID().toString(),
                "course",
                "Morning Hatha Yoga course was created",
                timestamp,
                null
            );
            activityDao.insert(courseActivity);
            
            Activity instanceActivity = new Activity(
                UUID.randomUUID().toString(),
                "instance",
                "New class instance scheduled for 2025-01-27",
                timestamp,
                null
            );
            activityDao.insert(instanceActivity);
            
            Activity syncActivity = new Activity(
                UUID.randomUUID().toString(),
                "sync",
                "Cloud sync completed - 10 records synced",
                timestamp,
                null
            );
            activityDao.insert(syncActivity);
            
            Log.d(TAG, "Sample activities insertion completed");
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert sample activities", e);
        }
    }
    
    private void insertSampleSyncHistory() {
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            
            // Sample sync history
            com.universalyoga.adminapp.models.SyncHistory syncHistory = com.universalyoga.adminapp.models.SyncHistory.createWithId();
            syncHistory.setType("upload");
            syncHistory.setTrigger("Sample data sync");
            syncHistory.setTimestamp(timestamp);
            syncHistory.setStatus("success");
            syncHistory.setDataSize(10);
            database.syncHistoryDao().insert(syncHistory);
            
            Log.d(TAG, "Sample sync history insertion completed");
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert sample sync history", e);
        }
    }
} 