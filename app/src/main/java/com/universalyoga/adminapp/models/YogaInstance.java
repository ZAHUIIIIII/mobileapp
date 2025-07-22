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

    public YogaInstance(int id, int courseId, String date, String teacher, String comments, int syncStatus) {
        this.id = id;
        this.courseId = courseId;
        this.date = date;
        this.teacher = teacher;
        this.comments = comments;
        this.syncStatus = syncStatus;
    }
    @Ignore
    public YogaInstance(int courseId, String date, String teacher, String comments) {
        this(0, courseId, date, teacher, comments, 0);
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
} 