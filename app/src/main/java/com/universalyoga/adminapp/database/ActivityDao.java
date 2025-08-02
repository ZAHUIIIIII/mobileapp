package com.universalyoga.adminapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.lifecycle.LiveData;
import com.universalyoga.adminapp.models.Activity;
import java.util.List;

@Dao
public interface ActivityDao {
    @Insert
    void insert(Activity activity);

    @Update
    void update(Activity activity);

    @Delete
    void delete(Activity activity);

    @Query("DELETE FROM activity")
    void deleteAll();

    @Query("SELECT * FROM activity ORDER BY timestamp DESC")
    LiveData<List<Activity>> getAllLive();

    @Query("SELECT * FROM activity ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<Activity>> getRecentLive(int limit);

    @Query("SELECT * FROM activity WHERE id = :id LIMIT 1")
    Activity getById(String id);
    
    // Count method for database management
    @Query("SELECT COUNT(*) FROM activity")
    int getActivityCount();
} 