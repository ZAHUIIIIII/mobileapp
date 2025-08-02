package com.universalyoga.adminapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.models.SyncHistory;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SyncHistoryAdapter extends RecyclerView.Adapter<SyncHistoryAdapter.ViewHolder> {
    private List<SyncHistory> syncHistories;

    public SyncHistoryAdapter() {
        this.syncHistories = new ArrayList<>();
    }

    public SyncHistoryAdapter(List<SyncHistory> syncHistories) {
        this.syncHistories = syncHistories != null ? syncHistories : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_sync_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SyncHistory syncHistory = syncHistories.get(position);
        holder.bind(syncHistory);
    }

    @Override
    public int getItemCount() {
        return syncHistories.size();
    }

    public void setData(List<SyncHistory> newSyncHistories) {
        this.syncHistories = newSyncHistories != null ? newSyncHistories : new ArrayList<>();
        notifyDataSetChanged();
    }

    public SyncHistory getSyncHistoryAt(int position) {
        return syncHistories.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSyncType, tvSyncStatus, tvSyncTime, tvSyncDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSyncType = itemView.findViewById(R.id.tvSyncType);
            tvSyncStatus = itemView.findViewById(R.id.tvSyncStatus);
            tvSyncTime = itemView.findViewById(R.id.tvSyncTime);
            tvSyncDetails = itemView.findViewById(R.id.tvSyncDetails);
        }

        public void bind(SyncHistory syncHistory) {
            // Set sync type
            String type = syncHistory.getType() != null ? syncHistory.getType() : "Unknown";
            tvSyncType.setText(type);

            // Set sync status with color
            String status = syncHistory.getStatus() != null ? syncHistory.getStatus() : "Unknown";
            tvSyncStatus.setText(status);
            
            // Set status color based on status
            int statusColor;
            switch (status.toLowerCase()) {
                case "completed":
                case "success":
                    statusColor = itemView.getContext().getColor(R.color.green_600);
                    break;
                case "failed":
                case "error":
                    statusColor = itemView.getContext().getColor(R.color.destructive);
                    break;
                case "in_progress":
                case "pending":
                    statusColor = itemView.getContext().getColor(R.color.primary);
                    break;
                default:
                    statusColor = itemView.getContext().getColor(R.color.muted_foreground);
                    break;
            }
            tvSyncStatus.setTextColor(statusColor);

            // Set sync time
            String timestamp = syncHistory.getTimestamp();
            if (timestamp != null && !timestamp.isEmpty()) {
                try {
                    long timeInMillis = Long.parseLong(timestamp);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                    String formattedTime = sdf.format(new Date(timeInMillis));
                    tvSyncTime.setText(formattedTime);
                } catch (NumberFormatException e) {
                    tvSyncTime.setText(timestamp);
                }
            } else {
                tvSyncTime.setText("Unknown time");
            }

            // Set sync details based on available data
            StringBuilder details = new StringBuilder();
            if (syncHistory.getRecordsUploaded() != null) {
                SyncHistory.Records records = syncHistory.getRecordsUploaded();
                if (records.total > 0) {
                    details.append(records.total).append(" records uploaded");
                }
            }
            if (syncHistory.getDataSize() > 0) {
                if (details.length() > 0) details.append(", ");
                details.append(syncHistory.getDataSize()).append("KB");
            }
            
            if (details.length() > 0) {
                tvSyncDetails.setText(details.toString());
                tvSyncDetails.setVisibility(View.VISIBLE);
            } else {
                tvSyncDetails.setVisibility(View.GONE);
            }
        }
    }
} 