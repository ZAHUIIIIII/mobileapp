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

public class UploadFragment extends Fragment {
    private Button btnUpload;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        
        courseDao = AppDatabase.getInstance(requireContext()).courseDao();
        instanceDao = AppDatabase.getInstance(requireContext()).instanceDao();
        apiService = ApiClient.get().create(ApiService.class);
        
        btnUpload = view.findViewById(R.id.btnUpload);
        progressBar = view.findViewById(R.id.progressBar);
        tvStatus = view.findViewById(R.id.tvStatus);
        
        btnUpload.setOnClickListener(v -> {
            if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                Toast.makeText(requireContext(), "No internet connection available.", Toast.LENGTH_SHORT).show();
                return;
            }
            performUpload();
        });
        
        return view;
    }
    
    private void performUpload() {
        btnUpload.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Preparing data for upload...");
        progressBar.setProgress(0);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            List<YogaCourse> coursesToUpload = courseDao.getAll();
            List<YogaInstance> instancesToUpload = instanceDao.getAll();
            
            requireActivity().runOnUiThread(() -> tvStatus.setText("Uploading courses..."));
            apiService.syncCourses(coursesToUpload).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        requireActivity().runOnUiThread(() -> {
                            tvStatus.setText("Courses uploaded. Uploading instances...");
                            progressBar.setProgress(50);
                        });
                        uploadInstances(instancesToUpload);
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            tvStatus.setText("Course upload failed: " + response.message());
                            Toast.makeText(requireContext(), "Course upload failed", Toast.LENGTH_SHORT).show();
                            resetUploadState();
                        });
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    requireActivity().runOnUiThread(() -> {
                        tvStatus.setText("Course upload error: " + t.getMessage());
                        Toast.makeText(requireContext(), "Course upload error", Toast.LENGTH_SHORT).show();
                        resetUploadState();
                    });
                }
            });
        });
    }

    private void uploadInstances(List<YogaInstance> instancesToUpload) {
        apiService.syncInstances(instancesToUpload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        tvStatus.setText("Upload completed successfully!");
                        Toast.makeText(requireContext(), "Data uploaded to cloud service", Toast.LENGTH_LONG).show();
                        progressBar.setProgress(100);
                        resetUploadState();
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        tvStatus.setText("Instance upload failed: " + response.message());
                        Toast.makeText(requireContext(), "Instance upload failed", Toast.LENGTH_SHORT).show();
                        resetUploadState();
                    });
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("Instance upload error: " + t.getMessage());
                    Toast.makeText(requireContext(), "Instance upload error", Toast.LENGTH_SHORT).show();
                    resetUploadState();
                });
            }
        });
    }

    private void resetUploadState() {
        btnUpload.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }
}