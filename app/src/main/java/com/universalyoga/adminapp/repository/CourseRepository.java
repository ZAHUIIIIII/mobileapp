package com.universalyoga.adminapp.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.ActivityDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.Activity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaInstance;
import com.universalyoga.adminapp.services.FirebaseService;
import java.util.concurrent.CompletableFuture;

public class CourseRepository {
    private static final String TAG = "CourseRepository";
    private final CourseDao courseDao;
    private final ActivityDao activityDao;
    private final InstanceDao instanceDao;
    private final ExecutorService executorService;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public CourseRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.courseDao = database.courseDao();
        this.activityDao = database.activityDao();
        this.instanceDao = database.instanceDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    // LiveData getters
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // Get all courses
    public LiveData<List<YogaCourse>> getAllCourses() {
        return courseDao.getAllLive();
    }

    // Get course by ID
    public LiveData<YogaCourse> getCourseById(int id) {
        return courseDao.getByIdLive(id);
    }

    // Add new course
    public void addCourse(YogaCourse course, OnCourseOperationCallback callback) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                errorMessage.postValue(null);

                // Set metadata
                course.setSyncStatus(0); // pending sync

                // Insert course
                long courseId = courseDao.insert(course);
                course.setId((int) courseId);

                // Log activity
                logActivity("course_created", 
                    "Created course \"" + course.getCourseName() + "\" for " + course.getDaysOfWeek(),
                    String.valueOf(courseId));

