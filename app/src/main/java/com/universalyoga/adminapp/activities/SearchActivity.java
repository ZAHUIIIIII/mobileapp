package com.universalyoga.adminapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.adapters.CourseListAdapter;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText etQuery;
    private Button btnSearch;
    private RecyclerView rvResults;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    
    @Override protected void onCreate(Bundle s){
        super.onCreate(s); 
        setContentView(R.layout.activity_search);
        
        courseDao = AppDatabase.getInstance(this).courseDao();
        instanceDao = AppDatabase.getInstance(this).instanceDao();
        
        etQuery = findViewById(R.id.editQuery);
        btnSearch = findViewById(R.id.btnSearch);
        rvResults = findViewById(R.id.searchRecycler);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        
        btnSearch.setOnClickListener(v -> performSearch());
    }
    
    private void performSearch() {
        String query = etQuery.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            List<YogaCourse> results = new ArrayList<>();
            List<YogaCourse> allCourses = courseDao.getAll();
            // Search by teacher name (in instances)
            for (YogaCourse course : allCourses) {
                List<YogaInstance> instances = instanceDao.getByCourse(course.getId());
                for (YogaInstance instance : instances) {
                    if (instance.getTeacher().toLowerCase().contains(query.toLowerCase())) {
                        if (!results.contains(course)) {
                            results.add(course);
                        }
                    }
                }
            }
            // Search by day of the week
            for (YogaCourse course : allCourses) {
                if (course.getDate().toLowerCase().contains(query.toLowerCase())) {
                    if (!results.contains(course)) {
                        results.add(course);
                    }
                }
            }
            // Search by date (in instances)
            for (YogaCourse course : allCourses) {
                List<YogaInstance> instances = instanceDao.getByCourse(course.getId());
                for (YogaInstance instance : instances) {
                    if (instance.getDate().contains(query)) {
                        if (!results.contains(course)) {
                            results.add(course);
                        }
                    }
                }
            }
            runOnUiThread(() -> {
                if (results.isEmpty()) {
                    Toast.makeText(this, "No courses found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, results.size() + " courses found", Toast.LENGTH_SHORT).show();
                }
                rvResults.setAdapter(new CourseListAdapter(results, course -> {
                    Intent intent = new Intent(this, EditCourseActivity.class);
                    intent.putExtra("id", course.getId());
                    startActivity(intent);
                }));
            });
        }).start();
    }
}
