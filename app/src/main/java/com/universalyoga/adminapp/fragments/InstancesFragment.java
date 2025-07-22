package com.universalyoga.adminapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.Observer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.activities.AddInstanceActivity;
import com.universalyoga.adminapp.adapters.InstanceAdapter;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.database.InstanceDao;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.List;

public class InstancesFragment extends Fragment {
    private RecyclerView recyclerView;
    private InstanceDao instanceDao;
    private InstanceAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instances, container, false);
        
        instanceDao = AppDatabase.getInstance(requireContext()).instanceDao();
        recyclerView = view.findViewById(R.id.recyclerInstances);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Floating Action Button for adding instances
        FloatingActionButton fabAddInstance = view.findViewById(R.id.fabAddInstance);
        fabAddInstance.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddInstanceActivity.class));
        });
        
        observeInstances();
        return view;
    }
    
    private void observeInstances() {
        adapter = new InstanceAdapter(java.util.Collections.emptyList(), instance -> {
            // Handle instance click - could show details or edit
        });
        recyclerView.setAdapter(adapter);
        instanceDao.getAllLive().observe(getViewLifecycleOwner(), new Observer<List<YogaInstance>>() {
            @Override
            public void onChanged(List<YogaInstance> instances) {
                adapter.updateInstances(instances);
            }
        });
    }
} 