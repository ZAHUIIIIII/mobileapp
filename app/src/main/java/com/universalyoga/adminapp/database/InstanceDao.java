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
}