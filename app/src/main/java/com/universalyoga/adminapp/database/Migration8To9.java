package com.universalyoga.adminapp.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.database.Cursor;

public class Migration8To9 extends Migration {
    public Migration8To9() {
        super(8, 9);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // This migration fixes the schema mismatch for the activity table
        // by recreating it with the correct NOT NULL constraints
        
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
            // Backup existing data
            database.execSQL("CREATE TABLE activity_backup AS SELECT * FROM activity");
            
            // Drop the existing table
            database.execSQL("DROP TABLE activity");
            
            // Create new table with correct schema
            database.execSQL("CREATE TABLE activity (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "relatedId TEXT)");
            
            // Restore data with proper defaults for NULL values
            database.execSQL("INSERT INTO activity (id, type, description, timestamp, relatedId) " +
                    "SELECT " +
                    "COALESCE(id, 'unknown_id') as id, " +
                    "COALESCE(type, 'unknown') as type, " +
                    "COALESCE(description, 'No description') as description, " +
                    "COALESCE(timestamp, datetime('now')) as timestamp, " +
                    "relatedId " +
                    "FROM activity_backup");
            
            // Drop backup table
            database.execSQL("DROP TABLE activity_backup");
        } else {
            // Create new table if it doesn't exist
            database.execSQL("CREATE TABLE activity (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "relatedId TEXT)");
        }
        
        // Ensure index exists
        try {
            database.execSQL("CREATE INDEX IF NOT EXISTS index_activity_timestamp ON activity (timestamp)");
        } catch (Exception e) {
            // Index might already exist, ignore
        }
    }
} 