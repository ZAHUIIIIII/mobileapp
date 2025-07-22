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

    @Query("SELECT * FROM courses WHERE " +
           "courseName LIKE :query OR " +
           "daysOfWeek LIKE :query OR " +
           "id IN (SELECT courseId FROM instances WHERE teacher LIKE :query OR date LIKE :query)")
    List<YogaCourse> searchCourses(String query);
} 