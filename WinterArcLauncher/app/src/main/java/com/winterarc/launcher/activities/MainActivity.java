package com.winterarc.launcher.activities;
import com.winterarc.launcher.R;
import com.winterarc.launcher.services.MotivationService;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.json.JSONException;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.winterarc.launcher.db.DatabaseHelper;

public class MainActivity extends Activity {
    
    private List<String> habits = new ArrayList<>();
    
    private float sleepHours = 8.0f;
    private TextView tvSleepHours;
    private TextView tvCurrentDate;
    private GridLayout habitGrid;
    
    private Calendar currentDate;
    private Calendar startDate;
    private Calendar endDate;
    private Calendar realToday;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    
    private DatabaseHelper dbHelper;
    private JSONObject currentHabitState = new JSONObject();
    private List<TextView> habitButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        dbHelper = new DatabaseHelper(this);
        loadHabitNames();
        
        startDate = Calendar.getInstance();
        startDate.set(2026, Calendar.OCTOBER, 1, 0, 0, 0);
        
        endDate = Calendar.getInstance();
        endDate.set(2026, Calendar.DECEMBER, 31, 23, 59, 59);
        
        realToday = Calendar.getInstance();
        
        currentDate = Calendar.getInstance();
        if (currentDate.before(startDate)) currentDate = (Calendar) startDate.clone();
        if (currentDate.after(endDate)) currentDate = (Calendar) endDate.clone();

        habitGrid = findViewById(R.id.habitGrid);
        tvSleepHours = findViewById(R.id.tvSleepHours);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        
        setupDateControls();
        setupEditGoalsButton();
        
        Button btnGraphType = findViewById(R.id.btnGraphType);
        btnGraphType.setOnClickListener(v -> {
            if (currentGraphType.equals("Sleep")) {
                currentGraphType = "Habits";
                btnGraphType.setText("Habits ▼");
            } else {
                currentGraphType = "Sleep";
                btnGraphType.setText("Sleep ▼");
            }
            setupChart();
        });
        buildHabitGrid();
        loadDataForCurrentDate();
        
        findViewById(R.id.btnMinus).setOnClickListener(v -> {
            if (isFutureDate()) return;
            if (sleepHours > 0) sleepHours -= 0.5f;
            updateSleepUI();
            saveDataForCurrentDate();
        });
        
        findViewById(R.id.btnPlus).setOnClickListener(v -> {
            if (isFutureDate()) return;
            if (sleepHours < 24) sleepHours += 0.5f;
            updateSleepUI();
            saveDataForCurrentDate();
        });

