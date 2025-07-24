package com.universalyoga.adminapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
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

public class InstancesFragment extends Fragment implements InstanceAdapter.OnInstanceActionListener {
    private RecyclerView recyclerView;
    private InstanceDao instanceDao;
    private InstanceAdapter adapter;
    private View emptyStateLayout;
    private TextView tvStatsInstances, tvStatsMembersRegistered;
    private com.google.android.material.textfield.TextInputEditText editTextSearch;
    private Spinner spinnerSearchBy;
    private ChipGroup chipGroupDaysFilter;
    private Chip chipMonday, chipTuesday, chipWednesday, chipThursday, chipFriday, chipSaturday, chipSunday;
    private List<YogaInstance> allInstances;
    private String currentSearchBy = "Search by Teacher"; // Default search criterion
    private java.util.Set<String> selectedDays = new java.util.HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instances, container, false);
        
        instanceDao = AppDatabase.getInstance(requireContext()).instanceDao();
        recyclerView = view.findViewById(R.id.recyclerInstances);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        tvStatsInstances = view.findViewById(R.id.tvStatsInstances);
        tvStatsMembersRegistered = view.findViewById(R.id.tvStatsMembersRegistered);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        spinnerSearchBy = view.findViewById(R.id.spinnerSearchBy);
        chipGroupDaysFilter = view.findViewById(R.id.chipGroupDaysFilter);
        chipMonday = view.findViewById(R.id.chipMonday);
        chipTuesday = view.findViewById(R.id.chipTuesday);
        chipWednesday = view.findViewById(R.id.chipWednesday);
        chipThursday = view.findViewById(R.id.chipThursday);
        chipFriday = view.findViewById(R.id.chipFriday);
        chipSaturday = view.findViewById(R.id.chipSaturday);
        chipSunday = view.findViewById(R.id.chipSunday);

        // Remove chipGroupDaysFilter.setOnCheckedChangeListener
        // Add individual listeners for each chip
        View.OnClickListener chipListener = v -> {
            selectedDays.clear();
            for (int i = 0; i < chipGroupDaysFilter.getChildCount(); i++) {
                View child = chipGroupDaysFilter.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                if (chip.isChecked()) {
                        selectedDays.add(chip.getText().toString().trim());
                    }
                }
            }
            filterInstances(editTextSearch.getText().toString());
        };
        chipMonday.setOnClickListener(chipListener);
        chipTuesday.setOnClickListener(chipListener);
        chipWednesday.setOnClickListener(chipListener);
        chipThursday.setOnClickListener(chipListener);
        chipFriday.setOnClickListener(chipListener);
        chipSaturday.setOnClickListener(chipListener);
        chipSunday.setOnClickListener(chipListener);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.search_by_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSearchBy.setAdapter(adapter);

        spinnerSearchBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSearchBy = parent.getItemAtPosition(position).toString();
                filterInstances(editTextSearch.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterInstances(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Floating Action Button for adding instances
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddInstanceActivity.class));
        });

        // Add New Instance button in empty state
        Button btnAddInstance = view.findViewById(R.id.btnAddInstance);
        if (btnAddInstance != null) {
            btnAddInstance.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), AddInstanceActivity.class));
            });
        }
        
        observeInstances();
        return view;
    }
    
    private void observeInstances() {
        adapter = new InstanceAdapter(java.util.Collections.emptyList(), this);
        recyclerView.setAdapter(adapter);
        instanceDao.getAllLive().observe(getViewLifecycleOwner(), new Observer<List<YogaInstance>>() {
            @Override
            public void onChanged(List<YogaInstance> instances) {
                allInstances = instances; // Store the full list
                filterInstances(editTextSearch.getText().toString()); // Apply filter
            }
        });
    }

    private void filterInstances(String query) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<YogaInstance> filteredList = new java.util.ArrayList<>();
                if (allInstances != null) {
                // Cache course names for performance
                java.util.Map<Integer, String> courseNameCache = new java.util.HashMap<>();
                AppDatabase db = AppDatabase.getInstance(requireContext());
                    for (YogaInstance instance : allInstances) {
                    if (!courseNameCache.containsKey(instance.getCourseId())) {
                        YogaCourse course = db.courseDao().getById(instance.getCourseId());
                        courseNameCache.put(instance.getCourseId(), course != null ? course.getCourseName() : "");
                        }
                    }

                for (YogaInstance instance : allInstances) {
                    String instanceDayOfWeek = getDayName(instance.getDate());
                    Log.d("InstanceFilter", "Instance date: " + instance.getDate() + ", getDayName: " + instanceDayOfWeek + ", selectedDays: " + selectedDays);
                    boolean dayMatch = true;
                    if (!selectedDays.isEmpty()) {
                        dayMatch = false;
                        for (String selected : selectedDays) {
                            if (instanceDayOfWeek.equalsIgnoreCase(selected.trim())) {
                                dayMatch = true;
                                break;
                            }
                        }
                    }
                    if (!dayMatch) continue;

                    boolean textMatch = true;
                    if (!query.isEmpty()) {
                        textMatch = false;
                    if (currentSearchBy.equals("Search by Teacher")) {
                            textMatch = instance.getTeacher() != null && instance.getTeacher().toLowerCase().contains(query.toLowerCase());
                    } else if (currentSearchBy.equals("Search by Instance")) {
                            String courseName = courseNameCache.get(instance.getCourseId());
                            textMatch = courseName != null && courseName.toLowerCase().contains(query.toLowerCase());
                        } else if (currentSearchBy.equals("Search by Type")) {
                            // Get course type from cache
                        YogaCourse course = db.courseDao().getById(instance.getCourseId());
                            String courseType = course != null ? course.getType() : "";
                            textMatch = courseType != null && courseType.toLowerCase().contains(query.toLowerCase());
                        }
                    }
                    if (textMatch) {
                        filteredList.add(instance);
                    }
                }
            }

            requireActivity().runOnUiThread(() -> {
                adapter.updateInstances(filteredList);
                tvStatsInstances.setText(String.valueOf(filteredList.size()));
                int totalEnrolled = 0;
                for (YogaInstance instance : filteredList) {
                    totalEnrolled += instance.getEnrolled();
                }
                tvStatsMembersRegistered.setText(String.valueOf(totalEnrolled));

                if (filteredList.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
        });
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
                // Always use English for abbreviation
                return new SimpleDateFormat("EEE", Locale.ENGLISH).format(calendar.getTime()).trim();
            } catch (Exception ignored) {
                // Try next format
            }
        }
        return ""; // Return empty string if no format matches
    }
}