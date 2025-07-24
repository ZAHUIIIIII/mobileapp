package com.universalyoga.adminapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.models.YogaCourse;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.ViewHolder> {
    private List<YogaCourse> courses;
    public interface OnCourseClickListener {
        void onCourseClick(YogaCourse course);
    }
    private OnCourseClickListener listener;
    public interface OnCourseActionListener {
        void onEdit(YogaCourse course);
        void onDelete(YogaCourse course);
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
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        YogaCourse course = courses.get(position);
        holder.bind(course, listener);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public void updateCourses(List<YogaCourse> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged();
    }

    // Add this method to support swipe-to-delete
    public YogaCourse getCourseAt(int position) {
        return courses.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvSchedule, tvType, tvPrice, tvDetails;
        private android.widget.ImageView ivOverflowMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvSchedule = itemView.findViewById(R.id.tvCourseSchedule);
            tvType = itemView.findViewById(R.id.tvCourseType);
            tvPrice = itemView.findViewById(R.id.tvCoursePrice);
            tvDetails = itemView.findViewById(R.id.tvCourseDetails);
            ivOverflowMenu = itemView.findViewById(R.id.ivOverflowMenu);
        }

        public void bind(final YogaCourse course, final OnCourseClickListener listener) {
            tvTitle.setText(course.getCourseName());
            tvType.setText(course.getType());
            tvPrice.setText(String.format(Locale.UK, "£%.2f", course.getPrice()));
            tvSchedule.setText(String.format(Locale.UK, "%s @ %s", course.getDaysOfWeek(), course.getTime()));
            tvDetails.setText(String.format(Locale.UK, "%d min • %d people", course.getDuration(), course.getCapacity()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });
            ivOverflowMenu.setOnClickListener(v -> {
                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(itemView.getContext(), ivOverflowMenu);
                popup.getMenuInflater().inflate(R.menu.menu_course_item, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        if (actionListener != null) actionListener.onEdit(course);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete) {
                        if (actionListener != null) actionListener.onDelete(course);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}

