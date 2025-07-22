package com.universalyoga.adminapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.activities.AddCourseActivity;
import com.universalyoga.adminapp.activities.AddInstanceActivity;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.List;

public class DashboardFragment extends Fragment {
    private TextView tvTotalCourses, tvTotalInstances;
    private CourseDao courseDao;
    private InstanceDao instanceDao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        courseDao = AppDatabase.getInstance(requireContext()).courseDao();
        instanceDao = AppDatabase.getInstance(requireContext()).instanceDao();
        
        tvTotalCourses = view.findViewById(R.id.tvTotalCourses);
        tvTotalInstances = view.findViewById(R.id.tvTotalInstances);
        
        // Quick action cards
        CardView cardAddCourse = view.findViewById(R.id.cardAddCourse);
        CardView cardAddInstance = view.findViewById(R.id.cardAddInstance);
        CardView cardViewCourses = view.findViewById(R.id.cardViewCourses);
        CardView cardViewInstances = view.findViewById(R.id.cardViewInstances);
        
        cardAddCourse.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddCourseActivity.class));
        });
        
        cardAddInstance.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddInstanceActivity.class));
        });
        
        cardViewCourses.setOnClickListener(v -> {
            // Navigate to courses fragment
            requireActivity().findViewById(R.id.nav_courses).performClick();
        });
        
        cardViewInstances.setOnClickListener(v -> {
            // Navigate to instances fragment
            requireActivity().findViewById(R.id.nav_instances).performClick();
        });
        
        observeStats();
        return view;
    }
    
    private void observeStats() {
        courseDao.getAllLive().observe(getViewLifecycleOwner(), new Observer<List<YogaCourse>>() {
            @Override
            public void onChanged(List<YogaCourse> courses) {
                tvTotalCourses.setText(String.valueOf(courses != null ? courses.size() : 0));
            }
        });
        instanceDao.getAllLive().observe(getViewLifecycleOwner(), new Observer<List<YogaInstance>>() {
            @Override
            public void onChanged(List<YogaInstance> instances) {
                tvTotalInstances.setText(String.valueOf(instances != null ? instances.size() : 0));
            }
        });
    }
} 