package com.universalyoga.adminapp.base;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import com.universalyoga.adminapp.R;

/**
 * Base fragment class that provides common functionality for all fragments.
 * Implements common patterns like error handling, loading states, and navigation.
 */
public abstract class BaseFragment extends Fragment {
    
    protected View loadingView;
    protected View contentView;
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupObservers();
        setupListeners();
    }
    
    /**
     * Initialize views and find them by ID
     */
    protected abstract void initializeViews(View view);
    
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
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show a toast message with custom duration
     */
    protected void showToast(String message, int duration) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, duration).show();
        }
    }
    
    /**
     * Show a snackbar message
     */
    protected void showSnackbar(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show a snackbar message with action
     */
    protected void showSnackbar(String message, String actionText, View.OnClickListener action) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
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
     * Get ViewModel with proper lifecycle management and custom ViewModelProvider.Factory
     */
    protected <T extends androidx.lifecycle.ViewModel> T getViewModel(Class<T> modelClass, 
                                                                   ViewModelProvider.Factory factory) {
        return new ViewModelProvider(this, factory).get(modelClass);
    }
    
    /**
     * Navigate to another fragment
     */
    protected void navigateToFragment(Fragment fragment, boolean addToBackStack) {
        if (getActivity() != null) {
            androidx.fragment.app.FragmentTransaction transaction = 
                getActivity().getSupportFragmentManager().beginTransaction();
            
            transaction.replace(R.id.fragment_container, fragment);
            
            if (addToBackStack) {
                transaction.addToBackStack(null);
            }
            
            transaction.commit();
        }
    }
    
    /**
     * Check if fragment is still attached to activity
     */
    protected boolean isFragmentValid() {
        return getActivity() != null && isAdded() && !isDetached();
    }
} 