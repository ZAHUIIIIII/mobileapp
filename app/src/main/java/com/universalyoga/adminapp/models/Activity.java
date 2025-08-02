package com.universalyoga.adminapp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(tableName = "activity", indices = {@Index(value = {"timestamp"})})
public class Activity {
    @PrimaryKey
    @NonNull
    private String id;
    
    @NonNull
    private String type;
    
    @NonNull
    private String description;
    
    @NonNull
    private String timestamp;
    
    private String relatedId; // optional

    public Activity() {}

    @Ignore
    public Activity(String id, String type, String description, String timestamp, String relatedId) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
        this.relatedId = relatedId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
} 