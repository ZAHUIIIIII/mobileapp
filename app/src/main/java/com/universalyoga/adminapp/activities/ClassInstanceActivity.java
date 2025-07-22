package com.universalyoga.adminapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.Observer;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.adapters.InstanceAdapter;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.List;

public class ClassInstanceActivity extends AppCompatActivity {
    private RecyclerView rvInstances;
    private InstanceDao instanceDao;
    private InstanceAdapter adapter;
    
    @Override protected void onCreate(Bundle s){
        super.onCreate(s); 
        setContentView(R.layout.activity_instance);
        
        instanceDao = AppDatabase.getInstance(this).instanceDao();
        rvInstances = findViewById(R.id.instanceRecycler);
        rvInstances.setLayoutManager(new LinearLayoutManager(this));
        
        // Add Instance button
        Button btnAddInstance = findViewById(R.id.btnAddInstance);
        btnAddInstance.setOnClickListener(v -> {
            startActivity(new Intent(this, AddInstanceActivity.class));
        });
        
        observeInstances();
    }
    
    private void observeInstances() {
        adapter = new InstanceAdapter(java.util.Collections.emptyList(), instance -> {
            // Handle instance click - could show details or edit
            // For now, just show a toast
        });
        rvInstances.setAdapter(adapter);
        instanceDao.getAllLive().observe(this, new Observer<List<YogaInstance>>() {
            @Override
            public void onChanged(List<YogaInstance> instances) {
                adapter.updateInstances(instances);
            }
        });
    }
}
