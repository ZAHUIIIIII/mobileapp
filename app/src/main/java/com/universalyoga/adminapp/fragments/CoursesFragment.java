package com.universalyoga.adminapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.activities.AddCourseActivity;
import com.universalyoga.adminapp.activities.EditCourseActivity;
import com.universalyoga.adminapp.adapters.CourseListAdapter;
import com.universalyoga.adminapp.base.BaseFragment;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.viewmodels.CourseViewModel;
import com.universalyoga.adminapp.utils.ToastHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import com.universalyoga.adminapp.activities.ClassInstanceActivity;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.Map;
import java.util.HashMap;

/**
 * Fragment for displaying and managing yoga courses.
 * Updated to match modern UI/UX patterns with collapsible filters and improved search.
 */
public class CoursesFragment extends BaseFragment {
    
    private RecyclerView recyclerView;
    private CourseListAdapter adapter;
    private View cardEmptyCourses;
    private View layoutNoResults;
    private TextView tvCourseCount;
    private TextInputEditText editTextSearch;
    private CourseViewModel viewModel;
    private List<YogaCourse> courseList = new ArrayList<>();
    private List<YogaCourse> allCourses = new ArrayList<>();
    
    // Filter UI elements
    private MaterialButton cardAdvancedFilters;
    private View layoutFiltersContent;
    private Chip chipActiveFilters;
    private AutoCompleteTextView spinnerDayFilter;
    private AutoCompleteTextView spinnerTypeFilter;
    private AutoCompleteTextView spinnerDifficultyFilter;
    private Button btnClearFilters;
    private Button btnClearSearch;
    private Button btnClearAllFilters;
    private ImageView btnClearSearchInput;
    
    // Filter state
    private Set<String> activeFilters = new HashSet<>();
    private String currentDayFilter = "";
    private String currentTypeFilter = "";
    private String currentDifficultyFilter = "";
    private String currentSearchQuery = "";
    
    // Filter options
    private static final String[] DAYS_OF_WEEK = {"All days", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private static final String[] CLASS_TYPES = {"All types", "Flow Yoga", "Aerial Yoga", "Family Yoga", "Hot Yoga", "Restorative Yoga", "Vinyasa Yoga", "Hatha Yoga", "Yin Yoga"};
    private static final String[] DIFFICULTY_LEVELS = {"All levels", "Beginner", "Intermediate", "Advanced"};
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }
    
