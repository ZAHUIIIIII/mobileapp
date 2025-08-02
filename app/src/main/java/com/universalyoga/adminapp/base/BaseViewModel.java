package com.universalyoga.adminapp.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base ViewModel class that provides common functionality for all ViewModels.
 * Implements common patterns like loading states, error handling, and background operations.
 */
public abstract class BaseViewModel extends ViewModel {
    
    protected final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    protected final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    protected final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    protected final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    /**
     * Get loading state LiveData
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Get error message LiveData
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Get success message LiveData
     */
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
    
    /**
     * Set loading state
     */
    protected void setLoading(boolean loading) {
        isLoading.postValue(loading);
    }
    
    /**
     * Set error message
     */
    protected void setError(String error) {
        errorMessage.postValue(error);
    }
    
    /**
     * Set success message
     */
    protected void setSuccess(String success) {
        successMessage.postValue(success);
    }
    
    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.postValue(null);
    }
    
    /**
     * Clear success message
     */
    public void clearSuccess() {
        successMessage.postValue(null);
    }
    
    /**
     * Execute task in background thread
     */
    protected void executeInBackground(Runnable task) {
        executor.execute(() -> {
            try {
                setLoading(true);
                task.run();
            } catch (Exception e) {
                setError("An error occurred: " + e.getMessage());
            } finally {
                setLoading(false);
            }
        });
    }
    
    /**
     * Execute task in background thread with custom error handling
     */
    protected void executeInBackground(Runnable task, String errorMessage) {
        executor.execute(() -> {
            try {
                setLoading(true);
                task.run();
            } catch (Exception e) {
                setError(errorMessage);
            } finally {
                setLoading(false);
            }
        });
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
} 