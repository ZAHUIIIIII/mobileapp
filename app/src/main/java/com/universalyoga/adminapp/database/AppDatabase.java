package com.universalyoga.adminapp.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import com.universalyoga.adminapp.models.Activity;
import com.universalyoga.adminapp.models.SyncHistory;

@Database(entities = {YogaCourse.class, YogaInstance.class, Activity.class, SyncHistory.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract CourseDao courseDao();
    public abstract InstanceDao instanceDao();
    public abstract ActivityDao activityDao();
    public abstract SyncHistoryDao syncHistoryDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "yoga_courses.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 