package com.universalyoga.adminapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import java.util.List;
import androidx.annotation.NonNull;

@Entity(tableName = "sync_history")
public class SyncHistory {
    @PrimaryKey
    @NonNull
    private String id;
    private String timestamp;
    private String status;
    private String type;
    private String trigger;
    private Integer duration;
    @Ignore
    private Records recordsProcessed;
    @Ignore
    private Records recordsUploaded;
    @Ignore
    private Records recordsSkipped;
    @Ignore
    private List<String> errors;
    private int retryCount;
    private int dataSize;
    @Ignore
    private String networkSpeed;
    @Ignore
    private DeviceInfo deviceInfo;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTrigger() { return trigger; }
    public void setTrigger(String trigger) { this.trigger = trigger; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Records getRecordsProcessed() { return recordsProcessed; }
    public void setRecordsProcessed(Records recordsProcessed) { this.recordsProcessed = recordsProcessed; }
    public Records getRecordsUploaded() { return recordsUploaded; }
    public void setRecordsUploaded(Records recordsUploaded) { this.recordsUploaded = recordsUploaded; }
    public Records getRecordsSkipped() { return recordsSkipped; }
    public void setRecordsSkipped(Records recordsSkipped) { this.recordsSkipped = recordsSkipped; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public int getDataSize() { return dataSize; }
    public void setDataSize(int dataSize) { this.dataSize = dataSize; }
    public String getNetworkSpeed() { return networkSpeed; }
    public void setNetworkSpeed(String networkSpeed) { this.networkSpeed = networkSpeed; }
    public DeviceInfo getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(DeviceInfo deviceInfo) { this.deviceInfo = deviceInfo; }

    public static SyncHistory createWithId() {
        SyncHistory history = new SyncHistory();
        history.setId(java.util.UUID.randomUUID().toString());
        return history;
    }

    public static class Records {
        public int classes;
        public int instances;
        public int total;
    }
    public static class DeviceInfo {
        public int battery;
        public String storage;
        public String connection;
    }
} 