                // Return success
                callback.onSuccess(course);
                Log.d(TAG, "Course added successfully: " + course.getCourseName());

            } catch (Exception e) {
                String error = "Failed to add course: " + e.getMessage();
                Log.e(TAG, error, e);
                errorMessage.postValue(error);
                callback.onError(error);
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Update course
    public void updateCourse(YogaCourse course, OnCourseOperationCallback callback) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Starting updateCourse for: " + course.getCourseName() + " (ID: " + course.getId() + ")");
                isLoading.postValue(true);
                errorMessage.postValue(null);

                // Set metadata
                course.setSyncStatus(0); // pending sync

                // Update course
                int updateResult = courseDao.update(course);
                Log.d(TAG, "Course update result: " + updateResult);

                // Update all instances for this course with new time and capacity data
                Log.d(TAG, "Calling updateInstancesForCourse...");
                updateInstancesForCourse(course);

                // Log activity
                logActivity("course_updated", 
                    "Updated course \"" + course.getCourseName() + "\"",
                    String.valueOf(course.getId()));

                // Return success
                callback.onSuccess(course);
                Log.d(TAG, "Course updated successfully: " + course.getCourseName());

            } catch (Exception e) {
                String error = "Failed to update course: " + e.getMessage();
                Log.e(TAG, error, e);
                errorMessage.postValue(error);
                callback.onError(error);
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Update all instances for a course when course details change
    private void updateInstancesForCourse(YogaCourse course) {
        try {
            Log.d(TAG, "Starting updateInstancesForCourse for course: " + course.getCourseName() + " (ID: " + course.getId() + ")");
            Log.d(TAG, "Course time: " + course.getTime() + ", duration: " + course.getDuration() + ", capacity: " + course.getCapacity());
            
            // Get all instances for this course
            List<YogaInstance> instances = instanceDao.getInstancesByCourseSync(course.getId());
            Log.d(TAG, "Found " + (instances != null ? instances.size() : 0) + " instances for course");
            
            if (instances != null && !instances.isEmpty()) {
                for (YogaInstance instance : instances) {
                    Log.d(TAG, "Updating instance ID: " + instance.getId() + 
                          " - Old startTime: " + instance.getStartTime() + 
                          ", Old endTime: " + instance.getEndTime() + 
                          ", Old capacity: " + instance.getCapacity());
                    
                    // Update only the fields that should be auto-updated from course
                    instance.setStartTime(course.getTime());
                    String newEndTime = calculateEndTime(course.getTime(), course.getDuration());
                    instance.setEndTime(newEndTime);
                    instance.setCapacity(course.getCapacity());
                    
                    // Keep existing enrolled count but ensure it doesn't exceed new capacity
                    int oldEnrolled = instance.getEnrolled();
                    if (instance.getEnrolled() > course.getCapacity()) {
                        instance.setEnrolled(course.getCapacity());
                    }
                    
                    // Mark as pending sync
                    instance.setSyncStatus(0);
                    
                    // Update the instance
                    int updateResult = instanceDao.update(instance);
                    Log.d(TAG, "Instance update result: " + updateResult + 
                          " - New startTime: " + instance.getStartTime() + 
                          ", New endTime: " + instance.getEndTime() + 
                          ", New capacity: " + instance.getCapacity() + 
                          ", Enrolled: " + oldEnrolled + " -> " + instance.getEnrolled());
                }
                
                Log.d(TAG, "Successfully updated " + instances.size() + " instances for course: " + course.getCourseName());
            } else {
                Log.d(TAG, "No instances found for course: " + course.getCourseName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating instances for course: " + e.getMessage(), e);
        }
    }

    // Calculate end time based on start time and duration
    private String calculateEndTime(String startTime, int durationMinutes) {
        try {
            Log.d(TAG, "Calculating end time for startTime: " + startTime + ", duration: " + durationMinutes + " minutes");
            
            // Handle different time formats
            String cleanTime = startTime.trim();
            
            // Remove AM/PM if present and convert to 24-hour format
            boolean isPM = cleanTime.toLowerCase().contains("pm");
            cleanTime = cleanTime.replaceAll("(?i)(am|pm)", "").trim();
            
            // Parse start time
            String[] timeParts = cleanTime.split(":");
            if (timeParts.length != 2) {
                Log.e(TAG, "Invalid time format: " + startTime);
                return startTime;
            }
            
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            // Convert to 24-hour format if needed
            if (isPM && hour != 12) {
                hour += 12;
            } else if (!isPM && hour == 12) {
                hour = 0;
            }
            
            Log.d(TAG, "Parsed time - Hour: " + hour + ", Minute: " + minute);
            
            // Add duration
            int totalMinutes = hour * 60 + minute + durationMinutes;
            int endHour = totalMinutes / 60;
            int endMinute = totalMinutes % 60;
            
            // Handle 24-hour overflow
            if (endHour >= 24) {
                endHour -= 24;
            }
            
            // Format end time
            String endTime = String.format(Locale.UK, "%02d:%02d", endHour, endMinute);
            Log.d(TAG, "Calculated end time: " + endTime);
            return endTime;
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating end time for " + startTime + ": " + e.getMessage(), e);
            return startTime; // Return start time as fallback
        }
    }

    // Delete course
    public void deleteCourse(int courseId, OnCourseOperationCallback callback) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                errorMessage.postValue(null);

                // Get course details before deletion for logging
                YogaCourse courseToDelete = courseDao.getById(courseId);
                String courseName = courseToDelete != null ? courseToDelete.getCourseName() : "Unknown";

                // Mark course for deletion (soft delete)
                courseToDelete.setSyncStatus(2); // 2 = pending delete
                courseDao.update(courseToDelete);

                // Log activity
                logActivity("course_deleted", 
                    "Deleted course \"" + courseName + "\"",
                    String.valueOf(courseId));

                // Sync deletion to Firebase
                FirebaseService.deleteCourseFromRealtimeDB(courseId)
                    .thenCompose(realtimeSuccess -> {
                        if (realtimeSuccess) {
                            return FirebaseService.deleteCourseFromFirestore(courseId);
                        }
                        return CompletableFuture.completedFuture(false);
                    })
                    .thenAccept(firestoreSuccess -> {
                        if (firestoreSuccess) {
                            // Now actually delete from local database
                            courseDao.deleteById(courseId);
                            Log.d(TAG, "Course deleted successfully from Firebase and local: " + courseName);
                        } else {
                            Log.e(TAG, "Failed to delete course from Firebase: " + courseName);
                        }
                    });

                // Return success
                callback.onSuccess(null);
                Log.d(TAG, "Course marked for deletion: " + courseName);

            } catch (Exception e) {
                String error = "Failed to delete course: " + e.getMessage();
                Log.e(TAG, error, e);
                errorMessage.postValue(error);
                callback.onError(error);
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Search courses
    public LiveData<List<YogaCourse>> searchCourses(String query) {
        return courseDao.searchCourses("%" + query + "%");
    }

    // Get courses by day
    public LiveData<List<YogaCourse>> getCoursesByDay(String day) {
        return courseDao.getByDay("%" + day + "%");
    }

    // Get courses by type
    public LiveData<List<YogaCourse>> getCoursesByType(String type) {
        return courseDao.getByType("%" + type + "%");
    }

    // Get courses by difficulty
    public LiveData<List<YogaCourse>> getCoursesByDifficulty(String difficulty) {
        return courseDao.getByDifficulty("%" + difficulty + "%");
    }

    // Get pending sync courses
    public LiveData<List<YogaCourse>> getPendingSyncCourses() {
        return courseDao.getPendingSync();
    }

    // Mark course as synced
    public void markCourseAsSynced(int courseId) {
        executorService.execute(() -> {
            try {
                courseDao.updateSyncStatus(courseId, 1); // synced
                Log.d(TAG, "Course marked as synced: " + courseId);
            } catch (Exception e) {
                Log.e(TAG, "Failed to mark course as synced: " + courseId, e);
            }
        });
    }

    // Statistics methods
    public LiveData<Integer> getCount() {
        return courseDao.getCount();
    }

    public LiveData<Integer> getTotalCapacity() {
        return courseDao.getTotalCapacity();
    }

    public LiveData<Double> getAveragePrice() {
        return courseDao.getAveragePrice();
    }

    // Validate course data
    public boolean validateCourse(YogaCourse course) {
        if (course == null) return false;
        
        return course.getCourseName() != null && !course.getCourseName().trim().isEmpty() &&
               course.getDaysOfWeek() != null && !course.getDaysOfWeek().trim().isEmpty() &&
               course.getTime() != null && !course.getTime().trim().isEmpty() &&
               course.getCapacity() > 0 &&
               course.getDuration() > 0 &&
               course.getPrice() >= 0;
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
    public interface OnCourseOperationCallback {
        void onSuccess(YogaCourse course);
        void onError(String error);
    }

    // Public method to manually update instances for a course (for testing/debugging)
    public void manuallyUpdateInstancesForCourse(int courseId) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Manually updating instances for course ID: " + courseId);
                YogaCourse course = courseDao.getById(courseId);
                if (course != null) {
                    updateInstancesForCourse(course);
                } else {
                    Log.e(TAG, "Course not found with ID: " + courseId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in manual instance update: " + e.getMessage(), e);
            }
        });
    }
} 