package com.universalyoga.adminapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(tableName = "instances",
        foreignKeys = @ForeignKey(entity = YogaCourse.class,
                parentColumns = "id",
                childColumns = "courseId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("courseId")})
public class YogaInstance {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "courseId")
    private int courseId;
    @ColumnInfo(name = "date")
    private String date;
    @ColumnInfo(name = "teacher")
    private String teacher;
    @ColumnInfo(name = "comments")
    private String comments;
    @ColumnInfo(name = "syncStatus")
    private int syncStatus; // 0 = not synced, 1 = synced, 2 = pending delete
    @ColumnInfo(name = "startTime")
    private String startTime;
    @ColumnInfo(name = "endTime")
    private String endTime;
    @ColumnInfo(name = "enrolled")
    private int enrolled;
    @ColumnInfo(name = "capacity")
    private int capacity;

    public YogaInstance() {
        // Required for Firebase
    }

    @Ignore
    public YogaInstance(int id, int courseId, String date, String teacher, String comments, int syncStatus, String startTime, String endTime, int enrolled, int capacity) {
        this.id = id;
        this.courseId = courseId;
        this.date = date;
        this.teacher = teacher;
        this.comments = comments;
        this.syncStatus = syncStatus;
        this.startTime = startTime;
        this.endTime = endTime;
        this.enrolled = enrolled;
        this.capacity = capacity;
    }
    @Ignore
    public YogaInstance(int courseId, String date, String teacher, String comments) {
        this(0, courseId, date, teacher, comments, 0, "", "", 0, 0);
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public int getSyncStatus() { return syncStatus; }
    public void setSyncStatus(int syncStatus) { this.syncStatus = syncStatus; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public int getEnrolled() { return enrolled; }
    public void setEnrolled(int enrolled) { this.enrolled = enrolled; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
} 