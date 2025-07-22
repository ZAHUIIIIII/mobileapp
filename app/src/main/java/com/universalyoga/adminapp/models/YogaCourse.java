package com.universalyoga.adminapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

@Entity(tableName = "courses")
public class YogaCourse {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "courseName")
    private String courseName;
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
    @ColumnInfo(name = "syncStatus")
    private int syncStatus; // 0 = not synced, 1 = synced, 2 = pending delete

    public YogaCourse(int id, String courseName, String daysOfWeek, String time, int capacity, int duration, double price, String type, String description, String roomLocation, int syncStatus) {
        this.id = id;
        this.courseName = courseName;
        this.daysOfWeek = daysOfWeek;
        this.time = time;
        this.capacity = capacity;
        this.duration = duration;
        this.price = price;
        this.type = type;
        this.description = description;
        this.roomLocation = roomLocation;
        this.syncStatus = syncStatus;
    }
    @Ignore
    public YogaCourse(String courseName, String daysOfWeek, String time, int capacity, int duration, double price, String type, String description, String roomLocation) {
        this(0, courseName, daysOfWeek, time, capacity, duration, price, type, description, roomLocation, 0);
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    /**
     * @deprecated Use getDaysOfWeek instead
     */
    @Deprecated
    public String getDate() { return daysOfWeek; }
    /**
     * @deprecated Use setDaysOfWeek instead
     */
    @Deprecated
    public void setDate(String date) { this.daysOfWeek = date; }
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
    public int getSyncStatus() { return syncStatus; }
    public void setSyncStatus(int syncStatus) { this.syncStatus = syncStatus; }
} 