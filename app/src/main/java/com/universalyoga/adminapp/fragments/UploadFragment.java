package com.universalyoga.adminapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import com.universalyoga.adminapp.network.ApiClient;
import com.universalyoga.adminapp.network.ApiService;
import java.util.List;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.universalyoga.adminapp.utils.NetworkUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;

public class UploadFragment extends Fragment {
    private Button btnSync, btnRetry, btnCancel;
    private ProgressBar progressBar;
    private TextView tvNetworkStatus, tvLastSync, tvDataSummary, tvUploadResult;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        
        courseDao = AppDatabase.getInstance(requireContext()).courseDao();
        instanceDao = AppDatabase.getInstance(requireContext()).instanceDao();
        apiService = ApiClient.get().create(ApiService.class);
        
        btnSync = view.findViewById(R.id.btnSync);
        btnRetry = view.findViewById(R.id.btnRetry);
        btnCancel = view.findViewById(R.id.btnCancel);
        progressBar = view.findViewById(R.id.progressBar);
        tvNetworkStatus = view.findViewById(R.id.tvNetworkStatus);
        tvLastSync = view.findViewById(R.id.tvLastSync);
        tvDataSummary = view.findViewById(R.id.tvDataSummary);
        tvUploadResult = view.findViewById(R.id.tvUploadResult);
        
        btnSync.setOnClickListener(v -> {
            if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                Toast.makeText(requireContext(), "No internet connection available.", Toast.LENGTH_SHORT).show();
                return;
            }
            performUpload();
        });
        // TODO: Add listeners for btnRetry and btnCancel if needed
        updateDataSummary();
        return view;
    }
    
    private void performUpload() {
        btnSync.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvUploadResult.setText("Preparing data for upload...");
        progressBar.setProgress(0);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<YogaCourse> coursesToUpload = courseDao.getAll();
            List<YogaInstance> instancesToUpload = instanceDao.getAll();

            Log.d("Upload", "Courses: " + coursesToUpload.size() + ", Instances: " + instancesToUpload.size());

            requireActivity().runOnUiThread(() -> {
                if (coursesToUpload.isEmpty() && instancesToUpload.isEmpty()) {
                    tvUploadResult.setText("No data to upload.");
                    resetUploadState();
                    return;
                }
                tvUploadResult.setText("Uploading to Firebase...");
            });

            uploadAllYogaClasses(requireContext(), coursesToUpload, instancesToUpload);
        });
    }

    private void uploadAllYogaClasses(android.content.Context context, List<YogaCourse> courses, List<YogaInstance> instances) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(context, "No internet connection available.", Toast.LENGTH_SHORT).show();
                resetUploadState();
            });
            return;
        }

        if (courses.isEmpty() && instances.isEmpty()) {
            requireActivity().runOnUiThread(() -> {
                tvUploadResult.setText("No data to upload.");
                resetUploadState();
            });
            return;
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("yoga_classes");

        // Group instances by courseId
        Map<Integer, Map<String, Object>> instancesByCourse = new HashMap<>();
        for (YogaInstance instance : instances) {
            int courseId = instance.getCourseId();
            if (!instancesByCourse.containsKey(courseId)) {
                instancesByCourse.put(courseId, new HashMap<>());
            }
            instancesByCourse.get(courseId).put(String.valueOf(instance.getId()), instance);
        }

        int total = courses.size();
        int[] uploaded = {0};
        for (YogaCourse course : courses) {
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("courseInfo", course);
            courseData.put("instances", instancesByCourse.getOrDefault(course.getId(), new HashMap<>()));
            dbRef.child(String.valueOf(course.getId())).setValue(courseData)
                .addOnSuccessListener(aVoid -> requireActivity().runOnUiThread(() -> {
                    uploaded[0]++;
                    int progress = (int)(((double)uploaded[0] / total) * 100);
                    progressBar.setProgress(progress);
                    tvUploadResult.setText("Uploaded " + uploaded[0] + "/" + total + " courses...");
                    if (uploaded[0] == total) {
                        tvUploadResult.setText("Upload completed successfully!");
                        progressBar.setProgress(100);
                        resetUploadState();
                        Toast.makeText(context, "Data uploaded to Firebase", Toast.LENGTH_LONG).show();
                        updateDataSummary();
                    }
                }))
                .addOnFailureListener(e -> requireActivity().runOnUiThread(() -> {
                    tvUploadResult.setText("Upload failed: " + e.getMessage());
                    resetUploadState();
                    Toast.makeText(context, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }));
        }
    }

    private void resetUploadState() {
        btnSync.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    private void updateDataSummary() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int courseCount = courseDao.getAll().size();
            int instanceCount = instanceDao.getAll().size();
            int unsyncedCount = 0; // Update this if you track unsynced items
            String summary = "Courses: " + courseCount + ", Instances: " + instanceCount + ", Unsynced: " + unsyncedCount;
            requireActivity().runOnUiThread(() -> tvDataSummary.setText(summary));
        });
    }
}