        findViewById(R.id.btnJourney).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, JourneyActivity.class));
        });

        // Request Notification Permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            } else {
                startMotivationService();
            }
        } else {
            startMotivationService();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startMotivationService();
        }
    }
    
    private void startMotivationService() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, MotivationService.class));
            } else {
                startService(new Intent(this, MotivationService.class));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private boolean isFutureDate() {
        // Compare year and day of year
        if (currentDate.get(Calendar.YEAR) > realToday.get(Calendar.YEAR)) return true;
        if (currentDate.get(Calendar.YEAR) == realToday.get(Calendar.YEAR) && 
            currentDate.get(Calendar.DAY_OF_YEAR) > realToday.get(Calendar.DAY_OF_YEAR)) {
            Toast.makeText(this, "You cannot edit the future.", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    
    private void loadHabitNames() {
        SharedPreferences prefs = getSharedPreferences("HabitNames", MODE_PRIVATE);
        String savedHabits = prefs.getString("habits_list", "Workout,10k Steps,3L Water,Read/Learn");
        habits = new ArrayList<>(Arrays.asList(savedHabits.split(",")));
    }
    
    private void setupEditGoalsButton() {
        findViewById(R.id.btnEditGoals).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EditGoalsActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHabitNames();
        buildHabitGrid();
        loadDataForCurrentDate();
    }

    private void setupDateControls() {
        findViewById(R.id.btnPrevDay).setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_YEAR, -1);
            if(currentDate.before(startDate)) currentDate = (Calendar) startDate.clone();
            loadDataForCurrentDate();
        });
        findViewById(R.id.btnNextDay).setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_YEAR, 1);
            if(currentDate.after(endDate)) currentDate = (Calendar) endDate.clone();
            loadDataForCurrentDate();
        });
    }
    
    private void buildHabitGrid() {
        habitGrid.removeAllViews();
        habitButtons.clear();
        for (int i = 0; i < habits.size(); i++) {
            final int index = i;
            TextView btn = new TextView(this);
            btn.setText(habits.get(i));
            btn.setTextColor(Color.WHITE);
            btn.setGravity(Gravity.CENTER);
            btn.setTextSize(12f);
            btn.setPadding(16, 32, 16, 32);
            
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 2, 1f);
            params.rowSpec = GridLayout.spec(i / 2);
            params.setMargins(8, 8, 8, 8);
            btn.setLayoutParams(params);
            
            btn.setOnClickListener(v -> {
                if (isFutureDate()) return;
                
                boolean isActive = btn.getTag() != null && (boolean) btn.getTag();
                setHabitButtonState(btn, !isActive);
                try {
                    currentHabitState.put(habits.get(index), !isActive);
                    saveDataForCurrentDate();
                    
                    if (!isActive) {
                        String[] congrats = {
                            "Outstanding work! Keep the momentum going.",
                            "One step closer to greatness.",
                            "Discipline equals freedom. Well done.",
                            "Another victory for the Winter Arc.",
                            "Proud of you. Keep pushing."
                        };
                        String msg = congrats[new java.util.Random().nextInt(congrats.length)];
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            });

            habitButtons.add(btn);
            habitGrid.addView(btn);
        }
    }
    
    private void setHabitButtonState(TextView btn, boolean isActive) {
        btn.setTag(isActive);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(24f);
        if (isActive) {
            drawable.setColor(Color.WHITE);
            btn.setTextColor(Color.BLACK);
        } else {
            drawable.setColor(Color.parseColor("#121212"));
            drawable.setStroke(2, Color.parseColor("#33FFFFFF"));
            btn.setTextColor(Color.WHITE);
        }
        btn.setBackground(drawable);
    }

    private void loadDataForCurrentDate() {
        tvCurrentDate.setText(dateFormat.format(currentDate.getTime()));
        String dbDate = dbDateFormat.format(currentDate.getTime());
        
        Cursor cursor = dbHelper.getRecord(dbDate);
        if (cursor != null && cursor.moveToFirst()) {
            String json = cursor.getString(cursor.getColumnIndexOrThrow("habits_json"));
            sleepHours = cursor.getFloat(cursor.getColumnIndexOrThrow("sleep_hours"));
            try {
                currentHabitState = new JSONObject(json);
            } catch (Exception e) { currentHabitState = new JSONObject(); }
            cursor.close();
        } else {
            currentHabitState = new JSONObject();
            sleepHours = 8.0f;
        }
        
        updateSleepUI();
        
        // Update habit UI
        for (int i = 0; i < habits.size(); i++) {
            boolean isDone = currentHabitState.optBoolean(habits.get(i), false);
            setHabitButtonState(habitButtons.get(i), isDone);
        }
        
        setupChart();
    }
    
    private void updateSleepUI() {
        tvSleepHours.setText(String.valueOf(sleepHours));
        if(isFutureDate()) {
            tvSleepHours.setTextColor(Color.parseColor("#555555"));
        } else {
            tvSleepHours.setTextColor(Color.WHITE);
        }
    }
    
    private void saveDataForCurrentDate() {
        if(isFutureDate()) return;
        String dbDate = dbDateFormat.format(currentDate.getTime());
        dbHelper.saveRecord(dbDate, currentHabitState.toString(), sleepHours);
        setupChart();
    }

    private String currentGraphType = "Sleep";

    private void setupChart() {
        com.github.mikephil.charting.charts.LineChart chart = findViewById(R.id.progressChart);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        
        // Match the black background of the image
        chart.setBackgroundColor(Color.parseColor("#0F0F13"));

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);
        xAxis.setTextColor(Color.parseColor("#888888"));
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setAxisLineWidth(2f);
        
        final String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < 7) return daysOfWeek[index];
                return "";
            }
        });
        
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setDrawLabels(false);
        yAxis.setAxisLineColor(Color.WHITE);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);

        List<com.github.mikephil.charting.data.Entry> entries = new ArrayList<>();
        List<com.github.mikephil.charting.data.Entry> expectedEntries = new ArrayList<>();
        
        java.util.Calendar weekCal = (java.util.Calendar) currentDate.clone();
        weekCal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY);
        
        float target = currentGraphType.equals("Sleep") ? 8.0f : habits.size();
        
        for (int i = 0; i < 7; i++) {
            String dbDateStr = dbDateFormat.format(weekCal.getTime());
            
            boolean isFuture = weekCal.after(realToday) && 
                (weekCal.get(java.util.Calendar.DAY_OF_YEAR) != realToday.get(java.util.Calendar.DAY_OF_YEAR) || 
                 weekCal.get(java.util.Calendar.YEAR) != realToday.get(java.util.Calendar.YEAR));
                 
            if (!isFuture) {
                Cursor c = dbHelper.getRecord(dbDateStr);
                boolean hasData = false;
                float val = 0;
                
                if (c != null && c.moveToFirst()) {
                    hasData = true;
                    if (currentGraphType.equals("Sleep")) {
                        val = c.getFloat(c.getColumnIndexOrThrow("sleep_hours"));
                    } else {
                        try {
                            String habitsJson = c.getString(c.getColumnIndexOrThrow("habits_json"));
                            org.json.JSONObject obj = new org.json.JSONObject(habitsJson);
                            int count = 0;
                            java.util.Iterator<String> keys = obj.keys();
                            while(keys.hasNext()) {
                                if (obj.getBoolean(keys.next())) count++;
                            }
                            val = count;
                        } catch (Exception e) {}
                    }
                }
                if (c != null) c.close();
                
                boolean isToday = weekCal.get(java.util.Calendar.DAY_OF_YEAR) == currentDate.get(java.util.Calendar.DAY_OF_YEAR);
                
                if (!hasData && isToday) {
                    hasData = true;
                    val = currentGraphType.equals("Sleep") ? sleepHours : getCompletedHabitsCount();
                }
                
                if (hasData) {
                    entries.add(new com.github.mikephil.charting.data.Entry(i, val));
                }
            }
            weekCal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }
        
        expectedEntries.add(new com.github.mikephil.charting.data.Entry(0, target));
        expectedEntries.add(new com.github.mikephil.charting.data.Entry(6, target));

        com.github.mikephil.charting.data.LineDataSet expectedSet = new com.github.mikephil.charting.data.LineDataSet(expectedEntries, "Expected");
        expectedSet.setColor(Color.parseColor("#888888"));
        expectedSet.setLineWidth(2f);
        expectedSet.enableDashedLine(15f, 10f, 0f);
        expectedSet.setDrawCircles(false);
        expectedSet.setDrawFilled(false);

        com.github.mikephil.charting.data.LineDataSet actualSet = new com.github.mikephil.charting.data.LineDataSet(entries, "Actual");
        actualSet.setColor(Color.WHITE);
        actualSet.setLineWidth(3f);
        actualSet.setDrawCircles(false); 
        actualSet.setDrawFilled(true); 
        actualSet.setFillColor(Color.parseColor("#33FFFFFF"));
        actualSet.setMode(com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER);
        
        com.github.mikephil.charting.data.LineData lineData = new com.github.mikephil.charting.data.LineData(expectedSet, actualSet);
        chart.setData(lineData);
        chart.animateX(1500, com.github.mikephil.charting.animation.Easing.EaseInOutSine);
        chart.invalidate();
    }
    
    private int getCompletedHabitsCount() {
        int count = 0;
        try {
            java.util.Iterator<String> keys = currentHabitState.keys();
            while(keys.hasNext()) {
                if (currentHabitState.getBoolean(keys.next())) count++;
            }
        } catch(Exception e) {}
        return count;
    }
}
