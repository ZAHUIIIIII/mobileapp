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
import java.util.List;

public class UploadFragment extends Fragment {
    private Button btnUpload;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private CourseDao courseDao;
    private InstanceDao instanceDao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        
        courseDao = AppDatabase.getInstance(requireContext()).courseDao();
        instanceDao = AppDatabase.getInstance(requireContext()).instanceDao();
        
        btnUpload = view.findViewById(R.id.btnUpload);
        progressBar = view.findViewById(R.id.progressBar);
        tvStatus = view.findViewById(R.id.tvStatus);
        
        btnUpload.setOnClickListener(v -> performUpload());
        
        return view;
    }
    
    private void performUpload() {
        btnUpload.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Preparing data for upload...");
        
        // Simulate upload process
        new Thread(() -> {
            try {
                // Get all courses and instances
                List<YogaCourse> courses = courseDao.getAll();
                List<YogaInstance> instances = instanceDao.getAll();
                
                // Simulate upload progress
                for (int i = 0; i <= 100; i += 10) {
                    final int progress = i;
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setProgress(progress);
                        tvStatus.setText("Uploading... " + progress + "%");
                    });
                    Thread.sleep(200);
                }
                
                // Simulate successful upload
                requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("Upload completed successfully!");
                    Toast.makeText(requireContext(), "Data uploaded to cloud service", Toast.LENGTH_LONG).show();
                    btnUpload.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
                
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("Upload failed: " + e.getMessage());
                    Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                    btnUpload.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start();
    }
} 