package com.universalyoga.adminapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.activities.AddCourseActivity;
import com.universalyoga.adminapp.activities.EditCourseActivity;
import com.universalyoga.adminapp.adapters.CourseListAdapter;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaCourse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import androidx.recyclerview.widget.ItemTouchHelper;
import java.util.Collections;
import java.util.Comparator;
import com.google.android.material.button.MaterialButton;
import android.widget.Toast;

public class CoursesFragment extends Fragment {
    private RecyclerView recyclerView;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    private CourseListAdapter adapter;
    private View cardEmptyCourses;
    private List<YogaCourse> fullCourseList = new ArrayList<>();

    private TextView tvStatsCourses, tvStatsCapacity, tvStatsRevenue;
    private TextInputEditText editTextSearch;
    private MaterialButton btnSortName, btnSortPrice;

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
        
        FloatingActionButton fabAddCourse = view.findViewById(R.id.fabAddCourse);
        fabAddCourse.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddCourseActivity.class)));
        
        cardEmptyCourses = view.findViewById(R.id.emptyStateLayout);
        tvStatsCourses = view.findViewById(R.id.tvStatsCourses);
        tvStatsCapacity = view.findViewById(R.id.tvStatsCapacity);
        tvStatsRevenue = view.findViewById(R.id.tvStatsRevenue);
        editTextSearch = view.findViewById(R.id.editTextSearch);

        btnSortName = view.findViewById(R.id.btnSortName);
        btnSortPrice = view.findViewById(R.id.btnSortPrice);

        btnSortName.setOnClickListener(v -> {
            Collections.sort(fullCourseList, Comparator.comparing(YogaCourse::getCourseName));
            adapter.updateCourses(fullCourseList);
        });
        btnSortPrice.setOnClickListener(v -> {
            Collections.sort(fullCourseList, Comparator.comparingDouble(YogaCourse::getPrice));
            adapter.updateCourses(fullCourseList);
        });

        // Add button to empty state
        Button btnAddCourse = view.findViewById(R.id.btnAddCourse);
        if (btnAddCourse != null) {
            btnAddCourse.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddCourseActivity.class)));
        }

        setupSearch();
        observeCourses();

        // Add swipe-to-delete with undo
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                YogaCourse deletedCourse = adapter.getCourseAt(position);
                Executors.newSingleThreadExecutor().execute(() -> {
                    courseDao.delete(deletedCourse);
                    requireActivity().runOnUiThread(() -> {
                        Snackbar.make(recyclerView, "Course deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> {
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    courseDao.insert(deletedCourse);
                                });
                            }).show();
                    });
                });
            }
        };
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);

        // Bulk delete action when in selection mode and button pressed again
        // This block is removed as per the edit hint.

        return view;
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourses(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCourses(String keyword) {
        List<YogaCourse> filteredList = new ArrayList<>();
        if (keyword.isEmpty()) {
            filteredList.addAll(fullCourseList);
        } else {
            String lowerCaseKeyword = keyword.toLowerCase();
            for (YogaCourse course : fullCourseList) {
                if (course.getCourseName().toLowerCase().contains(lowerCaseKeyword)) {
                    filteredList.add(course);
                }
            }
        }
        adapter.updateCourses(filteredList);
    }

    private void observeCourses() {
        adapter = new CourseListAdapter(new ArrayList<>(), course -> {
            Intent intent = new Intent(requireContext(), EditCourseActivity.class);
            intent.putExtra("id", course.getId());
            startActivity(intent);
        }, new CourseListAdapter.OnCourseActionListener() {
            @Override
            public void onEdit(YogaCourse course) {
                Intent intent = new Intent(requireContext(), EditCourseActivity.class);
                intent.putExtra("id", course.getId());
                startActivity(intent);
            }
            @Override
            public void onDelete(YogaCourse course) {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Course")
                    .setMessage("Are you sure you want to delete this course?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            int result = courseDao.delete(course);
                            requireActivity().runOnUiThread(() -> {
                                if (result > 0) {
                                    com.universalyoga.adminapp.utils.ToastHelper.pendingToastMessage = "Course deleted successfully!";
                                    // UI update will trigger onResume
                                } else {
                                    Snackbar.make(requireView(), "Delete failed!", Snackbar.LENGTH_LONG).show();
                                }
                            });
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        recyclerView.setAdapter(adapter);

        courseDao.getAllLive().observe(getViewLifecycleOwner(), courses -> {
            fullCourseList.clear();
            fullCourseList.addAll(courses);
            
            filterCourses(editTextSearch.getText().toString());
            updateStats(courses);

            if (courses == null || courses.isEmpty()) {
                cardEmptyCourses.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                cardEmptyCourses.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateStats(List<YogaCourse> courses) {
        int totalCourses = courses.size();
        int totalCapacity = 0;
        double totalRevenue = 0;

        for (YogaCourse course : courses) {
            totalCapacity += course.getCapacity();
            totalRevenue += course.getCapacity() * course.getPrice();
        }

        tvStatsCourses.setText(String.valueOf(totalCourses));
        tvStatsCapacity.setText(String.valueOf(totalCapacity));
        
        if (totalRevenue > 1000) {
            tvStatsRevenue.setText(String.format(Locale.UK, "£%.1fk", totalRevenue / 1000.0));
        } else {
            tvStatsRevenue.setText(String.format(Locale.UK, "£%.0f", totalRevenue));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_course_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        editTextSearch.setText(""); // Clear search to show all courses after edit
        // Ensure list refreshes after editing
        adapter.notifyDataSetChanged();
        if (com.universalyoga.adminapp.utils.ToastHelper.pendingToastMessage != null) {
            Toast.makeText(requireContext(), com.universalyoga.adminapp.utils.ToastHelper.pendingToastMessage, Toast.LENGTH_SHORT).show();
            com.universalyoga.adminapp.utils.ToastHelper.pendingToastMessage = null;
        }
    }

    private void showWipeAllConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Reset Database")
            .setMessage("Are you sure you want to delete all courses and class instances? This action cannot be undone.")
            .setPositiveButton("Delete All", (dialog, which) -> wipeAllData())
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
}
