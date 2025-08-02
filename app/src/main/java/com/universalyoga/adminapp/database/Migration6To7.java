package com.universalyoga.adminapp.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.database.Cursor;

public class Migration6To7 extends Migration {
    public Migration6To7() {
        super(6, 7);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Check if activity table already exists
        boolean tableExists = false;
        Cursor cursor = null;
        try {
            cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='activity'");
            tableExists = cursor.getCount() > 0;
        } catch (Exception e) {
            // Table doesn't exist, we can create it
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        // Only create the activity table if it doesn't exist
        if (!tableExists) {
            database.execSQL("CREATE TABLE activity (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "relatedId TEXT)");
        }
    }
} 