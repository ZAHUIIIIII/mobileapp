package com.universalyoga.adminapp.services;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.SyncHistory;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import com.universalyoga.adminapp.utils.NetworkUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoSyncService {
    private static final String TAG = "AutoSyncService";
    private static final int SYNC_DELAY_SECONDS = 300; // Delay before auto-sync after data change (5 minutes)
    
    private final Context context;
    private final CourseDao courseDao;
    private final InstanceDao instanceDao;
    private final ScheduledExecutorService scheduler;
    private final MutableLiveData<Boolean> isSyncing;
    private final MutableLiveData<String> syncStatus;
    
    private boolean autoSyncEnabled = false; // Disabled by default
    private CompletableFuture<Boolean> pendingSync;
    
    public AutoSyncService(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getInstance(context);
        this.courseDao = db.courseDao();
        this.instanceDao = db.instanceDao();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.isSyncing = new MutableLiveData<>(false);
        this.syncStatus = new MutableLiveData<>("Ready");
    }
    
    /**
     * Enable or disable automatic synchronization
     */
    public void setAutoSyncEnabled(boolean enabled) {
        this.autoSyncEnabled = enabled;
    }
    
    /**
     * Get auto sync status
     */
    public boolean isAutoSyncEnabled() {
        return autoSyncEnabled;
    }
    
    /**
     * Get syncing status LiveData
     */
    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }
    
    /**
     * Get sync status LiveData
     */
    public LiveData<String> getSyncStatus() {
        return syncStatus;
    }
    
    /**
     * Trigger automatic sync when data changes
     * This method should be called whenever local data is modified
     */
    public void triggerAutoSync() {
        if (!autoSyncEnabled) {
            return;
        }
        
        if (!NetworkUtils.isNetworkAvailable(context)) {
            syncStatus.postValue("No network - sync pending");
            return;
        }
        
        if (!FirebaseService.isInitialized()) {
            syncStatus.postValue("Firebase not ready - sync pending");
            return;
        }
        
        // Cancel any pending sync
        if (pendingSync != null && !pendingSync.isDone()) {
            pendingSync.cancel(true);
        }
        
        // Schedule sync with delay to avoid multiple rapid syncs
        scheduler.schedule(() -> {
            performAutoSync();
        }, SYNC_DELAY_SECONDS, TimeUnit.SECONDS);
        
        syncStatus.postValue("Auto sync scheduled...");
    }
    
    /**
     * Perform the actual automatic synchronization
     */
    private void performAutoSync() {
        if (isSyncing.getValue() != null && isSyncing.getValue()) {
            return;
        }
        
        isSyncing.postValue(true);
        syncStatus.postValue("Auto syncing...");
        
        // Create sync history record
        SyncHistory history = SyncHistory.createWithId();
        history.setTimestamp(String.valueOf(System.currentTimeMillis()));
        history.setStatus("in_progress");
        history.setType("auto");
        history.setTrigger("data_change");
        
        AppDatabase db = AppDatabase.getInstance(context);
        db.syncHistoryDao().insert(history);
        
        // Get all data to sync
        List<YogaCourse> courses = courseDao.getAll();
        List<YogaInstance> instances = instanceDao.getAll();
        
        // Perform upload
        pendingSync = FirebaseService.uploadCoursesToRealtimeDB(courses, instances)
            .thenCompose(realtimeSuccess -> {
                return FirebaseService.uploadCoursesToFirestore(courses, instances);
            })
            .thenApply(firestoreSuccess -> {
                
                // Update sync history
                history.setStatus(firestoreSuccess ? "success" : "failed");
                history.setDataSize(courses.size() + instances.size());
                db.syncHistoryDao().update(history);
                
                // Update UI
                isSyncing.postValue(false);
                if (firestoreSuccess) {
                    syncStatus.postValue("Auto sync completed");
                } else {
                    syncStatus.postValue("Auto sync failed");
                }
                
                return firestoreSuccess;
            })
            .exceptionally(throwable -> {
                Log.e(TAG, "Auto sync failed with exception", throwable);
                
                // Update sync history
                history.setStatus("failed");
                db.syncHistoryDao().update(history);
                
                // Update UI
                isSyncing.postValue(false);
                syncStatus.postValue("Auto sync failed: " + throwable.getMessage());
                
                return false;
            });
    }
    
    /**
     * Force immediate sync (bypasses auto sync settings)
     */
    public CompletableFuture<Boolean> forceSync() {
        Log.d(TAG, "Force sync requested");
        syncStatus.postValue("Force syncing...");
        
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.d(TAG, "No network available for force sync");
            syncStatus.postValue("No network available");
            return CompletableFuture.completedFuture(false);
        }
        
        if (!FirebaseService.isInitialized()) {
            Log.d(TAG, "Firebase not initialized for force sync");
            syncStatus.postValue("Firebase not ready");
            return CompletableFuture.completedFuture(false);
        }
        
        isSyncing.postValue(true);
        syncStatus.postValue("Force syncing...");
        
        Log.d(TAG, "Starting force sync");
        
        // Create sync history record
        SyncHistory history = SyncHistory.createWithId();
        history.setTimestamp(String.valueOf(System.currentTimeMillis()));
        history.setStatus("in_progress");
        history.setType("force");
        history.setTrigger("user");
        
        AppDatabase db = AppDatabase.getInstance(context);
        db.syncHistoryDao().insert(history);
        
        // Get all data to sync
        List<YogaCourse> courses = courseDao.getAll();
        List<YogaInstance> instances = instanceDao.getAll();
        
        Log.d(TAG, "Force syncing " + courses.size() + " courses and " + instances.size() + " instances");
        
        // Perform upload
        return FirebaseService.uploadCoursesToRealtimeDB(courses, instances)
            .thenCompose(realtimeSuccess -> {
                Log.d(TAG, "Realtime DB force sync: " + (realtimeSuccess ? "success" : "failed"));
                return FirebaseService.uploadCoursesToFirestore(courses, instances);
            })
            .thenApply(firestoreSuccess -> {
                Log.d(TAG, "Firestore force sync: " + (firestoreSuccess ? "success" : "failed"));
                
                // Update sync history
                history.setStatus(firestoreSuccess ? "success" : "failed");
                history.setDataSize(courses.size() + instances.size());
                db.syncHistoryDao().update(history);
                
                // Update UI
                isSyncing.postValue(false);
                if (firestoreSuccess) {
                    syncStatus.postValue("Force sync completed");
                    Log.d(TAG, "Force sync completed successfully");
                } else {
                    syncStatus.postValue("Force sync failed");
                    Log.e(TAG, "Force sync failed");
                }
                
                return firestoreSuccess;
            })
            .exceptionally(throwable -> {
                Log.e(TAG, "Force sync failed with exception", throwable);
                
                // Update sync history
                history.setStatus("failed");
                db.syncHistoryDao().update(history);
                
                // Update UI
                isSyncing.postValue(false);
                syncStatus.postValue("Force sync failed: " + throwable.getMessage());
                
                return false;
            });
    }
    
    /**
     * Shutdown the service
     */
    public void shutdown() {
        if (pendingSync != null && !pendingSync.isDone()) {
            pendingSync.cancel(true);
        }
        scheduler.shutdown();
        Log.d(TAG, "AutoSyncService shutdown");
    }
} 