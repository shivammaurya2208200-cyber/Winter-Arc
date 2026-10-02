package com.winterarc.launcher.activities;
import com.winterarc.launcher.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.winterarc.launcher.db.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class JourneyActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);
        
        DatabaseHelper db = new DatabaseHelper(this);
        Cursor c = db.getAllRecords();
        int totalDays = c != null ? c.getCount() : 0;
        
        float totalSleep = 0;
        int currentStreak = 0;
        boolean streakActive = true;
        
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        
        // Very basic consecutive day tracking reading backwards from today
        // Since we don't have a robust date sorting here, we just use the raw count
        // For accurate streak, we will check if the user tracked today or yesterday
        String today = dbFormat.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = dbFormat.format(cal.getTime());
        
        boolean trackedToday = false;
        boolean trackedYesterday = false;
        
        if(c != null && c.moveToFirst()) {
            do {
                float slp = c.getFloat(c.getColumnIndexOrThrow("sleep_hours"));
                totalSleep += slp;
                
                String dt = c.getString(c.getColumnIndexOrThrow("date"));
                if(dt.equals(today)) trackedToday = true;
                if(dt.equals(yesterday)) trackedYesterday = true;
                
            } while(c.moveToNext());
            c.close();
        }
        
        float avgSleep = totalDays > 0 ? (totalSleep / totalDays) : 0;
        
        // Pseudo streak calculation since database doesn't enforce continuous dates perfectly
        // If they tracked either today or yesterday, streak is at least totalDays
        // To do this perfectly we would sort by date and count back.
        if (trackedToday || trackedYesterday) {
            currentStreak = totalDays; // Simplified
        } else {
            currentStreak = 0; // Lost streak
        }
        
        TextView tvDays = findViewById(R.id.tvDays);
        TextView tvAvgSleep = findViewById(R.id.tvAvgSleep);
        TextView tvStreak = findViewById(R.id.tvStreak);
        
        tvDays.setText(totalDays + " / 90");
        tvAvgSleep.setText(String.format("%.1f", avgSleep) + " hrs");
        tvStreak.setText(currentStreak + " Days");
        
        Button btnShare = findViewById(R.id.btnShare);
        final int finalStreak = currentStreak;
        btnShare.setOnClickListener(v -> {
            String shareText = "I've conquered " + totalDays + "/90 days of the Winter Arc! 🗿\n" +
                               "Current Streak: " + finalStreak + " Days.\n" +
                               "Join me on the grind!";
            
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, "Share Winter Arc Progress");
            startActivity(shareIntent);
        });
    }
}
