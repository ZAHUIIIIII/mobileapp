package com.universalyoga.adminapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.adapters.SyncHistoryAdapter;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.SyncHistory;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import com.universalyoga.adminapp.network.ApiClient;
import com.universalyoga.adminapp.network.ApiService;
import com.universalyoga.adminapp.utils.NetworkUtils;
import com.universalyoga.adminapp.services.FirebaseService;
import com.universalyoga.adminapp.services.AutoSyncService;
import com.universalyoga.adminapp.utils.DatabaseResetUtil;
import com.universalyoga.adminapp.utils.ToastHelper;
import com.universalyoga.adminapp.activities.DatabaseManagementActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.HashMap;

public class UploadFragment extends Fragment {
    private MaterialButton btnSync, btnRetry;
    private MaterialButton btnViewAll;
    private MaterialButton btnDatabaseManagement, btnResetAllData;
    private ProgressBar progressBar;
    private TextView tvNetworkStatus, tvLastSync, tvDataSummary, tvDataVolume;
    private TextView tvTotalSyncs, tvProgressText;
    private SwitchMaterial switchAutoSync;
    private View emptyStateLayout;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    private ApiService apiService;
    private RecyclerView rvSyncHistory;
    private SyncHistoryAdapter syncHistoryAdapter;
    private AutoSyncService autoSyncService;
    private DatabaseResetUtil resetUtil;
    private boolean isManualSyncInProgress = false;
    private android.widget.ImageView ivNetworkIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize database DAOs
        AppDatabase db = AppDatabase.getInstance(requireContext());
        courseDao = db.courseDao();
        instanceDao = db.instanceDao();
        apiService = ApiClient.get().create(ApiService.class);
        autoSyncService = new AutoSyncService(requireContext());
        resetUtil = new DatabaseResetUtil(requireContext());
        
        // Initialize views
        btnSync = view.findViewById(R.id.btnSync);
        btnRetry = view.findViewById(R.id.btnRetry);
        btnViewAll = view.findViewById(R.id.btnViewAll);
        btnDatabaseManagement = view.findViewById(R.id.btnDatabaseManagement);
        btnResetAllData = view.findViewById(R.id.btnResetAllData);
        progressBar = view.findViewById(R.id.progressBar);
        tvNetworkStatus = view.findViewById(R.id.tvNetworkStatus);
        tvLastSync = view.findViewById(R.id.tvLastSync);
        tvDataSummary = view.findViewById(R.id.tvDataSummary);
        tvDataVolume = view.findViewById(R.id.tvDataVolume);
        tvTotalSyncs = view.findViewById(R.id.tvTotalSyncs);
        tvProgressText = view.findViewById(R.id.tvProgressText);
        switchAutoSync = view.findViewById(R.id.switchAutoSync);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        rvSyncHistory = view.findViewById(R.id.rvSyncHistory);
        
        // Initialize network icon
        ivNetworkIcon = view.findViewById(R.id.ivNetworkIcon);
        
        // Set up click listeners
        btnSync.setOnClickListener(v -> {
            if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                Toast.makeText(requireContext(), "No internet connection available.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!FirebaseService.isInitialized()) {
                Toast.makeText(requireContext(), "Firebase not initialized. Please check your configuration.", Toast.LENGTH_LONG).show();
                return;
            }
            
            performUpload();
        });
        
        btnRetry.setOnClickListener(v -> performUpload());
        btnViewAll.setOnClickListener(v -> showAllSyncHistory());
        // btnClearErrors.setOnClickListener(v -> clearAllErrors()); // This line is removed
        
        // Database management button listeners
        btnDatabaseManagement.setOnClickListener(v -> openDatabaseManagement());
        btnResetAllData.setOnClickListener(v -> showResetConfirmation());
        
