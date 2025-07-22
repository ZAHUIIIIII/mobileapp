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

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import java.util.concurrent.Executors;
import com.universalyoga.adminapp.database.InstanceDao;
import com.google.android.material.snackbar.Snackbar;

public class CoursesFragment extends Fragment {
    private RecyclerView recyclerView;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    private CourseListAdapter adapter;
    private TextView tvTotalCoursesSummary, tvTotalCoursesSubtitle;
    private View cardEmptyCourses;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courses, container, false);
        
        courseDao = AppDatabase.getInstance(requireContext()).courseDao();
        instanceDao = AppDatabase.getInstance(requireContext()).instanceDao();
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
        cardEmptyCourses = view.findViewById(R.id.emptyStateLayout);
        observeCourses();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_course_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_wipe_all) {
            showWipeAllConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showWipeAllConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Reset Database")
            .setMessage("Are you sure you want to delete all courses and class instances? This action cannot be undone.")
            .setPositiveButton("Delete All", (dialog, which) -> {
                wipeAllData();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void wipeAllData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            courseDao.deleteAll();
            instanceDao.deleteAll();
            requireActivity().runOnUiThread(() -> {
                Snackbar.make(requireView(), "All data has been wiped.", Snackbar.LENGTH_SHORT).show();
            });
        });
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
                if (courses == null || courses.isEmpty()) {
                    cardEmptyCourses.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    cardEmptyCourses.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}