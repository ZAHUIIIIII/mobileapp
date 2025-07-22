package com.universalyoga.adminapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.Observer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.activities.AddCourseActivity;
import com.universalyoga.adminapp.activities.EditCourseActivity;
import com.universalyoga.adminapp.adapters.CourseListAdapter;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.models.YogaCourse;
import java.util.List;
import android.widget.TextView;
import androidx.recyclerview.widget.DefaultItemAnimator;

public class CoursesFragment extends Fragment {
    private RecyclerView recyclerView;
    private CourseDao courseDao;
    private CourseListAdapter adapter;
    private TextView tvTotalCoursesSummary, tvTotalCoursesSubtitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courses, container, false);
        
        courseDao = AppDatabase.getInstance(requireContext()).courseDao();
        recyclerView = view.findViewById(R.id.recyclerCourses);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        
        // Floating Action Button for adding courses
        FloatingActionButton fabAddCourse = view.findViewById(R.id.fabAddCourse);
        fabAddCourse.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddCourseActivity.class));
        });
        
        tvTotalCoursesSummary = view.findViewById(R.id.tvTotalCoursesSummary);
        tvTotalCoursesSubtitle = view.findViewById(R.id.tvTotalCoursesSubtitle);
        observeCourses();
        return view;
    }
    
    private void observeCourses() {
        adapter = new CourseListAdapter(java.util.Collections.emptyList(), course -> {
            Intent intent = new Intent(requireContext(), EditCourseActivity.class);
            intent.putExtra("id", course.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        courseDao.getAllLive().observe(getViewLifecycleOwner(), new Observer<List<YogaCourse>>() {
            @Override
            public void onChanged(List<YogaCourse> courses) {
                adapter.updateCourses(courses);
                if (tvTotalCoursesSummary != null) {
                    tvTotalCoursesSummary.setText(String.valueOf(courses != null ? courses.size() : 0));
                }
            }
        });
    }
} 