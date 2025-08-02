package com.universalyoga.adminapp.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.database.Cursor;

public class Migration7To8 extends Migration {
    public Migration7To8() {
        super(7, 8);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Check if activity table exists
        boolean tableExists = false;
        Cursor cursor = null;
        try {
            cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='activity'");
            tableExists = cursor.getCount() > 0;
        } catch (Exception e) {
            // Table doesn't exist
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        if (tableExists) {
            // Table exists but schema might be wrong, recreate it
            try {
                // Create temporary table with correct schema
                database.execSQL("CREATE TABLE activity_new (" +
                        "id TEXT PRIMARY KEY NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "timestamp TEXT NOT NULL, " +
                        "relatedId TEXT)");
                
                // Copy data from old table to new table
                database.execSQL("INSERT INTO activity_new (id, type, description, timestamp, relatedId) " +
                        "SELECT id, COALESCE(type, 'unknown') as type, " +
                        "COALESCE(description, 'No description') as description, " +
                        "COALESCE(timestamp, datetime('now')) as timestamp, " +
                        "relatedId FROM activity");
                
                // Drop old table
                database.execSQL("DROP TABLE activity");
                
                // Rename new table to original name
                database.execSQL("ALTER TABLE activity_new RENAME TO activity");
                
            } catch (Exception e) {
                // If recreation fails, drop and recreate
                database.execSQL("DROP TABLE IF EXISTS activity");
                database.execSQL("CREATE TABLE activity (" +
                        "id TEXT PRIMARY KEY NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "timestamp TEXT NOT NULL, " +
                        "relatedId TEXT)");
            }
        } else {
            // Table doesn't exist, create it with correct schema
            database.execSQL("CREATE TABLE activity (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "relatedId TEXT)");
        }
        
        // Add index if it doesn't exist
        try {
            database.execSQL("CREATE INDEX IF NOT EXISTS index_activity_timestamp ON activity (timestamp)");
        } catch (Exception e) {
            // Index might already exist, ignore
        }
    }
} 