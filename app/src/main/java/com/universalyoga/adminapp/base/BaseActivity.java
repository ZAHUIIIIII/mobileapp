package com.universalyoga.adminapp.base;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import com.universalyoga.adminapp.R;

/**
 * Base activity class that provides common functionality for all activities.
 * Implements common patterns like error handling, loading states, and navigation.
 */
public abstract class BaseActivity extends AppCompatActivity {
    
    protected View loadingView;
    protected View contentView;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initializeViews();
        setupObservers();
        setupListeners();
    }
    
    /**
     * Get the layout resource ID for this activity
     */
    protected abstract int getLayoutId();
    
    /**
     * Initialize views and find them by ID
     */
    protected abstract void initializeViews();
    
    /**
     * Setup observers for ViewModels or other data sources
     */
    protected abstract void setupObservers();
    
    /**
     * Setup click listeners and other UI interactions
     */
    protected abstract void setupListeners();
    
    /**
     * Show loading state
     */
    protected void showLoading() {
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
        }
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Hide loading state
     */
    protected void hideLoading() {
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Show a toast message
     */
    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Show a toast message with custom duration
     */
    protected void showToast(String message, int duration) {
        Toast.makeText(this, message, duration).show();
    }
    
    /**
     * Show a snackbar message
     */
    protected void showSnackbar(String message) {
        if (contentView != null) {
            Snackbar.make(contentView, message, Snackbar.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show a snackbar message with action
     */
    protected void showSnackbar(String message, String actionText, View.OnClickListener action) {
        if (contentView != null) {
            Snackbar.make(contentView, message, Snackbar.LENGTH_LONG)
                    .setAction(actionText, action)
                    .show();
        }
    }
    
    /**
     * Show error message
     */
    protected void showError(String errorMessage) {
        showSnackbar(getString(R.string.error_prefix) + " " + errorMessage);
    }
    
    /**
     * Show success message
     */
    protected void showSuccess(String successMessage) {
        showSnackbar(getString(R.string.success_prefix) + " " + successMessage);
    }
    
    /**
     * Get ViewModel with proper lifecycle management
     */
    protected <T extends androidx.lifecycle.ViewModel> T getViewModel(Class<T> modelClass) {
        return new ViewModelProvider(this).get(modelClass);
    }
    
    /**
     * Handle back navigation
     */
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
} 