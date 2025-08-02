package com.universalyoga.adminapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.List;

@Dao
public interface InstanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(YogaInstance instance);
    
    @Update
    int update(YogaInstance instance);
    
    @Delete
    int delete(YogaInstance instance);
    
    @Query("DELETE FROM instances")
    void deleteAll();
    
    @Query("SELECT * FROM instances ORDER BY id DESC")
    LiveData<List<YogaInstance>> getAllLive();
    
    @Query("SELECT * FROM instances ORDER BY id DESC")
    List<YogaInstance> getAll();
    
    @Query("SELECT * FROM instances WHERE courseId = :courseId")
    LiveData<List<YogaInstance>> getByCourse(int courseId);
    
    @Query("SELECT * FROM instances WHERE courseId = :courseId")
    List<YogaInstance> getInstancesByCourseSync(int courseId);
    
    @Query("SELECT * FROM instances WHERE id = :id LIMIT 1")
    YogaInstance getById(int id);
    
    @Query("SELECT * FROM instances WHERE id = :id LIMIT 1")
    LiveData<YogaInstance> getByIdLive(int id);
    
    @Query("SELECT * FROM instances WHERE courseId = :courseId")
    LiveData<List<YogaInstance>> getByCourseIdLive(int courseId);
    
    // Search methods
    @Query("SELECT * FROM instances WHERE " +
           "teacher LIKE :query OR " +
           "date LIKE :query OR " +
           "comments LIKE :query")
    LiveData<List<YogaInstance>> searchInstances(String query);
    
    // Filter methods
    @Query("SELECT * FROM instances WHERE date = :date")
    LiveData<List<YogaInstance>> getByDate(String date);
    
    @Query("SELECT * FROM instances WHERE teacher LIKE :teacher")
    LiveData<List<YogaInstance>> getByTeacher(String teacher);
    
    @Query("SELECT * FROM instances WHERE date BETWEEN :startDate AND :endDate")
    LiveData<List<YogaInstance>> getByDateRange(String startDate, String endDate);
    
    @Query("SELECT * FROM instances WHERE date >= :date ORDER BY date ASC")
    LiveData<List<YogaInstance>> getUpcoming(String date);
    
    // Sync status methods
    @Query("SELECT * FROM instances WHERE syncStatus = 0")
    LiveData<List<YogaInstance>> getPendingSync();
    
    @Query("UPDATE instances SET syncStatus = :status WHERE id = :instanceId")
    void updateSyncStatus(int instanceId, int status);
    
    // Update enrollment
    @Query("UPDATE instances SET enrolled = :enrolled WHERE id = :instanceId")
    void updateEnrollment(int instanceId, int enrolled);
    
    // Delete by ID
    @Query("DELETE FROM instances WHERE id = :instanceId")
    void deleteById(int instanceId);
    
    // Statistics
    @Query("SELECT COUNT(*) FROM instances")
    LiveData<Integer> getTotalCount();
    
    @Query("SELECT SUM(enrolled) FROM instances")
    LiveData<Integer> getTotalEnrollment();
    
    @Query("SELECT AVG(enrolled) FROM instances")
    LiveData<Double> getAverageEnrollment();
    
    @Query("SELECT COUNT(*) FROM instances WHERE date >= :date")
    LiveData<Integer> getUpcomingCount(String date);
    
    @Query("SELECT COUNT(*) FROM instances WHERE date = :date")
    LiveData<Integer> getTodayCount(String date);
    
    // Get all unique teachers
    @Query("SELECT DISTINCT teacher FROM instances WHERE teacher IS NOT NULL AND teacher != '' ORDER BY teacher ASC")
    List<String> getAllTeachers();
    
    // Count method for database management
    @Query("SELECT COUNT(*) FROM instances")
    int getInstanceCount();
    
    @Query("SELECT COUNT(*) FROM instances WHERE syncStatus = 0")
    int getUnsyncedCount();
    
    @Query("SELECT * FROM instances WHERE syncStatus = 2")
    List<YogaInstance> getPendingDeletions();
}