package com.universalyoga.adminapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.activities.AddCourseActivity;
import com.universalyoga.adminapp.activities.AddInstanceActivity;
import com.universalyoga.adminapp.adapters.ActivityAdapter;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.ActivityDao;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.Activity;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import com.universalyoga.adminapp.utils.ActivityLogger;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {
    private TextView tvTotalCourses, tvTotalInstances, tvTotalCapacity, tvAvgPrice;
    private LinearLayout layoutEmptyActivity;
    private RecyclerView rvRecentActivity;
    private ActivityAdapter activityAdapter;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    private ActivityDao activityDao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        // Initialize database DAOs
        try {
            AppDatabase database = AppDatabase.getInstance(requireContext());
            courseDao = database.courseDao();
            instanceDao = database.instanceDao();
            activityDao = database.activityDao();
        } catch (Exception e) {
            // Database initialization failed
        }
        
        // Initialize stats TextViews
        tvTotalCourses = view.findViewById(R.id.tvTotalCourses);
        tvTotalInstances = view.findViewById(R.id.tvTotalInstances);
        tvTotalCapacity = view.findViewById(R.id.tvTotalCapacity);
        tvAvgPrice = view.findViewById(R.id.tvAvgPrice);
        
        // Initialize recent activity views
        layoutEmptyActivity = view.findViewById(R.id.layout_empty_activity);
        rvRecentActivity = view.findViewById(R.id.rv_recent_activity);
        
        // Setup RecyclerView for recent activity
        activityAdapter = new ActivityAdapter();
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentActivity.setAdapter(activityAdapter);
        
        // Quick action cards
        CardView cardAddCourse = view.findViewById(R.id.cardAddCourse);
        CardView cardAddInstance = view.findViewById(R.id.cardAddInstance);
        CardView cardViewCourses = view.findViewById(R.id.cardViewCourses);
        
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
        
        // Load data
        observeStats();
        observeRecentActivity();
        
        return view;
    }
    
    private void observeStats() {
        if (courseDao == null) {
            return;
        }
        
        courseDao.getAllLive().observe(getViewLifecycleOwner(), new Observer<List<YogaCourse>>() {
            @Override
            public void onChanged(List<YogaCourse> courses) {
                try {
                    int courseCount = courses != null ? courses.size() : 0;
                    tvTotalCourses.setText(String.valueOf(courseCount));
                    
                    // Calculate total capacity
                    int totalCapacity = 0;
                    if (courses != null) {
                        for (YogaCourse course : courses) {
                            totalCapacity += course.getCapacity();
                        }
                    }
                    
                    tvTotalCapacity.setText(String.valueOf(totalCapacity));
                    
                    // Calculate average price based on member registrations and course prices
                    calculateAveragePrice(courses);
                } catch (Exception e) {
                    // Error updating course stats
                }
            }
        });
        
        if (instanceDao == null) {
            return;
        }
        
        instanceDao.getAllLive().observe(getViewLifecycleOwner(), new Observer<List<YogaInstance>>() {
            @Override
            public void onChanged(List<YogaInstance> instances) {
                try {
                    tvTotalInstances.setText(String.valueOf(instances != null ? instances.size() : 0));
                } catch (Exception e) {
                    // Error updating instance stats
                }
            }
        });
    }
    
    private void observeRecentActivity() {
        if (activityDao == null) {
            showEmptyActivityState();
            return;
        }
        
        // Use the new method to get only recent activities (limit to 5)
        activityDao.getRecentLive(5).observe(getViewLifecycleOwner(), new Observer<List<Activity>>() {
            @Override
            public void onChanged(List<Activity> activities) {
                try {
                    if (activities != null && !activities.isEmpty()) {
                        activityAdapter.setActivities(activities);
                        showActivityList();
                    } else {
                        // Show empty state
                        showEmptyActivityState();
                    }
                } catch (Exception e) {
                    showEmptyActivityState();
                }
            }
        });
    }
    
    private void showActivityList() {
        if (rvRecentActivity != null && layoutEmptyActivity != null) {
            rvRecentActivity.setVisibility(View.VISIBLE);
            layoutEmptyActivity.setVisibility(View.GONE);
        }
    }
    
    private void showEmptyActivityState() {
        if (rvRecentActivity != null && layoutEmptyActivity != null) {
            rvRecentActivity.setVisibility(View.GONE);
            layoutEmptyActivity.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Calculate average price based on member registrations and course prices
     * Formula: Average Price = (Total Member Registrations × Course Price) / Number of Courses
     */
    private void calculateAveragePrice(List<YogaCourse> courses) {
        if (instanceDao == null || courses == null || courses.isEmpty()) {
            tvAvgPrice.setText("£0.00");
            return;
        }
        
        // Get all instances to calculate total registrations
        instanceDao.getAllLive().observe(getViewLifecycleOwner(), new Observer<List<YogaInstance>>() {
            @Override
            public void onChanged(List<YogaInstance> instances) {
                try {
                    if (instances == null || instances.isEmpty()) {
                        tvAvgPrice.setText("£0.00");
                        return;
                    }
                    
                    // Create a map of course ID to course price for quick lookup
                    Map<Integer, Double> coursePriceMap = new HashMap<>();
                    for (YogaCourse course : courses) {
                        coursePriceMap.put(course.getId(), course.getPrice());
                    }
                    
                    // Calculate total revenue (member registrations × course price)
                    double totalRevenue = 0.0;
                    int totalRegistrations = 0;
                    
                    for (YogaInstance instance : instances) {
                        Double coursePrice = coursePriceMap.get(instance.getCourseId());
                        if (coursePrice != null) {
                            int enrolled = instance.getEnrolled();
                            double instanceRevenue = enrolled * coursePrice;
                            totalRevenue += instanceRevenue;
                            totalRegistrations += enrolled;
                            
                            android.util.Log.d("DashboardFragment", String.format(
                                "Instance %d: %d enrolled × £%.2f = £%.2f", 
                                instance.getId(), enrolled, coursePrice, instanceRevenue));
                        }
                    }
                    
                    // Calculate average revenue per course
                    double avgRevenue = courses.size() > 0 ? totalRevenue / courses.size() : 0.0;
                    tvAvgPrice.setText(String.format(Locale.UK, "£%.2f", avgRevenue));
                    
                    android.util.Log.d("DashboardFragment", String.format(
                        "Total Revenue: £%.2f, Total Registrations: %d, Courses: %d, Avg Revenue: £%.2f",
                        totalRevenue, totalRegistrations, courses.size(), avgRevenue));
                    
                } catch (Exception e) {
                    tvAvgPrice.setText("£0.00");
                }
            }
        });
    }
    
    // Method to manually create sample activities for testing
    public void createSampleActivities() {
        try {
            if (activityDao == null) {
                return;
            }
            
            // Create sample activities for demonstration
            ActivityLogger logger = ActivityLogger.getInstance(requireContext());
            
            // Add some sample activities
            logger.logCourseCreated("Yoga Basics");
            logger.logInstanceCreated("Yoga Basics - Monday 9:00 AM");
            logger.logSyncStarted();
            logger.logSyncCompleted(15);
            logger.logCourseCreated("Advanced Yoga");
        } catch (Exception e) {
            // Failed to create sample activities
        }
    }
} 