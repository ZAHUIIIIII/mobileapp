package com.universalyoga.adminapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class InstancesFragment extends Fragment implements InstanceAdapter.OnInstanceActionListener {
    private RecyclerView recyclerView;
    private InstanceDao instanceDao;
    private InstanceAdapter adapter;
    private View emptyStateLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instances, container, false);
        
        instanceDao = AppDatabase.getInstance(requireContext()).instanceDao();
        recyclerView = view.findViewById(R.id.recyclerInstances);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        
        // Floating Action Button for adding instances
        FloatingActionButton fabAddInstance = view.findViewById(R.id.fabAddInstance);
        fabAddInstance.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddInstanceActivity.class));
        });
        
        observeInstances();
        return view;
    }
    
    private void observeInstances() {
        adapter = new InstanceAdapter(java.util.Collections.emptyList(), this);
        recyclerView.setAdapter(adapter);
        instanceDao.getAllLive().observe(getViewLifecycleOwner(), new Observer<List<YogaInstance>>() {
            @Override
            public void onChanged(List<YogaInstance> instances) {
                adapter.updateInstances(instances);
                if (instances == null || instances.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onInstanceClick(YogaInstance instance) {
        // Not implemented for now, as per requirement
    }

    @Override
    public void onEditClick(YogaInstance instance) {
        Intent intent = new Intent(requireContext(), EditInstanceActivity.class);
        intent.putExtra("instanceId", instance.getId());
        startActivity(intent);
    }

    @Override
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
}