    @Override
    protected void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerCourses);
        cardEmptyCourses = view.findViewById(R.id.emptyStateLayout);
        layoutNoResults = view.findViewById(R.id.layout_no_results);
        tvCourseCount = view.findViewById(R.id.tv_course_count);
        editTextSearch = view.findViewById(R.id.et_search);
        contentView = view.findViewById(R.id.recyclerCourses);
        
        // Initialize filter UI elements
        cardAdvancedFilters = view.findViewById(R.id.btn_advanced_filters);
        layoutFiltersContent = view.findViewById(R.id.layout_advanced_filters_content);
        chipActiveFilters = view.findViewById(R.id.chip_active_filters);
        spinnerDayFilter = view.findViewById(R.id.spinner_day_filter);
        spinnerTypeFilter = view.findViewById(R.id.spinner_type_filter);
        spinnerDifficultyFilter = view.findViewById(R.id.spinner_difficulty_filter);
        btnClearFilters = view.findViewById(R.id.btn_clear_filters);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);
        btnClearAllFilters = view.findViewById(R.id.btn_clear_all_filters);
        btnClearSearchInput = view.findViewById(R.id.btn_clear_search_input);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        
        // Setup adapter
        adapter = new CourseListAdapter(courseList, null, new CourseListAdapter.OnCourseActionListener() {
            @Override
            public void onEdit(YogaCourse course) {
                Intent intent = new Intent(requireContext(), EditCourseActivity.class);
                intent.putExtra("id", course.getId());
                startActivity(intent);
            }
            
            @Override
            public void onDelete(YogaCourse course) {
                showDeleteConfirmationDialog(course);
            }
            
            @Override
            public void onManageInstances(YogaCourse course) {
                try {
                    // Validate course and course ID
                    if (course == null) {
                        android.util.Log.e("CoursesFragment", "Course is null");
                        return;
                    }
                    
                    Integer courseId = course.getId();
                    if (courseId == null || courseId <= 0) {
                        android.util.Log.e("CoursesFragment", "Invalid course ID: " + courseId);
                        return;
                    }
                    
                    // Check if activity is available
                    if (getActivity() == null) {
                        android.util.Log.e("CoursesFragment", "Activity is null");
                        return;
                    }
                    
                    // Navigate to instances management
                    Intent intent = new Intent(getActivity(), ClassInstanceActivity.class);
                    intent.putExtra("courseId", courseId);
                    android.util.Log.d("CoursesFragment", "Starting ClassInstanceActivity with courseId: " + courseId);
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("CoursesFragment", "Error starting ClassInstanceActivity: " + e.getMessage());
                }
            }
        });
        
        recyclerView.setAdapter(adapter);
        
        // Setup add course button
        MaterialButton btnAddCourse = view.findViewById(R.id.btn_add_course);
        btnAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddCourseActivity.class);
            startActivity(intent);
        });
        
        // Setup create first course button
        MaterialButton btnCreateFirstCourse = view.findViewById(R.id.btn_create_first_course);
        btnCreateFirstCourse.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddCourseActivity.class);
            startActivity(intent);
        });
        
        // Setup clear search button
        btnClearSearch.setOnClickListener(v -> {
            editTextSearch.setText("");
            currentSearchQuery = "";
            applyLocalFilters();
        });
        
        // Setup clear all filters button
        btnClearAllFilters.setOnClickListener(v -> {
            clearAllFilters();
            editTextSearch.setText("");
            currentSearchQuery = "";
            applyLocalFilters();
        });
        
        // Setup clear search input button
        btnClearSearchInput.setOnClickListener(v -> {
            editTextSearch.setText("");
            currentSearchQuery = "";
            applyLocalFilters();
        });
        
        // Setup filter functionality
        setupFilters();
    }
    
    private void setupFilters() {
        // Setup advanced filters toggle
        cardAdvancedFilters.setOnClickListener(v -> toggleAdvancedFilters());
        
        // Setup filter dropdowns
        setupFilterDropdowns();
        
        // Setup clear filters button
        btnClearFilters.setOnClickListener(v -> {
            clearAllFilters();
            applyLocalFilters();
        });
    }
    
    private void setupFilterDropdowns() {
        // Day filter
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, DAYS_OF_WEEK);
        spinnerDayFilter.setAdapter(dayAdapter);
        spinnerDayFilter.setOnItemClickListener((parent, view, position, id) -> {
            currentDayFilter = position == 0 ? "" : DAYS_OF_WEEK[position];
            updateActiveFilters();
            applyLocalFilters();
        });
        
        // Type filter
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, CLASS_TYPES);
        spinnerTypeFilter.setAdapter(typeAdapter);
        spinnerTypeFilter.setOnItemClickListener((parent, view, position, id) -> {
            currentTypeFilter = position == 0 ? "" : CLASS_TYPES[position];
            updateActiveFilters();
            applyLocalFilters();
        });
        
        // Difficulty filter
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, DIFFICULTY_LEVELS);
        spinnerDifficultyFilter.setAdapter(difficultyAdapter);
        spinnerDifficultyFilter.setOnItemClickListener((parent, view, position, id) -> {
            currentDifficultyFilter = position == 0 ? "" : DIFFICULTY_LEVELS[position];
            updateActiveFilters();
            applyLocalFilters();
        });
    }
    
    private void toggleAdvancedFilters() {
        boolean isExpanded = layoutFiltersContent.getVisibility() == View.VISIBLE;
        layoutFiltersContent.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        
        // Update button text and icon
        cardAdvancedFilters.setText(isExpanded ? "Advanced Filters" : "Hide Filters");
        
        // Show contextual help for first time
        if (!isExpanded) {
            ToastHelper.showFirstTimeToast(requireContext(), "advanced_filters", "Use filters to find specific classes by day, type, or difficulty level.");
        }
    }
    
    private void updateActiveFilters() {
        activeFilters.clear();
        if (!currentDayFilter.isEmpty()) activeFilters.add("Day: " + currentDayFilter);
        if (!currentTypeFilter.isEmpty()) activeFilters.add("Type: " + currentTypeFilter);
        if (!currentDifficultyFilter.isEmpty()) activeFilters.add("Level: " + currentDifficultyFilter);
        
        // Update chip visibility and text
        if (!activeFilters.isEmpty()) {
            chipActiveFilters.setVisibility(View.VISIBLE);
            btnClearFilters.setVisibility(View.VISIBLE);
            
            // Show active filters in chip
            StringBuilder chipText = new StringBuilder();
            for (String filter : activeFilters) {
                if (chipText.length() > 0) chipText.append(", ");
                chipText.append(filter);
            }
            chipActiveFilters.setText(chipText.toString());
            
            // Setup chip close listener
            chipActiveFilters.setOnCloseIconClickListener(v -> {
                clearAllFilters();
                applyLocalFilters();
            });
        } else {
            chipActiveFilters.setVisibility(View.GONE);
            btnClearFilters.setVisibility(View.GONE);
        }
    }
    
    private void clearAllFilters() {
        currentDayFilter = "";
        currentTypeFilter = "";
        currentDifficultyFilter = "";
        activeFilters.clear();
        
        // Reset dropdown selections
        spinnerDayFilter.setText("", false);
        spinnerTypeFilter.setText("", false);
        spinnerDifficultyFilter.setText("", false);
        
        // Update UI
        chipActiveFilters.setVisibility(View.GONE);
        btnClearFilters.setVisibility(View.GONE);
        
        applyLocalFilters();
    }
    
    private void applyFilters() {
        if (viewModel != null) {
            viewModel.setFilters(currentDayFilter, currentTypeFilter, currentDifficultyFilter);
        }
    }
    
    @Override
    protected void setupObservers() {
        viewModel = getViewModel(CourseViewModel.class);
        
        // Observe all courses
        viewModel.getAllCourses().observe(getViewLifecycleOwner(), courses -> {
            allCourses.clear();
            if (courses != null) {
                allCourses.addAll(courses);
            }
            applyLocalFilters();
            loadInstanceCounts();
        });
        
        // Observe filtered courses
        viewModel.getFilteredCourses().observe(getViewLifecycleOwner(), courses -> {
            courseList.clear();
            if (courses != null) {
                courseList.addAll(courses);
            }
            adapter.notifyDataSetChanged();
            updateEmptyState();
            updateCourseCount();
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });
        
        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                ToastHelper.showErrorToast(requireContext(), errorMessage);
                viewModel.clearError();
            }
        });
        
        // Observe success messages
        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                ToastHelper.showSuccessToast(requireContext(), successMessage);
                viewModel.clearSuccess();
            }
        });
    }
    
    private void loadInstanceCounts() {
        try {
            AppDatabase database = AppDatabase.getInstance(requireContext());
            InstanceDao instanceDao = database.instanceDao();
            
            // Load instance counts for all courses
            Map<Integer, Integer> instanceCounts = new HashMap<>();
            for (YogaCourse course : allCourses) {
                List<YogaInstance> instances = instanceDao.getInstancesByCourseSync(course.getId());
                instanceCounts.put(course.getId(), instances != null ? instances.size() : 0);
            }
            
            // Update adapter with instance counts
            adapter.updateInstanceCounts(instanceCounts);
        } catch (Exception e) {
            // Error loading instance counts
        }
    }
    
    private void applyLocalFilters() {
        List<YogaCourse> filtered = new ArrayList<>(allCourses);
        
        // Apply search filter
        if (!currentSearchQuery.trim().isEmpty()) {
            String query = currentSearchQuery.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(course -> 
                        (course.getDaysOfWeek() != null && course.getDaysOfWeek().toLowerCase().contains(query)) ||
                        (course.getType() != null && course.getType().toLowerCase().contains(query)) ||
                        (course.getDescription() != null && course.getDescription().toLowerCase().contains(query)) ||
                        (course.getRoomLocation() != null && course.getRoomLocation().toLowerCase().contains(query)) ||
                        (course.getInstructor() != null && course.getInstructor().toLowerCase().contains(query)) ||
                        (course.getDifficulty() != null && course.getDifficulty().toLowerCase().contains(query)))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Apply day filter
        if (!currentDayFilter.isEmpty()) {
            filtered = filtered.stream()
                    .filter(course -> course.getDaysOfWeek() != null && 
                                    course.getDaysOfWeek().equalsIgnoreCase(currentDayFilter))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Apply type filter
        if (!currentTypeFilter.isEmpty()) {
            filtered = filtered.stream()
                    .filter(course -> course.getType() != null && 
                                    course.getType().equalsIgnoreCase(currentTypeFilter))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Apply difficulty filter
        if (!currentDifficultyFilter.isEmpty()) {
            filtered = filtered.stream()
                    .filter(course -> course.getDifficulty() != null && 
                                    course.getDifficulty().equalsIgnoreCase(currentDifficultyFilter))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        courseList.clear();
        courseList.addAll(filtered);
        adapter.notifyDataSetChanged();
        updateEmptyState();
        updateCourseCount();
    }
    
    @Override
    protected void setupListeners() {
        // Setup search functionality with debouncing
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                
                // Show/hide clear search button
                if (s.length() > 0) {
                    btnClearSearchInput.setVisibility(View.VISIBLE);
                } else {
                    btnClearSearchInput.setVisibility(View.GONE);
                }
                
                applyLocalFilters();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    /**
     * Update empty state visibility
     */
    private void updateEmptyState() {
        if (courseList.isEmpty()) {
            if (hasActiveFilters()) {
                // Show no results state
                layoutNoResults.setVisibility(View.VISIBLE);
                cardEmptyCourses.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            } else {
                // Show empty state
                cardEmptyCourses.setVisibility(View.VISIBLE);
                layoutNoResults.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            }
        } else {
            cardEmptyCourses.setVisibility(View.GONE);
            layoutNoResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private boolean hasActiveFilters() {
        return !currentSearchQuery.trim().isEmpty() || !activeFilters.isEmpty();
    }
    
    /**
     * Update course count display
     */
    private void updateCourseCount() {
        if (tvCourseCount != null) {
            int totalCount = allCourses.size();
            int filteredCount = courseList.size();
            String countText = String.format("%d of %d classes", filteredCount, totalCount);
            if (hasActiveFilters()) {
                countText += " (filtered)";
            }
            tvCourseCount.setText(countText);
        }
    }
    
    /**
     * Show delete confirmation dialog
     */
    private void showDeleteConfirmationDialog(YogaCourse course) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete '" + course.getType() + " on " + course.getDaysOfWeek() + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteCourse(course);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Show wipe all confirmation dialog
     */
    private void showWipeAllConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete All Classes")
                .setMessage("Are you sure you want to delete all classes? This action cannot be undone.")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    viewModel.deleteAllCourses();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to the fragment
        if (viewModel != null) {
            viewModel.getAllCourses();
        }
        // Refresh instance counts
        loadInstanceCounts();
    }
}
