package com.universalyoga.adminapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.universalyoga.adminapp.models.YogaCourse;
import java.util.List;

@Dao
public interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(YogaCourse course);
    
    @Update
    int update(YogaCourse course);
    
    @Delete
    int delete(YogaCourse course);
    
    @Query("DELETE FROM courses")
    void deleteAll();
    
    @Query("SELECT * FROM courses ORDER BY id DESC")
    LiveData<List<YogaCourse>> getAllLive();
    
    @Query("SELECT * FROM courses ORDER BY id DESC")
    List<YogaCourse> getAll();
    
    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    YogaCourse getById(int id);

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    LiveData<YogaCourse> getByIdLive(int id);

    @Query("SELECT * FROM courses WHERE " +
           "type LIKE :query OR " +
           "daysOfWeek LIKE :query OR " +
           "difficulty LIKE :query OR " +
           "instructor LIKE :query")
    LiveData<List<YogaCourse>> searchCourses(String query);
    
    // Filter methods
    @Query("SELECT * FROM courses WHERE daysOfWeek LIKE :day")
    LiveData<List<YogaCourse>> getByDay(String day);
    
    @Query("SELECT * FROM courses WHERE type LIKE :type")
    LiveData<List<YogaCourse>> getByType(String type);
    
    @Query("SELECT * FROM courses WHERE difficulty LIKE :difficulty")
    LiveData<List<YogaCourse>> getByDifficulty(String difficulty);
    
    @Query("SELECT * FROM courses WHERE instructor LIKE :instructor")
    LiveData<List<YogaCourse>> getByInstructor(String instructor);
    
    // Sync status methods
    @Query("SELECT * FROM courses WHERE syncStatus = 0")
    LiveData<List<YogaCourse>> getPendingSync();
    
    @Query("UPDATE courses SET syncStatus = :status WHERE id = :courseId")
    void updateSyncStatus(int courseId, int status);
    
    // Delete by ID
    @Query("DELETE FROM courses WHERE id = :courseId")
    void deleteById(int courseId);
    
    // Statistics
    @Query("SELECT COUNT(*) FROM courses")
    LiveData<Integer> getCount();
    
    @Query("SELECT SUM(capacity) FROM courses")
    LiveData<Integer> getTotalCapacity();
    
    @Query("SELECT AVG(price) FROM courses")
    LiveData<Double> getAveragePrice();
    
    // Additional methods for repository (keeping for backward compatibility)
    @Query("SELECT * FROM courses ORDER BY id DESC")
    List<YogaCourse> getAllCourses();
    
    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    YogaCourse getCourseById(int id);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCourse(YogaCourse course);
    
    @Update
    void updateCourse(YogaCourse course);
    
    @Delete
    void deleteCourse(YogaCourse course);
    
    @Query("DELETE FROM courses")
    void deleteAllCourses();
    
    @Query("SELECT * FROM courses WHERE daysOfWeek LIKE :dayOfWeek")
    List<YogaCourse> getCoursesByDay(String dayOfWeek);
    
    @Query("SELECT COUNT(*) FROM courses")
    int getCourseCount();
    
    @Query("SELECT COUNT(*) FROM courses WHERE syncStatus = 0")
    int getUnsyncedCount();
    
    @Query("SELECT * FROM courses WHERE syncStatus = 2")
    List<YogaCourse> getPendingDeletions();
} 