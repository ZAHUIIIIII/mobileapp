package com.universalyoga.adminapp.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;

@Database(entities = {YogaCourse.class, YogaInstance.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract CourseDao courseDao();
    public abstract InstanceDao instanceDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "yoga_courses.db")
                            .fallbackToDestructiveMigration()
                            .addMigrations(new Migration2To3())
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 