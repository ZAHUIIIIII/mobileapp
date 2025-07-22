package com.universalyoga.adminapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.R;
import java.util.List;
import java.util.function.Consumer;
import android.widget.CheckBox;
import java.util.HashSet;
import java.util.Set;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.VH> {
    private List<YogaCourse> data;
    private Consumer<YogaCourse> onClick;
    private boolean selectMode = false;
    private Set<Integer> selectedIds = new HashSet<>();
    public CourseListAdapter(List<YogaCourse> d, Consumer<YogaCourse> cb){data=d;onClick=cb;}
    public void setSelectMode(boolean enabled) {
        selectMode = enabled;
        if (!enabled) selectedIds.clear();
        notifyDataSetChanged();
    }
    public boolean isSelectMode() { return selectMode; }
    public void toggleSelection(int id) {
        if (selectedIds.contains(id)) selectedIds.remove(id);
        else selectedIds.add(id);
        notifyDataSetChanged();
    }
    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
    }
    public Set<Integer> getSelectedIds() { return selectedIds; }
    public List<YogaCourse> getSelectedCourses() {
        List<YogaCourse> result = new java.util.ArrayList<>();
        for (YogaCourse c : data) if (selectedIds.contains(c.getId())) result.add(c);
        return result;
    }
    @Override public VH onCreateViewHolder(ViewGroup p,int i){
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_course,p,false);
        return new VH(v);
    }
    @Override public void onBindViewHolder(VH h,int i){
        YogaCourse c=data.get(i);
        h.tvCourseTitle.setText(c.getCourseName());
        h.tvCourseSchedule.setText(c.getDate() + " @ " + c.getTime() + " • " + c.getDuration() + " min • " + c.getCapacity() + " people");
        h.tvCourseType.setText(c.getType());
        h.tvCoursePrice.setText("$" + String.format("%.2f", c.getPrice()));
        h.tvCourseDescription.setText(c.getDescription());
        if (selectMode) {
            h.checkBox.setVisibility(View.VISIBLE);
            h.checkBox.setChecked(selectedIds.contains(c.getId()));
            h.itemView.setOnClickListener(v -> toggleSelection(c.getId()));
        } else {
            h.checkBox.setVisibility(View.GONE);
            h.itemView.setOnClickListener(v -> onClick.accept(c));
        }
        h.itemView.setOnLongClickListener(v -> {
            if (!selectMode) {
                setSelectMode(true);
                toggleSelection(c.getId());
                return true;
            }
            return false;
        });
    }
    @Override public int getItemCount(){return data.size();}

    public void updateCourses(List<YogaCourse> newCourses) {
        this.data = newCourses;
        notifyDataSetChanged();
    }
    static class VH extends RecyclerView.ViewHolder{
        TextView tvCourseTitle, tvCourseSchedule, tvCourseType, tvCoursePrice, tvCourseDescription;
        CheckBox checkBox;
        public VH(View v){
            super(v);
            tvCourseTitle = v.findViewById(R.id.tvCourseTitle);
            tvCourseSchedule = v.findViewById(R.id.tvCourseSchedule);
            tvCourseType = v.findViewById(R.id.tvCourseType);
            tvCoursePrice = v.findViewById(R.id.tvCoursePrice);
            tvCourseDescription = v.findViewById(R.id.tvCourseDescription);
            checkBox = v.findViewById(R.id.checkBoxSelect);
        }
    }
}
