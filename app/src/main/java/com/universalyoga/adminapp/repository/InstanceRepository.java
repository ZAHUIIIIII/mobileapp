package com.universalyoga.adminapp.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.ActivityDao;
import com.universalyoga.adminapp.models.YogaInstance;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.Activity;
import com.universalyoga.adminapp.utils.ActivityLogger;
import com.universalyoga.adminapp.services.FirebaseService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class InstanceRepository {
    private static final String TAG = "InstanceRepository";
    private final InstanceDao instanceDao;
    private final CourseDao courseDao;
    private final ActivityDao activityDao;
    private final ExecutorService executorService;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public InstanceRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.instanceDao = database.instanceDao();
        this.courseDao = database.courseDao();
        this.activityDao = database.activityDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    // LiveData getters
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // Get all instances
    public LiveData<List<YogaInstance>> getAllInstances() {
        return instanceDao.getAllLive();
    }

    // Get instance by ID
    public LiveData<YogaInstance> getInstanceById(int id) {
        return instanceDao.getByIdLive(id);
    }

    // Get instances by course ID
    public LiveData<List<YogaInstance>> getInstancesByCourse(int courseId) {
        return instanceDao.getByCourseIdLive(courseId);
    }

    // Add new instance
    public void addInstance(YogaInstance instance, OnInstanceOperationCallback callback) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                errorMessage.postValue(null);

                // Validate course exists
                YogaCourse course = courseDao.getById(instance.getCourseId());
                if (course == null) {
                    throw new IllegalArgumentException("Course not found");
                }

                // Set metadata
                instance.setSyncStatus(0); // pending sync

                // Insert instance
                long instanceId = instanceDao.insert(instance);
                instance.setId((int) instanceId);

                // Log activity
                logActivity("instance_created", 
                    "Scheduled " + course.getCourseName() + " for " + instance.getDate(),
                    String.valueOf(instanceId));

                // Return success
                callback.onSuccess(instance);
                Log.d(TAG, "Instance added successfully: " + instance.getDate());

            } catch (Exception e) {
                String error = "Failed to add instance: " + e.getMessage();
                Log.e(TAG, error, e);
                errorMessage.postValue(error);
                callback.onError(error);
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Update instance
    public void updateInstance(YogaInstance instance, OnInstanceOperationCallback callback) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                errorMessage.postValue(null);

                // Validate course exists
                YogaCourse course = courseDao.getById(instance.getCourseId());
                if (course == null) {
                    throw new IllegalArgumentException("Course not found");
                }

                // Set metadata
                instance.setSyncStatus(0); // pending sync

                // Update instance
                instanceDao.update(instance);

                // Log activity
                logActivity("instance_updated", 
                    "Updated class instance for " + instance.getDate(),
                    String.valueOf(instance.getId()));

                // Return success
                callback.onSuccess(instance);
                Log.d(TAG, "Instance updated successfully: " + instance.getDate());

            } catch (Exception e) {
                String error = "Failed to update instance: " + e.getMessage();
                Log.e(TAG, error, e);
                errorMessage.postValue(error);
                callback.onError(error);
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Delete instance
    public void deleteInstance(int instanceId, OnInstanceOperationCallback callback) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                errorMessage.postValue(null);

                // Get instance details before deletion for logging
                YogaInstance instanceToDelete = instanceDao.getById(instanceId);
                if (instanceToDelete == null) {
                    throw new IllegalArgumentException("Instance not found");
                }

                YogaCourse course = courseDao.getById(instanceToDelete.getCourseId());
                String courseName = course != null ? course.getCourseName() : "Unknown";

                // Mark instance for deletion (soft delete)
                instanceToDelete.setSyncStatus(2); // 2 = pending delete
                instanceDao.update(instanceToDelete);

                // Log activity
                logActivity("instance_deleted", 
                    "Deleted class instance for " + instanceToDelete.getDate(),
                    String.valueOf(instanceId));

                // Sync deletion to Firebase
                FirebaseService.deleteInstanceFromRealtimeDB(instanceToDelete.getCourseId(), instanceId)
                    .thenCompose(realtimeSuccess -> {
                        if (realtimeSuccess) {
                            return FirebaseService.deleteInstanceFromFirestore(instanceToDelete.getCourseId(), instanceId);
                        }
                        return CompletableFuture.completedFuture(false);
                    })
                    .thenAccept(firestoreSuccess -> {
                        if (firestoreSuccess) {
                            // Now actually delete from local database
                            instanceDao.deleteById(instanceId);
                            Log.d(TAG, "Instance deleted successfully from Firebase and local: " + instanceToDelete.getDate());
                        } else {
                            Log.e(TAG, "Failed to delete instance from Firebase: " + instanceToDelete.getDate());
                        }
                    });

                // Return success
                callback.onSuccess(null);
                Log.d(TAG, "Instance marked for deletion: " + instanceToDelete.getDate());

            } catch (Exception e) {
                String error = "Failed to delete instance: " + e.getMessage();
                Log.e(TAG, error, e);
                errorMessage.postValue(error);
                callback.onError(error);
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Search instances
    public LiveData<List<YogaInstance>> searchInstances(String query) {
        return instanceDao.searchInstances("%" + query + "%");
    }

    // Get instances by date
    public LiveData<List<YogaInstance>> getInstancesByDate(String date) {
        return instanceDao.getByDate(date);
    }

    // Get instances by teacher
    public LiveData<List<YogaInstance>> getInstancesByTeacher(String teacher) {
        return instanceDao.getByTeacher("%" + teacher + "%");
    }

    // Get instances by date range
    public LiveData<List<YogaInstance>> getInstancesByDateRange(String startDate, String endDate) {
        return instanceDao.getByDateRange(startDate, endDate);
    }

    // Get today's instances
    public LiveData<List<YogaInstance>> getTodayInstances() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        return instanceDao.getByDate(today);
    }

    // Get upcoming instances
    public LiveData<List<YogaInstance>> getUpcomingInstances() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        return instanceDao.getUpcoming(today);
    }

    // Get pending sync instances
    public LiveData<List<YogaInstance>> getPendingSyncInstances() {
        return instanceDao.getPendingSync();
    }

    // Mark instance as synced
    public void markInstanceAsSynced(int instanceId) {
        executorService.execute(() -> {
            try {
                instanceDao.updateSyncStatus(instanceId, 1); // synced
                Log.d(TAG, "Instance marked as synced: " + instanceId);
            } catch (Exception e) {
                Log.e(TAG, "Failed to mark instance as synced: " + instanceId, e);
            }
        });
    }

    // Update enrollment
    public void updateEnrollment(int instanceId, int enrolled) {
        executorService.execute(() -> {
            try {
                instanceDao.updateEnrollment(instanceId, enrolled);
            } catch (Exception e) {
                Log.e(TAG, "Failed to update enrollment for instance: " + instanceId, e);
            }
        });
    }

    // Get instance statistics
    public LiveData<Integer> getTotalInstances() {
        return instanceDao.getTotalCount();
    }

    public LiveData<Integer> getTotalEnrollment() {
        return instanceDao.getTotalEnrollment();
    }

    public LiveData<Double> getAverageEnrollment() {
        return instanceDao.getAverageEnrollment();
    }

    // Validate instance data
    public boolean validateInstance(YogaInstance instance) {
        if (instance == null) return false;
        
        return instance.getCourseId() > 0 &&
               instance.getDate() != null && !instance.getDate().trim().isEmpty() &&
               instance.getTeacher() != null && !instance.getTeacher().trim().isEmpty() &&
               instance.getEnrolled() >= 0 &&
               instance.getCapacity() > 0 &&
               instance.getEnrolled() <= instance.getCapacity();
    }

    // Validate date matches course schedule
    public boolean validateDateMatchesCourse(String date, int courseId) {
        try {
            YogaCourse course = courseDao.getById(courseId);
            if (course == null || course.getDaysOfWeek() == null) {
                return false;
            }

            // Parse date to get day of week
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
            Date parsedDate = dateFormat.parse(date);
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            String selectedDay = dayFormat.format(parsedDate);

            // Check if selected day matches course schedule
            String[] courseDays = course.getDaysOfWeek().split(",");
            for (String day : courseDays) {
                if (selectedDay.equalsIgnoreCase(day.trim())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error validating date matches course", e);
            return false;
        }
    }

    // Log activity
    private void logActivity(String type, String description, String relatedId) {
        try {
            Activity activity = new Activity();
            activity.setId(String.valueOf(System.currentTimeMillis()));
            activity.setType(type);
            activity.setDescription(description);
            activity.setTimestamp(getCurrentTimestamp());
            activity.setRelatedId(relatedId);
            
            activityDao.insert(activity);
        } catch (Exception e) {
            Log.e(TAG, "Failed to log activity", e);
        }
    }

    // Get current timestamp
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Cleanup
    public void cleanup() {
        executorService.shutdown();
    }

    // Callback interface
    public interface OnInstanceOperationCallback {
        void onSuccess(YogaInstance instance);
        void onError(String error);
    }
} 