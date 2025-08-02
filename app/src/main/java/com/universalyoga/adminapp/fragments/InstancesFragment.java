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
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.universalyoga.adminapp.models.YogaCourse;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.Observer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.activities.AddInstanceActivity;
import com.universalyoga.adminapp.activities.EditInstanceActivity;
import com.universalyoga.adminapp.adapters.InstanceAdapter;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.List;
import java.util.concurrent.Executors;
import android.app.AlertDialog;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class InstancesFragment extends Fragment implements InstanceAdapter.OnInstanceActionListener {
    private RecyclerView recyclerView;
    private InstanceDao instanceDao;
    private InstanceAdapter adapter;
    private View emptyStateLayout;
    private View layoutNoResults;
    private TextView tvStatsInstances, tvStatsMembersRegistered, tvInstanceCount;
    private TextInputEditText editTextSearch;
    private List<YogaInstance> allInstances = new ArrayList<>();
    private List<YogaInstance> filteredInstances = new ArrayList<>();
    private String currentSearchQuery = "";
    
    // Filter UI elements
    private MaterialButton btnAdvancedFilters;
    private View layoutAdvancedFiltersContent;
    private Chip chipActiveFilters;
    private AutoCompleteTextView spinnerDayFilter;
    private AutoCompleteTextView spinnerTypeFilter;
    private AutoCompleteTextView spinnerTeacherFilter;
    private Button btnClearFilters;
    private Button btnClearSearch;
    private Button btnClearAllFilters;
    private ImageView btnClearSearchInput;
    
    // Filter state
    private Set<String> activeFilters = new HashSet<>();
    private String currentDayFilter = "";
    private String currentTypeFilter = "";
    private String currentTeacherFilter = "";
    
    // Filter options
    private static final String[] DAYS_OF_WEEK = {"All days", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private static final String[] CLASS_TYPES = {"All types", "Flow Yoga", "Aerial Yoga", "Family Yoga", "Hot Yoga", "Restorative Yoga", "Vinyasa Yoga", "Hatha Yoga", "Yin Yoga"};
    private static final String[] TEACHER_OPTIONS = {"All teachers"}; // Will be populated dynamically

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instances, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupFilters();
        setupListeners(view);
        observeInstances();
        
        return view;
    }
    
    private void initializeViews(View view) {
        instanceDao = AppDatabase.getInstance(requireContext()).instanceDao();
        recyclerView = view.findViewById(R.id.recyclerInstances);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        layoutNoResults = view.findViewById(R.id.layout_no_results);
        tvStatsInstances = view.findViewById(R.id.tvStatsInstances);
        tvStatsMembersRegistered = view.findViewById(R.id.tvStatsMembersRegistered);
        tvInstanceCount = view.findViewById(R.id.tv_instance_count);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        
        // Filter UI elements
        btnAdvancedFilters = view.findViewById(R.id.btn_advanced_filters);
        layoutAdvancedFiltersContent = view.findViewById(R.id.layout_advanced_filters_content);
        chipActiveFilters = view.findViewById(R.id.chip_active_filters);
        spinnerDayFilter = view.findViewById(R.id.spinner_day_filter);
        spinnerTypeFilter = view.findViewById(R.id.spinner_type_filter);
        spinnerTeacherFilter = view.findViewById(R.id.spinner_teacher_filter);
        btnClearFilters = view.findViewById(R.id.btn_clear_filters);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);
        btnClearAllFilters = view.findViewById(R.id.btn_clear_all_filters);
        btnClearSearchInput = view.findViewById(R.id.btn_clear_search_input);
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InstanceAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }
    
    private void setupFilters() {
        // Setup advanced filters toggle
        btnAdvancedFilters.setOnClickListener(v -> toggleAdvancedFilters());
        
        // Setup filter dropdowns
        setupFilterDropdowns();
        
        // Setup clear filters button
        btnClearFilters.setOnClickListener(v -> {
            clearAllFilters();
            applyLocalFilters();
        });
        
        // Setup clear search button
        btnClearSearch.setOnClickListener(v -> {
            currentSearchQuery = "";
            editTextSearch.setText("");
            applyLocalFilters();
        });
        
        // Setup clear all filters button
        btnClearAllFilters.setOnClickListener(v -> {
            currentSearchQuery = "";
            editTextSearch.setText("");
            clearAllFilters();
            applyLocalFilters();
        });
        
        // Setup clear search input button
        btnClearSearchInput.setOnClickListener(v -> {
            currentSearchQuery = "";
            editTextSearch.setText("");
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
        
        // Teacher filter - will be populated dynamically
        updateTeacherFilterOptions();
    }
    
    private void updateTeacherFilterOptions() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> teachers = instanceDao.getAllTeachers();
            List<String> teacherOptions = new ArrayList<>();
            teacherOptions.add("All teachers");
            teacherOptions.addAll(teachers);
            
            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, teacherOptions);
                spinnerTeacherFilter.setAdapter(teacherAdapter);
                spinnerTeacherFilter.setOnItemClickListener((parent, view, position, id) -> {
                    currentTeacherFilter = position == 0 ? "" : teacherOptions.get(position);
                    updateActiveFilters();
                    applyLocalFilters();
                });
            });
        });
    }
    
    private void toggleAdvancedFilters() {
        boolean isExpanded = layoutAdvancedFiltersContent.getVisibility() == View.VISIBLE;
        layoutAdvancedFiltersContent.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        btnAdvancedFilters.setText(isExpanded ? "Advanced Filters" : "Hide Filters");
    }
    
    private void updateActiveFilters() {
        activeFilters.clear();
        if (!currentDayFilter.isEmpty()) activeFilters.add("Day: " + currentDayFilter);
        if (!currentTypeFilter.isEmpty()) activeFilters.add("Type: " + currentTypeFilter);
        if (!currentTeacherFilter.isEmpty()) activeFilters.add("Teacher: " + currentTeacherFilter);
        
        // Update chip visibility and text
        if (!activeFilters.isEmpty()) {
            chipActiveFilters.setVisibility(View.VISIBLE);
            
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
        }
    }
    
    private void clearAllFilters() {
        currentDayFilter = "";
        currentTypeFilter = "";
        currentTeacherFilter = "";
        activeFilters.clear();
        
        // Reset dropdown selections
        spinnerDayFilter.setText("", false);
        spinnerTypeFilter.setText("", false);
        spinnerTeacherFilter.setText("", false);
        
        // Update UI
        chipActiveFilters.setVisibility(View.GONE);
    }
    
    private void setupListeners(View view) {
        // Setup search functionality
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
        

        
        // Add Instance button in header
        Button btnAddInstance = view.findViewById(R.id.btn_add_instance);
        if (btnAddInstance != null) {
            btnAddInstance.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), AddInstanceActivity.class));
            });
        }

        // Add New Instance button in empty state
        Button btnAddInstanceEmpty = view.findViewById(R.id.btnAddInstance);
        if (btnAddInstanceEmpty != null) {
            btnAddInstanceEmpty.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), AddInstanceActivity.class));
            });
        }
    }
    
    private void observeInstances() {
        instanceDao.getAllLive().observe(getViewLifecycleOwner(), new Observer<List<YogaInstance>>() {
            @Override
            public void onChanged(List<YogaInstance> instances) {
                allInstances.clear();
                if (instances != null) {
                    allInstances.addAll(instances);
                }
                updateTeacherFilterOptions();
                applyLocalFilters();
            }
        });
    }
    
    private void applyLocalFilters() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<YogaInstance> filtered = new ArrayList<>(allInstances);
            
            // Cache course data to avoid multiple database calls
            Map<Integer, YogaCourse> courseCache = new HashMap<>();
            AppDatabase db = AppDatabase.getInstance(requireContext());
            
            // Pre-load all course data
            for (YogaInstance instance : allInstances) {
                if (!courseCache.containsKey(instance.getCourseId())) {
                    YogaCourse course = db.courseDao().getById(instance.getCourseId());
                    courseCache.put(instance.getCourseId(), course);
                }
            }
            
            // Apply search filter
            if (!currentSearchQuery.trim().isEmpty()) {
                String query = currentSearchQuery.toLowerCase().trim();
                filtered = filtered.stream()
                        .filter(instance -> {
                            boolean matches = false;
                            
                            // Search in teacher name
                            if (instance.getTeacher() != null && 
                                instance.getTeacher().toLowerCase().contains(query)) {
                                matches = true;
                            }
                            
                            // Search in course name
                            if (!matches) {
                                YogaCourse course = courseCache.get(instance.getCourseId());
                                if (course != null && course.getCourseName() != null && 
                                    course.getCourseName().toLowerCase().contains(query)) {
                                    matches = true;
                                }
                            }
                            
                            // Search in course type
                            if (!matches) {
                                YogaCourse course = courseCache.get(instance.getCourseId());
                                if (course != null && course.getType() != null && 
                                    course.getType().toLowerCase().contains(query)) {
                                    matches = true;
                                }
                            }
                            
                            // Search in date
                            if (!matches && instance.getDate() != null && 
                                instance.getDate().toLowerCase().contains(query)) {
                                matches = true;
                            }
                            
                            // Search in comments
                            if (!matches && instance.getComments() != null && 
                                instance.getComments().toLowerCase().contains(query)) {
                                matches = true;
                            }
                            
                            return matches;
                        })
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Apply day filter
            if (!currentDayFilter.isEmpty()) {
                filtered = filtered.stream()
                        .filter(instance -> {
                            String instanceDay = getDayName(instance.getDate());
                            return instanceDay.equalsIgnoreCase(currentDayFilter);
                        })
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Apply type filter
            if (!currentTypeFilter.isEmpty()) {
                filtered = filtered.stream()
                        .filter(instance -> {
                            YogaCourse course = courseCache.get(instance.getCourseId());
                            return course != null && 
                                   course.getType() != null && 
                                   course.getType().equalsIgnoreCase(currentTypeFilter);
                        })
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Apply teacher filter
            if (!currentTeacherFilter.isEmpty()) {
                filtered = filtered.stream()
                        .filter(instance -> instance.getTeacher() != null && 
                                          instance.getTeacher().equalsIgnoreCase(currentTeacherFilter))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Create final copy for lambda
            final List<YogaInstance> finalFiltered = new ArrayList<>(filtered);
            
            // Update UI on main thread
            requireActivity().runOnUiThread(() -> {
                filteredInstances.clear();
                filteredInstances.addAll(finalFiltered);
                adapter.updateInstances(filteredInstances);
                updateEmptyState();
                updateStats();
            });
        });
    }
    

    
    private void updateEmptyState() {
        if (filteredInstances.isEmpty()) {
            if (hasActiveFilters()) {
                // Show no results state
                layoutNoResults.setVisibility(View.VISIBLE);
                emptyStateLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            } else {
                // Show empty state
                emptyStateLayout.setVisibility(View.VISIBLE);
                layoutNoResults.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            }
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            layoutNoResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private boolean hasActiveFilters() {
        return !currentSearchQuery.trim().isEmpty() || !activeFilters.isEmpty();
    }
    
    private void updateStats() {
        tvStatsInstances.setText(String.valueOf(filteredInstances.size()));
        int totalEnrolled = 0;
        for (YogaInstance instance : filteredInstances) {
            totalEnrolled += instance.getEnrolled();
        }
        tvStatsMembersRegistered.setText(String.valueOf(totalEnrolled));
        tvInstanceCount.setText(filteredInstances.size() + " of " + allInstances.size() + " instances");
    }

    public void onInstanceClick(YogaInstance instance) {
        // Not implemented for now, as per requirement
    }

    public void onEditClick(YogaInstance instance) {
        Intent intent = new Intent(requireContext(), EditInstanceActivity.class);
        intent.putExtra("instanceId", instance.getId());
        startActivity(intent);
    }

    public void onDeleteClick(YogaInstance instance) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Instance")
            .setMessage("Are you sure you want to delete this instance?")
            .setPositiveButton("Delete", (dialog, which) -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    int result = instanceDao.delete(instance);
                    requireActivity().runOnUiThread(() -> {
                        if (result > 0) {
                            Snackbar.make(requireView(), "Instance deleted!", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(requireView(), "Delete failed!", Snackbar.LENGTH_LONG).show();
                        }
                    });
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private String getDayName(String dateString) {
        if (dateString == null) return "";
        dateString = dateString.trim();
        String[] possibleFormats = {
            "EEEE, dd/MM/yyyy",
            "dd/MM/yyyy",
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "yyyy/MM/dd",
            "dd-MM-yyyy",
            "yyyyMMdd",
            "yyyy-MM-dd HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss"
        };
        for (String format : possibleFormats) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
                dateFormat.setLenient(true);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateFormat.parse(dateString));
                // Return full day name to match filter options
                return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendar.getTime()).trim();
            } catch (Exception ignored) {
                // Try next format
            }
        }
        return ""; // Return empty string if no format matches
    }
}