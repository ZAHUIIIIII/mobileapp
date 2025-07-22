package com.universalyoga.adminapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.activities.EditCourseActivity;
import com.universalyoga.adminapp.adapters.CourseListAdapter;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private EditText etQuery;
    private Button btnSearch;
    private RecyclerView recyclerView;
    private CourseDao courseDao;
    private InstanceDao instanceDao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        
        courseDao = AppDatabase.getInstance(requireContext()).courseDao();
        instanceDao = AppDatabase.getInstance(requireContext()).instanceDao();
        
        etQuery = view.findViewById(R.id.etSearchQuery);
        btnSearch = view.findViewById(R.id.btnSearch);
        recyclerView = view.findViewById(R.id.recyclerSearchResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        btnSearch.setOnClickListener(v -> performSearch());
        
        return view;
    }
    
    private void performSearch() {
        String query = etQuery.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT).show();
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
            requireActivity().runOnUiThread(() -> {
                if (results.isEmpty()) {
                    Toast.makeText(requireContext(), "No courses found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), results.size() + " courses found", Toast.LENGTH_SHORT).show();
                }
                CourseListAdapter adapter = new CourseListAdapter(results, course -> {
                    Intent intent = new Intent(requireContext(), EditCourseActivity.class);
                    intent.putExtra("id", course.getId());
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
} 