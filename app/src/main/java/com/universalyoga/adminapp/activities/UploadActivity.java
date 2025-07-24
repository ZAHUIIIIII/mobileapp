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
import com.universalyoga.adminapp.network.ApiClient;
import com.universalyoga.adminapp.network.ApiService;
import java.util.List;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.universalyoga.adminapp.utils.NetworkUtils;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {
    private Button btnUpload;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        courseDao = AppDatabase.getInstance(this).courseDao();
        instanceDao = AppDatabase.getInstance(this).instanceDao();
        apiService = ApiClient.get().create(ApiService.class);

        btnUpload = findViewById(R.id.btnUpload);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        btnUpload.setOnClickListener(v -> performUpload());
    }

    private void performUpload() {
        btnUpload.setEnabled(false);
        progressBar.setVisibility(android.view.View.VISIBLE);
        tvStatus.setText("Preparing data for upload...");
        progressBar.setProgress(0);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<YogaCourse> coursesToUpload = courseDao.getAll();
            List<YogaInstance> instancesToUpload = instanceDao.getAll();

            runOnUiThread(() -> tvStatus.setText("Uploading to Firebase..."));
            uploadAllYogaClasses(this, coursesToUpload, instancesToUpload);
        });
    }

    private void uploadAllYogaClasses(android.content.Context context, List<YogaCourse> courses, List<YogaInstance> instances) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
                        runOnUiThread(() -> {
                Toast.makeText(context, "No internet connection available.", Toast.LENGTH_SHORT).show();
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

        for (YogaCourse course : courses) {
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("courseInfo", course);
            courseData.put("instances", instancesByCourse.getOrDefault(course.getId(), new HashMap<>()));
            dbRef.child(String.valueOf(course.getId())).setValue(courseData)
                .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                    tvStatus.setText("Upload completed successfully!");
                    progressBar.setProgress(100);
                    resetUploadState();
                    Toast.makeText(context, "Data uploaded to Firebase", Toast.LENGTH_LONG).show();
                }))
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    tvStatus.setText("Upload failed: " + e.getMessage());
                        resetUploadState();
                    Toast.makeText(context, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }));
                }
    }

    private void uploadInstances(List<YogaInstance> instancesToUpload) {
        apiService.syncInstances(instancesToUpload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        tvStatus.setText("Upload completed successfully!");
                        Toast.makeText(UploadActivity.this, "Data uploaded to cloud service", Toast.LENGTH_LONG).show();
                        progressBar.setProgress(100);
                        resetUploadState();
                    });
                } else {
                    runOnUiThread(() -> {
                        tvStatus.setText("Instance upload failed: " + response.message());
                        Toast.makeText(UploadActivity.this, "Instance upload failed", Toast.LENGTH_SHORT).show();
                        resetUploadState();
                    });
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                runOnUiThread(() -> {
                    tvStatus.setText("Instance upload error: " + t.getMessage());
                    Toast.makeText(UploadActivity.this, "Instance upload error", Toast.LENGTH_SHORT).show();
                    resetUploadState();
                });
            }
        });
    }

    private void resetUploadState() {
        btnUpload.setEnabled(true);
        progressBar.setVisibility(android.view.View.GONE);
    }
}