package com.universalyoga.adminapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.models.YogaCourse;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.ViewHolder> {
    private List<YogaCourse> courses;
    private Map<Integer, Integer> courseInstanceCounts = new HashMap<>();
    
    public interface OnCourseClickListener {
        void onCourseClick(YogaCourse course);
    }
    private OnCourseClickListener listener;
    
    public interface OnCourseActionListener {
        void onEdit(YogaCourse course);
        void onDelete(YogaCourse course);
        void onManageInstances(YogaCourse course);
    }
    private OnCourseActionListener actionListener;

    public CourseListAdapter(List<YogaCourse> courses, OnCourseClickListener listener, OnCourseActionListener actionListener) {
        this.courses = courses;
        this.listener = listener;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        YogaCourse course = courses.get(position);
        holder.bind(course, listener, actionListener);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public void updateCourses(List<YogaCourse> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged();
    }
    
    public void updateInstanceCounts(Map<Integer, Integer> instanceCounts) {
        this.courseInstanceCounts.clear();
        this.courseInstanceCounts.putAll(instanceCounts);
        notifyDataSetChanged();
    }

    public YogaCourse getCourseAt(int position) {
        return courses.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCourseTitle, tvCourseSchedule, tvCourseTime, tvPrice, tvDuration;
        private TextView tvCapacity, tvLocation, tvInstructor, tvAdditionalInfo, tvInstancesCount;
        private TextView tvDifficultyBadge;
        private View btnViewInstances, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvCourseSchedule = itemView.findViewById(R.id.tvCourseSchedule);
            tvCourseTime = itemView.findViewById(R.id.tvCourseTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvInstructor = itemView.findViewById(R.id.tvInstructor);
            tvAdditionalInfo = itemView.findViewById(R.id.tvAdditionalInfo);
            tvInstancesCount = itemView.findViewById(R.id.tvInstancesCount);
            tvDifficultyBadge = itemView.findViewById(R.id.tvDifficultyBadge);
            
            btnViewInstances = itemView.findViewById(R.id.btnViewInstances);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(final YogaCourse course, final OnCourseClickListener listener, final OnCourseActionListener actionListener) {
            // Basic course information - use type as course name
            tvCourseTitle.setText(course.getType());
            tvPrice.setText(String.format(Locale.UK, "£%.0f", course.getPrice()));
            tvDuration.setText(String.format(Locale.UK, "%d min", course.getDuration()));
            
            // Schedule and time
            tvCourseSchedule.setText(course.getDaysOfWeek());
            tvCourseTime.setText(course.getTime());
            
            // Capacity and location
            tvCapacity.setText(String.format(Locale.UK, "Capacity: %d", course.getCapacity()));
            
            if (course.getRoomLocation() != null && !course.getRoomLocation().trim().isEmpty()) {
                tvLocation.setText(course.getRoomLocation());
            } else {
                tvLocation.setText("TBD");
            }
            
            // Instructor - display properly
            String instructor = course.getInstructor();
            if (instructor != null && !instructor.trim().isEmpty()) {
                tvInstructor.setText("Instructor: " + instructor);
            } else {
                tvInstructor.setText("Instructor: TBD");
            }
            
            // Additional info - show course description if available
            String description = course.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                tvAdditionalInfo.setVisibility(View.VISIBLE);
                tvAdditionalInfo.setText(description);
            } else {
                tvAdditionalInfo.setVisibility(View.GONE);
            }
            
            // Instances count - show actual count or placeholder
            Integer instanceCount = courseInstanceCounts.get(course.getId());
            if (instanceCount != null && instanceCount > 0) {
                tvInstancesCount.setText(instanceCount + " instance" + (instanceCount > 1 ? "s" : ""));
            } else {
                tvInstancesCount.setText("0 instances");
            }
            
            // Difficulty badge
            String difficulty = course.getDifficulty();
            if (difficulty != null && !difficulty.trim().isEmpty()) {
                tvDifficultyBadge.setVisibility(View.VISIBLE);
                tvDifficultyBadge.setText(difficulty);
            } else {
                tvDifficultyBadge.setVisibility(View.GONE);
            }
            
            // Click listeners
            itemView.setOnClickListener(v -> {
                // Check if the click is on a button - if so, don't trigger item click
                if (v.getId() == R.id.btnViewInstances || v.getId() == R.id.btnEdit || v.getId() == R.id.btnDelete) {
                    return;
                }
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });
            
            // Action button listeners
            if (btnViewInstances != null) {
                btnViewInstances.setOnClickListener(v -> {
                    v.setClickable(false); // Prevent double clicks
                    try {
                        if (actionListener != null) {
                            actionListener.onManageInstances(course);
                        }
                    } catch (Exception e) {
                        // Log error for debugging
                        android.util.Log.e("CourseListAdapter", "Error in btnViewInstances click: " + e.getMessage());
                    } finally {
                        v.setClickable(true); // Re-enable clicks
                    }
                });
            }
            
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    v.setClickable(false); // Prevent double clicks
                    try {
                        if (actionListener != null) {
                            actionListener.onEdit(course);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("CourseListAdapter", "Error in btnEdit click: " + e.getMessage());
                    } finally {
                        v.setClickable(true); // Re-enable clicks
                    }
                });
            }
            
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    v.setClickable(false); // Prevent double clicks
                    try {
                        if (actionListener != null) {
                            actionListener.onDelete(course);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("CourseListAdapter", "Error in btnDelete click: " + e.getMessage());
                    } finally {
                        v.setClickable(true); // Re-enable clicks
                    }
                });
            }
        }
    }
}

