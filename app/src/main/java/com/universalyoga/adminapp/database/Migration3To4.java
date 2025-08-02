package com.universalyoga.adminapp.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration3To4 extends Migration {
    public Migration3To4() {
        super(3, 4);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Add instructor column
        database.execSQL("ALTER TABLE courses ADD COLUMN instructor TEXT");
        
        // Add difficulty column
        database.execSQL("ALTER TABLE courses ADD COLUMN difficulty TEXT");
    }
} 