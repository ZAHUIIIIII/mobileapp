package com.universalyoga.adminapp.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration5To6 extends Migration {
    public Migration5To6() {
        super(5, 6);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Create a new table with the updated schema (without courseName column)
        database.execSQL("CREATE TABLE courses_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "daysOfWeek TEXT, " +
                "time TEXT, " +
                "capacity INTEGER NOT NULL, " +
                "duration INTEGER NOT NULL, " +
                "price REAL NOT NULL, " +
                "type TEXT, " +
                "description TEXT, " +
                "roomLocation TEXT, " +
                "instructor TEXT, " +
                "difficulty TEXT, " +
                "syncStatus INTEGER NOT NULL DEFAULT 0)");

        // Copy data from old table to new table, using type as the course name
        database.execSQL("INSERT INTO courses_new (id, daysOfWeek, time, capacity, duration, price, type, description, roomLocation, instructor, difficulty, syncStatus) " +
                "SELECT id, daysOfWeek, time, capacity, duration, price, type, description, roomLocation, instructor, difficulty, syncStatus FROM courses");

        // Drop the old table
        database.execSQL("DROP TABLE courses");

        // Rename the new table to the original name
        database.execSQL("ALTER TABLE courses_new RENAME TO courses");
    }
} 