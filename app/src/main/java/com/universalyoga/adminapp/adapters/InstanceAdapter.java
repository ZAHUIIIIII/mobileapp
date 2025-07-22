package com.universalyoga.adminapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.List;

public class InstanceAdapter extends RecyclerView.Adapter<InstanceAdapter.ViewHolder> {
    private List<YogaInstance> instances;
    private OnInstanceClickListener listener;
    
    public interface OnInstanceClickListener {
        void onInstanceClick(YogaInstance instance);
    }
    
    public InstanceAdapter(List<YogaInstance> instances, OnInstanceClickListener listener) {
        this.instances = instances;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_instance, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        YogaInstance instance = instances.get(position);
        holder.bind(instance);
    }
    
    @Override
    public int getItemCount() {
        return instances.size();
    }
    
    public void updateInstances(List<YogaInstance> newInstances) {
        this.instances = newInstances;
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate, tvTeacher, tvComments;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvComments = itemView.findViewById(R.id.tvComments);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onInstanceClick(instances.get(position));
                }
            });
        }
        
        public void bind(YogaInstance instance) {
            tvDate.setText(instance.getDate());
            tvTeacher.setText(instance.getTeacher());
            tvComments.setText(instance.getComments());
        }
    }
}
