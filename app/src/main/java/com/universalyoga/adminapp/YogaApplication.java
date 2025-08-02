package com.universalyoga.adminapp;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.universalyoga.adminapp.services.FirebaseService;

public class YogaApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Enable offline persistence for Realtime Database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        
        // Initialize Firebase services
        FirebaseService.initialize();
    }
} 