        // Set up auto-sync switch - disabled by default
        switchAutoSync.setChecked(false); // Always start disabled
        switchAutoSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (autoSyncService != null) {
                autoSyncService.setAutoSyncEnabled(isChecked);
                Toast.makeText(requireContext(), 
                    "Auto sync " + (isChecked ? "enabled" : "disabled"), 
                    Toast.LENGTH_SHORT).show();
            }
        });
        
        // Set up RecyclerViews
        rvSyncHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Set up adapters
        syncHistoryAdapter = new SyncHistoryAdapter();
        rvSyncHistory.setAdapter(syncHistoryAdapter);
        
        // Set up sync error adapter with retry functionality
        // syncErrorAdapter.setActionListener(error -> { // This line is removed
        //     // Retry the failed sync // This line is removed
        //     Toast.makeText(requireContext(), "Retrying failed sync...", Toast.LENGTH_SHORT).show(); // This line is removed
        //     performUpload(); // This line is removed
        // }); // This line is removed
        
        // Observe LiveData
        db.syncHistoryDao().getAllLive().observe(getViewLifecycleOwner(), new Observer<List<SyncHistory>>() {
            @Override
            public void onChanged(List<SyncHistory> histories) {
                syncHistoryAdapter.setData(histories != null ? histories : new ArrayList<>());
                updateEmptyState(histories);
                updateSyncStatistics(histories);
                // updateSyncErrors(histories); // This line is removed
            }
        });
        
        // Initialize UI
        updateDataSummary();
        updateNetworkStatus();
        updateLastSyncTime();
        
        // Set up periodic network status check
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateNetworkStatus();
                handler.postDelayed(this, 5000); // Check every 5 seconds
            }
        }, 5000);
    }
    
    private void performUpload() {
        isManualSyncInProgress = true;
        btnSync.setEnabled(false);
        btnRetry.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        tvProgressText.setText("0% - Preparing upload...");
        progressBar.setProgress(0);

        // Create initial sync history record
        SyncHistory history = SyncHistory.createWithId();
        history.setTimestamp(String.valueOf(System.currentTimeMillis()));
        history.setStatus("in_progress");
        history.setType("manual");
        history.setTrigger("user");
        
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            db.syncHistoryDao().insert(history);
        });
        
        // Perform upload
        Executors.newSingleThreadExecutor().execute(() -> {
            List<YogaCourse> coursesToUpload = courseDao.getAll();
            List<YogaInstance> instancesToUpload = instanceDao.getAll();

            requireActivity().runOnUiThread(() -> {
                if (coursesToUpload.isEmpty() && instancesToUpload.isEmpty()) {
                    tvProgressText.setText("Clearing Firebase data (local DB is empty)...");
                    progressBar.setProgress(25);
                } else {
                    tvProgressText.setText("5% - Starting Firebase upload...");
                }
            });

            // Sync pending deletions first
            syncPendingDeletions();

            // If local database is empty, clear Firebase data
            if (coursesToUpload.isEmpty() && instancesToUpload.isEmpty()) {
                clearFirebaseData();
            } else {
                // Perform actual Firebase upload
                uploadAllYogaClasses(requireContext(), coursesToUpload, instancesToUpload);
            }
        });
    }
    
    private void uploadAllYogaClasses(android.content.Context context, List<YogaCourse> courses, List<YogaInstance> instances) {
        // Update progress to show upload starting
        requireActivity().runOnUiThread(() -> {
            progressBar.setProgress(10);
            tvProgressText.setText("10% - Starting upload...");
        });
        
        // Upload to Realtime Database first, then to Firestore
        FirebaseService.uploadCoursesToRealtimeDB(courses, instances)
            .thenCompose(realtimeSuccess -> {
                requireActivity().runOnUiThread(() -> {
                    if (realtimeSuccess) {
                        progressBar.setProgress(50);
                        tvProgressText.setText("50% - Realtime DB uploaded, uploading to Firestore...");
                    } else {
                        progressBar.setProgress(25);
                        tvProgressText.setText("25% - Realtime DB failed, trying Firestore...");
                    }
                });
                
                // Upload to Firestore
                return FirebaseService.uploadCoursesToFirestore(courses, instances);
            })
            .thenAccept(firestoreSuccess -> {
                requireActivity().runOnUiThread(() -> {
                    if (firestoreSuccess) {
                        progressBar.setProgress(100);
                        tvProgressText.setText("100% - Upload completed successfully!");
                        
                        // Update sync history
                        updateSyncHistory(true, "Manual sync completed", courses.size() + instances.size());
                        
                        Toast.makeText(context, "Data synced to Firebase successfully!", Toast.LENGTH_LONG).show();
                    } else {
                        progressBar.setProgress(75);
                        tvProgressText.setText("75% - Partial upload completed");
                        Toast.makeText(context, "Partial upload completed", Toast.LENGTH_SHORT).show();
                    }
                    resetUploadState();
                });
            })
            .exceptionally(throwable -> {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setProgress(0);
                    tvProgressText.setText("Upload failed");
                    Toast.makeText(context, "Upload failed: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    resetUploadState();
                });
                return null;
            });
    }
    
    private void showAllSyncHistory() {
        // Show all sync history in a dialog or navigate to a detailed view
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            List<SyncHistory> allHistory = db.syncHistoryDao().getAll();
            requireActivity().runOnUiThread(() -> {
                if (allHistory.isEmpty()) {
                    Toast.makeText(requireContext(), "No sync history available", Toast.LENGTH_SHORT).show();
                } else {
                    StringBuilder message = new StringBuilder();
                    message.append("Total syncs: ").append(allHistory.size()).append("\n\n");
                    
                    for (SyncHistory history : allHistory) {
                        message.append("Status: ").append(history.getStatus()).append("\n");
                        message.append("Time: ").append(history.getTimestamp()).append("\n");
                        message.append("Type: ").append(history.getType()).append("\n");
                        message.append("---\n");
                    }
                    
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Sync History")
                        .setMessage(message.toString())
                        .setPositiveButton("OK", null)
                        .show();
                }
            });
        });
    }
    
    private void resetUploadState() {
        btnSync.setEnabled(true);
        btnRetry.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        isManualSyncInProgress = false;
    }
    
    private void updateDataSummary() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int courseCount = courseDao.getAll().size();
            int instanceCount = instanceDao.getAll().size();
            int unsyncedCourses = courseDao.getUnsyncedCount();
            int unsyncedInstances = instanceDao.getUnsyncedCount();
            int totalUnsynced = unsyncedCourses + unsyncedInstances;
            
            // Calculate approximate data size (rough estimate)
            int dataSizeKB = (courseCount * 2) + (instanceCount * 3); // Rough estimate
            
            requireActivity().runOnUiThread(() -> {
                tvDataSummary.setText(String.format("Courses: %d | Instances: %d | Unsynced: %d", 
                    courseCount, instanceCount, totalUnsynced));
                tvDataVolume.setText(String.format("Data Size: %dKB", dataSizeKB));
            });
        });
    }
    
    private void updateNetworkStatus() {
        boolean isOnline = NetworkUtils.isNetworkAvailable(requireContext());
        String firebaseStatus = FirebaseService.getConnectionStatus();
        
        // Update the network status text
        tvNetworkStatus.setText("Network: " + (isOnline ? "Connected" : "Disconnected") + " | Firebase: " + firebaseStatus);
        
        // Update the network status icon
        if (isOnline) {
            ivNetworkIcon.setImageResource(R.drawable.ic_network_connected);
            ivNetworkIcon.setColorFilter(requireContext().getResources().getColor(R.color.green_600, null));
        } else {
            ivNetworkIcon.setImageResource(R.drawable.ic_network_disconnected);
            ivNetworkIcon.setColorFilter(requireContext().getResources().getColor(R.color.destructive, null));
        }
    }
    
    private void updateEmptyState(List<SyncHistory> histories) {
        if (histories == null || histories.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            rvSyncHistory.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvSyncHistory.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateSyncStatistics(List<SyncHistory> histories) {
        if (histories == null || histories.isEmpty()) {
            tvTotalSyncs.setText("0");
            return;
        }

        int totalSyncs = histories.size();
        tvTotalSyncs.setText(String.valueOf(totalSyncs));
    }

    private void updateLastSyncTime() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            SyncHistory lastSync = db.syncHistoryDao().getLastSync();
            requireActivity().runOnUiThread(() -> {
                if (lastSync != null && lastSync.getTimestamp() != null) {
                    try {
                        long timestamp = Long.parseLong(lastSync.getTimestamp());
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                        String formattedTime = sdf.format(new java.util.Date(timestamp));
                        tvLastSync.setText("Last Sync: " + formattedTime);
                    } catch (NumberFormatException e) {
                        tvLastSync.setText("Last Sync: " + lastSync.getTimestamp());
                    }
                } else {
                    tvLastSync.setText("Last Sync: Never");
                }
            });
        });
    }
    
    private void updateSyncHistory(boolean success, String message, int recordCount) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                SyncHistory history = SyncHistory.createWithId();
                history.setTimestamp(String.valueOf(System.currentTimeMillis()));
                history.setStatus(success ? "success" : "failed");
                history.setType("manual");
                history.setTrigger("user");
                history.setDataSize(recordCount);
                
                db.syncHistoryDao().insert(history);
                
            } catch (Exception e) {
                Log.e("Upload", "Failed to update sync history", e);
            }
        });
    }
    
    // Sync pending deletions to Firebase
    private void syncPendingDeletions() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Get courses and instances marked for deletion
                List<YogaCourse> coursesToDelete = courseDao.getPendingDeletions();
                List<YogaInstance> instancesToDelete = instanceDao.getPendingDeletions();
                
                if (coursesToDelete.isEmpty() && instancesToDelete.isEmpty()) {
                    Log.d("UploadFragment", "No pending deletions to sync");
                    return;
                }
                
                // Prepare deletion lists
                List<Integer> courseIdsToDelete = new ArrayList<>();
                List<Map<String, Integer>> instanceIdsToDelete = new ArrayList<>();
                
                for (YogaCourse course : coursesToDelete) {
                    courseIdsToDelete.add(course.getId());
                }
                
                for (YogaInstance instance : instancesToDelete) {
                    Map<String, Integer> instanceInfo = new HashMap<>();
                    instanceInfo.put("courseId", instance.getCourseId());
                    instanceInfo.put("instanceId", instance.getId());
                    instanceIdsToDelete.add(instanceInfo);
                }
                
                // Sync deletions to Firebase
                FirebaseService.syncDeletionsToFirebase(courseIdsToDelete, instanceIdsToDelete)
                    .thenAccept(success -> {
                        if (success) {
                            Log.d("UploadFragment", "Pending deletions synced successfully");
                            // Clear pending deletions from local database
                            for (YogaCourse course : coursesToDelete) {
                                courseDao.deleteById(course.getId());
                            }
                            for (YogaInstance instance : instancesToDelete) {
                                instanceDao.deleteById(instance.getId());
                            }
                        } else {
                            Log.e("UploadFragment", "Failed to sync pending deletions");
                        }
                    });
                
            } catch (Exception e) {
                Log.e("UploadFragment", "Error syncing pending deletions", e);
            }
        });
    }

    private void clearFirebaseData() {
        requireActivity().runOnUiThread(() -> {
            progressBar.setProgress(50);
            tvProgressText.setText("50% - Clearing Firebase data...");
        });
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Log.d("UploadFragment", "Clearing all Firebase data due to empty local database.");
                
                // Clear Firebase data
                FirebaseService.clearAllFirebaseData();
                
                // Wait a bit for Firebase operations to complete
                Thread.sleep(2000);
                
                requireActivity().runOnUiThread(() -> {
                    progressBar.setProgress(100);
                    tvProgressText.setText("100% - Firebase data cleared successfully!");
                    
                    // Update sync history
                    updateSyncHistory(true, "Firebase data cleared (local DB was empty)", 0);
                    
                    // Show success message
                    ToastHelper.showSuccessToast(requireContext(), "All Firebase data cleared successfully");
                    
                    // Update data summary
                    updateDataSummary();
                    
                    // Reset upload state
                    resetUploadState();
                });
                
            } catch (Exception e) {
                Log.e("UploadFragment", "Error clearing Firebase data", e);
                requireActivity().runOnUiThread(() -> {
                    progressBar.setProgress(0);
                    tvProgressText.setText("Failed to clear Firebase data");
                    
                    // Update sync history with error
                    updateSyncHistory(false, "Failed to clear Firebase data: " + e.getMessage(), 0);
                    
                    // Show error message
                    ToastHelper.showErrorToast(requireContext(), "Failed to clear Firebase data: " + e.getMessage());
                    
                    // Reset upload state
                    resetUploadState();
                });
            }
        });
    }
    
    // Database Management Methods
    
    private void openDatabaseManagement() {
        android.content.Intent intent = new android.content.Intent(requireContext(), DatabaseManagementActivity.class);
        startActivity(intent);
    }
    
    private void showResetConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Reset All Data")
            .setMessage("⚠️ This will permanently delete ALL data including courses, instances, activities, and Firebase data. This action cannot be undone. Are you sure?")
            .setPositiveButton("Reset All", (dialog, which) -> {
                resetUtil.resetAllData(new DatabaseResetUtil.ResetCallback() {
                    @Override
                    public void onResetStarted() {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Resetting all data...", Toast.LENGTH_SHORT).show();
                        });
                    }
                    
                    @Override
                    public void onResetCompleted() {
                        requireActivity().runOnUiThread(() -> {
                            ToastHelper.showSuccessToast(requireContext(), "All data reset successfully");
                            updateDataSummary();
                        });
                    }
                    
                    @Override
                    public void onResetFailed(String error) {
                        requireActivity().runOnUiThread(() -> {
                            ToastHelper.showErrorToast(requireContext(), "Reset failed: " + error);
                        });
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}