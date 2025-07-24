package com.universalyoga.adminapp.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration2To3 extends Migration {
    public Migration2To3() {
        super(2, 3);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Add new columns to the 'instances' table
        database.execSQL("ALTER TABLE instances ADD COLUMN startTime TEXT");
        database.execSQL("ALTER TABLE instances ADD COLUMN endTime TEXT");
        database.execSQL("ALTER TABLE instances ADD COLUMN enrolled INTEGER NOT NULL DEFAULT 0");
        database.execSQL("ALTER TABLE instances ADD COLUMN capacity INTEGER NOT NULL DEFAULT 0");
    }
}
