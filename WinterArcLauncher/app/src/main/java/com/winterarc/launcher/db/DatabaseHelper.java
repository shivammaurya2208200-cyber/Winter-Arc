package com.winterarc.launcher.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "WinterArcV2.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE daily_records (date TEXT PRIMARY KEY, habits_json TEXT, sleep_hours REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS daily_records");
        onCreate(db);
    }

    public void saveRecord(String date, String habitsJson, float sleepHours) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("habits_json", habitsJson);
        values.put("sleep_hours", sleepHours);
        
        db.insertWithOnConflict("daily_records", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
    
    public Cursor getRecord(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM daily_records WHERE date = ?", new String[]{date});
    }
    
    public Cursor getAllRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM daily_records ORDER BY date ASC", null);
    }
}
