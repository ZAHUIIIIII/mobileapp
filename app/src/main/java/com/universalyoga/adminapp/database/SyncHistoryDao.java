package com.universalyoga.adminapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.lifecycle.LiveData;
import com.universalyoga.adminapp.models.SyncHistory;
import java.util.List;

@Dao
public interface SyncHistoryDao {
    @Insert
    void insert(SyncHistory history);

    @Update
    void update(SyncHistory history);

    @Delete
    void delete(SyncHistory history);

    @Query("DELETE FROM sync_history")
    void deleteAll();

    @Query("SELECT * FROM sync_history ORDER BY timestamp DESC")
    LiveData<List<SyncHistory>> getAllLive();

    @Query("SELECT * FROM sync_history ORDER BY timestamp DESC")
    List<SyncHistory> getAll();

    @Query("SELECT * FROM sync_history WHERE id = :id LIMIT 1")
    SyncHistory getById(String id);
    
    @Query("SELECT * FROM sync_history WHERE status = 'success' ORDER BY timestamp DESC LIMIT 1")
    SyncHistory getLastSync();
    
    // Count method for database management
    @Query("SELECT COUNT(*) FROM sync_history")
    int getSyncHistoryCount();
} 