package com.universalyoga.adminapp.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration9To10 extends Migration {
    public Migration9To10() {
        super(9, 10);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Remove unnecessary sync-related tables
        database.execSQL("DROP TABLE IF EXISTS sync_queue");
        database.execSQL("DROP TABLE IF EXISTS sync_error");
        database.execSQL("DROP TABLE IF EXISTS sync_stats");
    }
} 