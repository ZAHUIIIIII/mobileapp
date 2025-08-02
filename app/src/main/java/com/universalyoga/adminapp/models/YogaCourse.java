package com.universalyoga.adminapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

@Entity(tableName = "courses")
public class YogaCourse {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "daysOfWeek")
    private String daysOfWeek; // Comma-separated days, e.g. "Mon,Wed,Fri"
    @ColumnInfo(name = "time")
    private String time;
    @ColumnInfo(name = "capacity")
    private int capacity;
    @ColumnInfo(name = "duration")
    private int duration;
    @ColumnInfo(name = "price")
    private double price;
    @ColumnInfo(name = "type")
    private String type;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "roomLocation")
    private String roomLocation;
    @ColumnInfo(name = "instructor")
    private String instructor;
    @ColumnInfo(name = "difficulty")
    private String difficulty;
    @ColumnInfo(name = "syncStatus")
    private int syncStatus; // 0 = not synced, 1 = synced, 2 = pending delete

    public YogaCourse() {
        // Required for Room
    }

    @Ignore
    public YogaCourse(int id, String daysOfWeek, String time, int capacity, int duration, double price, String type, String description, String roomLocation, String instructor, String difficulty, int syncStatus) {
        this.id = id;
        this.daysOfWeek = daysOfWeek;
        this.time = time;
        this.capacity = capacity;
        this.duration = duration;
        this.price = price;
        this.type = type;
        this.description = description;
        this.roomLocation = roomLocation;
        this.instructor = instructor;
        this.difficulty = difficulty;
        this.syncStatus = syncStatus;
    }
    
    @Ignore
    public YogaCourse(String daysOfWeek, String time, int capacity, int duration, double price, String type, String description, String roomLocation, String instructor, String difficulty) {
        this(0, daysOfWeek, time, capacity, duration, price, type, description, roomLocation, instructor, difficulty, 0);
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getCourseName() { return type; }
    public void setCourseName(String courseName) { this.type = courseName; }
    
    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getRoomLocation() { return roomLocation; }
    public void setRoomLocation(String roomLocation) { this.roomLocation = roomLocation; }
    
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public int getSyncStatus() { return syncStatus; }
    public void setSyncStatus(int syncStatus) { this.syncStatus = syncStatus; }

    @Override
    public String toString() {
        return type;
    }
} 