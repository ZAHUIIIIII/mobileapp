package com.universalyoga.adminapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.models.Activity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
    private List<Activity> activities = new ArrayList<>();

    public void setActivities(List<Activity> activities) {
        this.activities = activities != null ? activities : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        try {
            Activity activity = activities.get(position);
            if (activity != null) {
                holder.bind(activity);
            } else {
                holder.bindEmpty();
            }
        } catch (Exception e) {
            holder.bindEmpty();
        }
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivActivityIcon;
        private TextView tvActivityDescription;
        private TextView tvActivityTime;
        private TextView tvActivityType;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            ivActivityIcon = itemView.findViewById(R.id.ivActivityIcon);
            tvActivityDescription = itemView.findViewById(R.id.tvActivityDescription);
            tvActivityTime = itemView.findViewById(R.id.tvActivityTime);
            tvActivityType = itemView.findViewById(R.id.tvActivityType);
        }

        public void bind(Activity activity) {
            if (activity == null) {
                bindEmpty();
                return;
            }
            
            try {
                // Set description
                String description = activity.getDescription();
                if (description != null && !description.trim().isEmpty()) {
                    tvActivityDescription.setText(description.trim());
                } else {
                    tvActivityDescription.setText("Unknown activity");
                }
                
                // Set time
                String timestamp = activity.getTimestamp();
                if (timestamp != null && !timestamp.trim().isEmpty()) {
                    tvActivityTime.setText(formatTimeAgo(timestamp));
                } else {
                    tvActivityTime.setText("Unknown time");
                }
                
                // Set type badge
                String type = activity.getType();
                if (type != null && !type.trim().isEmpty()) {
                    tvActivityType.setText(type.trim().toUpperCase());
                } else {
                    tvActivityType.setText("UNKNOWN");
                }
                
                // Set icon based on activity type
                setActivityIcon(type);
            } catch (Exception e) {
                bindEmpty();
            }
        }
        
        public void bindEmpty() {
            try {
                tvActivityDescription.setText("Invalid activity");
                tvActivityTime.setText("Unknown time");
                tvActivityType.setText("UNKNOWN");
                ivActivityIcon.setImageResource(R.drawable.ic_activity);
            } catch (Exception e) {
                // Ignore binding errors
            }
        }

        private void setActivityIcon(String type) {
            try {
                if (type == null) {
                    ivActivityIcon.setImageResource(R.drawable.ic_activity);
                    return;
                }
                
                switch (type.toLowerCase().trim()) {
                    case "course":
                        ivActivityIcon.setImageResource(R.drawable.ic_book_open);
                        break;
                    case "instance":
                        ivActivityIcon.setImageResource(R.drawable.ic_calendar);
                        break;
                    case "sync":
                        ivActivityIcon.setImageResource(R.drawable.ic_cloud);
                        break;
                    default:
                        ivActivityIcon.setImageResource(R.drawable.ic_activity);
                        break;
                }
            } catch (Exception e) {
                ivActivityIcon.setImageResource(R.drawable.ic_activity);
            }
        }

        private String formatTimeAgo(String timestamp) {
            try {
                if (timestamp == null || timestamp.trim().isEmpty()) {
                    return "Unknown time";
                }
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date activityTime = sdf.parse(timestamp.trim());
                if (activityTime == null) {
                    return "Unknown time";
                }
                
                Date now = new Date();
                long diffInMillis = now.getTime() - activityTime.getTime();
                
                // Handle future timestamps
                if (diffInMillis < 0) {
                    return "Just now";
                }
                
                long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
                long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
                
                if (diffInMinutes < 1) {
                    return "Just now";
                } else if (diffInMinutes < 60) {
                    return diffInMinutes + " min ago";
                } else if (diffInHours < 24) {
                    return diffInHours + " hour" + (diffInHours > 1 ? "s" : "") + " ago";
                } else {
                    return diffInDays + " day" + (diffInDays > 1 ? "s" : "") + " ago";
                }
            } catch (Exception e) {
                return "Unknown time";
            }
        }
    }
} 