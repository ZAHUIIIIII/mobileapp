package com.universalyoga.adminapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.base.BaseActivity;
import com.universalyoga.adminapp.fragments.CoursesFragment;
import com.universalyoga.adminapp.fragments.CustomerBookingsFragment;
import com.universalyoga.adminapp.fragments.DashboardFragment;
import com.universalyoga.adminapp.fragments.InstancesFragment;
import com.universalyoga.adminapp.fragments.UploadFragment;
import com.universalyoga.adminapp.utils.ToastHelper;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.CourseDao;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.List;

/**
 * Main activity that hosts the bottom navigation and manages fragment switching.
 * Refactored to use BaseActivity for better architecture.
 */
public class MainActivity extends BaseActivity {
    
    private BottomNavigationView bottomNavigationView;
    private TextView tvClassesCount, tvInstancesCount, tvPageTitle, tvPendingSync;
    private CourseDao courseDao;
    private InstanceDao instanceDao;
    
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }
    
    @Override
    protected void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        contentView = findViewById(R.id.fragment_container);
        
        // Initialize header TextViews
        tvClassesCount = findViewById(R.id.tv_classes_count);
        tvInstancesCount = findViewById(R.id.tv_instances_count);
        tvPageTitle = findViewById(R.id.tv_page_title);
        tvPendingSync = findViewById(R.id.tv_pending_sync);
        
        // Initialize database DAOs
        try {
            AppDatabase database = AppDatabase.getInstance(this);
            courseDao = database.courseDao();
            instanceDao = database.instanceDao();
        } catch (Exception e) {
            // Database initialization failed
        }
    }
    
    @Override
    protected void setupObservers() {
        // Observe course count
        if (courseDao != null) {
            courseDao.getAllLive().observe(this, new Observer<List<YogaCourse>>() {
                @Override
                public void onChanged(List<YogaCourse> courses) {
                    try {
                        int courseCount = courses != null ? courses.size() : 0;
                        tvClassesCount.setText(courseCount + " classes");
                        updatePendingSyncCount();
                    } catch (Exception e) {
                        // Error updating course count
                    }
                }
            });
        }
        
        // Observe instance count
        if (instanceDao != null) {
            instanceDao.getAllLive().observe(this, new Observer<List<YogaInstance>>() {
                @Override
                public void onChanged(List<YogaInstance> instances) {
                    try {
                        int instanceCount = instances != null ? instances.size() : 0;
                        tvInstancesCount.setText(instanceCount + " instances");
                        updatePendingSyncCount();
                    } catch (Exception e) {
                        // Error updating instance count
                    }
                }
            });
        }
    }
    
    private void updatePendingSyncCount() {
        try {
            if (courseDao != null && instanceDao != null) {
                // Count unsynced items (syncStatus = 0)
                int unsyncedCourses = courseDao.getUnsyncedCount();
                int unsyncedInstances = instanceDao.getUnsyncedCount();
                int totalUnsynced = unsyncedCourses + unsyncedInstances;
                
                if (totalUnsynced > 0) {
                    tvPendingSync.setText(" • " + totalUnsynced + " pending sync");
                    tvPendingSync.setVisibility(View.VISIBLE);
                } else {
                    tvPendingSync.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            // Error updating pending sync count
            tvPendingSync.setVisibility(View.GONE);
        }
    }
    
    @Override
    protected void setupListeners() {
        // Bottom navigation listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String pageTitle = "Dashboard";
            
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
                pageTitle = "Dashboard";
            } else if (itemId == R.id.nav_courses) {
                selectedFragment = new CoursesFragment();
                pageTitle = "Courses";
            } else if (itemId == R.id.nav_instances) {
                selectedFragment = new InstancesFragment();
                pageTitle = "Instances";
            } else if (itemId == R.id.nav_customer_bookings) {
                selectedFragment = new CustomerBookingsFragment();
                pageTitle = "Bookings";
            } else if (itemId == R.id.nav_upload) {
                selectedFragment = new UploadFragment();
                pageTitle = "Upload";
            }

            if (selectedFragment != null) {
                // Update page title and load fragment
                updatePageTitle(pageTitle);
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
        

    }
    
    /**
     * Update the page title in the header
     */
    private void updatePageTitle(String title) {
        if (tvPageTitle != null) {
            tvPageTitle.setText(title);
            android.util.Log.d("MainActivity", "Page title updated to: " + title);
        }
    }
    

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Show first-time welcome message
        ToastHelper.showFirstTimeToast(this, "welcome", "Welcome to Yoga Course Manager! Tap the + button to add your first class.");
        
        // Set default fragment if this is the first time
        if (savedInstanceState == null) {
            // Set initial page title and load default fragment
            tvPageTitle.setText("Dashboard");
            loadFragment(new DashboardFragment());
            // Set the default selected item in bottom navigation
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        }
    }

    /**
     * Load a fragment into the container
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        
        // Ensure title is updated after fragment transaction
        getSupportFragmentManager().executePendingTransactions();
    }
    
    @Override
    public void onBackPressed() {
        // Handle back navigation and update title accordingly
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
            // Update title based on current fragment
            updateTitleForCurrentFragment();
        } else {
            super.onBackPressed();
        }
    }
    
    /**
     * Update title based on the currently displayed fragment
     */
    private void updateTitleForCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        
        if (currentFragment != null) {
            String title = "Dashboard"; // Default
            
            if (currentFragment instanceof DashboardFragment) {
                title = "Dashboard";
            } else if (currentFragment instanceof CoursesFragment) {
                title = "Courses";
            } else if (currentFragment instanceof InstancesFragment) {
                title = "Instances";
            } else if (currentFragment instanceof CustomerBookingsFragment) {
                title = "Bookings";
            } else if (currentFragment instanceof UploadFragment) {
                title = "Upload";
            }
            
            updatePageTitle(title);
        }
    }
}