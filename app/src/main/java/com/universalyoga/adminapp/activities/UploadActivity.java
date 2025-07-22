package com.universalyoga.adminapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.List;

public class UploadActivity extends AppCompatActivity {
    private Button btnUpload;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        
        courseDao = AppDatabase.getInstance(this).courseDao();
        instanceDao = AppDatabase.getInstance(this).instanceDao();
        
        btnUpload = findViewById(R.id.btnUpload);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        
        btnUpload.setOnClickListener(v -> performUpload());
    }
    
    private void performUpload() {
        btnUpload.setEnabled(false);
        progressBar.setVisibility(android.view.View.VISIBLE);
        tvStatus.setText("Preparing data for upload...");
        
        // Simulate upload process
        new Thread(() -> {
            try {
                // Get all courses and instances
                List<YogaCourse> courses = courseDao.getAll();
                // List<YogaInstance> instances = instanceDao.getAllInstances(); // TODO: Implement
                
                // Simulate upload progress
                for (int i = 0; i <= 100; i += 10) {
                    final int progress = i;
                    runOnUiThread(() -> {
                        progressBar.setProgress(progress);
                        tvStatus.setText("Uploading... " + progress + "%");
                    });
                    Thread.sleep(200);
                }
                
                // Simulate successful upload
                runOnUiThread(() -> {
                    tvStatus.setText("Upload completed successfully!");
                    Toast.makeText(this, "Data uploaded to cloud service", Toast.LENGTH_LONG).show();
                    btnUpload.setEnabled(true);
                    progressBar.setVisibility(android.view.View.GONE);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvStatus.setText("Upload failed: " + e.getMessage());
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                    btnUpload.setEnabled(true);
                    progressBar.setVisibility(android.view.View.GONE);
                });
            }
        }).start();
    }
}
