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
        private TextView tvCourseName, tvTeacherName, tvInstanceDate, tvInstanceDetails;
        private ImageView ivOverflowMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvInstanceDate = itemView.findViewById(R.id.tvInstanceDate);
            tvInstanceDetails = itemView.findViewById(R.id.tvInstanceDetails);
            ivOverflowMenu = itemView.findViewById(R.id.ivOverflowMenu);
        }

        public void bind(YogaInstance instance) {
            AppDatabase db = AppDatabase.getInstance(itemView.getContext());
            // Fetch course details on a background thread
            Executors.newSingleThreadExecutor().execute(() -> {
                YogaCourse course = db.courseDao().getById(instance.getCourseId());
                itemView.post(() -> {
                    if (course != null) {
                        tvCourseName.setText(course.getCourseName());
                        tvTeacherName.setText(String.format("with %s - %s", instance.getTeacher(), course.getRoomLocation()));
                        tvTeacherName.setCompoundDrawableTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.md_theme_secondary));
                    } else {
                        tvCourseName.setText("Unknown Course"); // Fallback for missing course
                        tvTeacherName.setText("with " + instance.getTeacher());
                    }
                });
            });

            tvTeacherName.setText("with " + instance.getTeacher());
            tvInstanceDate.setText(instance.getDate());
            tvInstanceDetails.setText(instance.getStartTime() + " - " + instance.getEndTime() + " • " + instance.getEnrolled() + " / " + instance.getCapacity() + " filled");

            ivOverflowMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.instance_item_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        if (listener != null) {
                            listener.onEditClick(instance);
                        }
                        return true;
                    } else if (item.getItemId() == R.id.action_delete) {
                        if (listener != null) {
                            listener.onDeleteClick(instance);
                        }
                        return true;
                    } else {
                        return false;
                    }
                });
                popup.show();
            });
        }
    }
}