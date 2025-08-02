package com.universalyoga.adminapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.database.AppDatabase;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.List;
import java.util.concurrent.Executors;

public class InstanceAdapter extends RecyclerView.Adapter<InstanceAdapter.ViewHolder> {
    private List<YogaInstance> instances;
    private OnInstanceActionListener listener;

    public interface OnInstanceActionListener {
        void onEditClick(YogaInstance instance);
        void onDeleteClick(YogaInstance instance);
    }

    public InstanceAdapter(List<YogaInstance> instances, OnInstanceActionListener listener) {
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
        private TextView tvCourseName, tvDate, tvInstanceDetails, tvTime, tvTeacher, tvEnrollment, tvLocation, tvEnrollmentProgress;
        private View btnEdit, btnDelete;
        private com.google.android.material.progressindicator.LinearProgressIndicator progressEnrollment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvInstanceDetails = itemView.findViewById(R.id.tvInstanceDetails);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvEnrollment = itemView.findViewById(R.id.tvEnrollment);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvEnrollmentProgress = itemView.findViewById(R.id.tvEnrollmentProgress);
            progressEnrollment = itemView.findViewById(R.id.progressEnrollment);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(YogaInstance instance) {
            AppDatabase db = AppDatabase.getInstance(itemView.getContext());
            // Fetch course details on a background thread
            Executors.newSingleThreadExecutor().execute(() -> {
                YogaCourse course = db.courseDao().getById(instance.getCourseId());
                itemView.post(() -> {
                    if (course != null) {
                        tvCourseName.setText(course.getCourseName());
                        // Display comments if available, otherwise show a default message
                        String comments = instance.getComments();
                        if (comments != null && !comments.trim().isEmpty()) {
                            tvInstanceDetails.setText(comments);
                        } else {
                            tvInstanceDetails.setText(String.format("Class instance for %s", course.getCourseName()));
                        }
                        tvLocation.setText(course.getRoomLocation() != null ? course.getRoomLocation() : "TBD");
                    } else {
                        tvCourseName.setText("Unknown Course"); // Fallback for missing course
                        // Display comments if available, otherwise show a default message
                        String comments = instance.getComments();
                        if (comments != null && !comments.trim().isEmpty()) {
                            tvInstanceDetails.setText(comments);
                        } else {
                            tvInstanceDetails.setText("Class instance details");
                        }
                        tvLocation.setText("TBD");
                    }
                });
            });

            tvDate.setText(instance.getDate());
            tvTime.setText(instance.getStartTime() + " - " + instance.getEndTime());
            tvTeacher.setText(instance.getTeacher());
            tvEnrollment.setText(instance.getEnrolled() + "/" + instance.getCapacity() + " enrolled");

            // Calculate and set enrollment progress
            int enrolled = instance.getEnrolled();
            int capacity = instance.getCapacity();
            int progress = capacity > 0 ? (enrolled * 100) / capacity : 0;
            tvEnrollmentProgress.setText(progress + "%");
            progressEnrollment.setProgress(progress);

            // Button click listeners
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEditClick(instance);
                    }
                });
            }

            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClick(instance);
                    }
                });
            }
        }
    }
}