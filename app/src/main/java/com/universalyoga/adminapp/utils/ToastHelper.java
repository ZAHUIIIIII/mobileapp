package com.universalyoga.adminapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;

public class ToastHelper {
    
    private static final String PREF_NAME = "toast_preferences";
    private static final String PREF_FIRST_TIME = "first_time_";
    private static final String PREF_LAST_SHOWN = "last_shown_";
    private static final long MIN_INTERVAL = 3000; // 3 seconds minimum between toasts
    
    private static Map<String, Long> lastToastTime = new HashMap<>();
    private static SharedPreferences preferences;
    
    public static String pendingToastMessage = null;
    
    /**
     * Initialize the ToastHelper with context
     */
    public static void init(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }
    
    /**
     * Show a toast message with intelligent timing
     */
    public static void showToast(Context context, String message) {
        if (context == null) return;
        
        init(context);
        
        long currentTime = System.currentTimeMillis();
        long lastTime = lastToastTime.getOrDefault(message, 0L);
        
        // Only show if enough time has passed since last similar toast
        if (currentTime - lastTime > MIN_INTERVAL) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            lastToastTime.put(message, currentTime);
        }
    }
    
    /**
     * Show a toast message only on first time or when explicitly requested
     */
    public static void showFirstTimeToast(Context context, String key, String message) {
        if (context == null) return;
        
        init(context);
        
        boolean isFirstTime = preferences.getBoolean(PREF_FIRST_TIME + key, true);
        if (isFirstTime) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            preferences.edit().putBoolean(PREF_FIRST_TIME + key, false).apply();
        }
    }
    
    /**
     * Show a contextual help toast with smart timing
     */
    public static void showContextualHelp(Context context, String fieldKey, String helpMessage) {
        if (context == null) return;
        
        init(context);
        
        long currentTime = System.currentTimeMillis();
        long lastTime = preferences.getLong(PREF_LAST_SHOWN + fieldKey, 0L);
        
        // Show help if it's been more than 30 seconds since last shown for this field
        if (currentTime - lastTime > 30000) {
            Toast.makeText(context, helpMessage, Toast.LENGTH_SHORT).show();
            preferences.edit().putLong(PREF_LAST_SHOWN + fieldKey, currentTime).apply();
        }
    }
    
    /**
     * Show error toast with priority (always shows)
     */
    public static void showErrorToast(Context context, String errorMessage) {
        if (context == null) return;
        
        Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Show success toast with priority (always shows)
     */
    public static void showSuccessToast(Context context, String successMessage) {
        if (context == null) return;
        
        Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Reset first-time flags for testing
     */
    public static void resetFirstTimeFlags(Context context) {
        if (context == null) return;
        
        init(context);
        preferences.edit().clear().apply();
    }
    
    /**
     * Check if it's the first time for a specific key
     */
    public static boolean isFirstTime(Context context, String key) {
        if (context == null) return true;
        
        init(context);
        return preferences.getBoolean(PREF_FIRST_TIME + key, true);
    }
} 