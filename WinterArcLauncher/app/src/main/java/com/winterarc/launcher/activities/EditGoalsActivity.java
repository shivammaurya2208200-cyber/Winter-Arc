package com.winterarc.launcher.activities;
import com.winterarc.launcher.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditGoalsActivity extends Activity {

    private List<String> habits;
    private LinearLayout goalList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_goals);

        SharedPreferences prefs = getSharedPreferences("HabitNames", MODE_PRIVATE);
        String savedHabits = prefs.getString("habits_list", "Workout,10k Steps,3L Water,Read/Learn");
        habits = new ArrayList<>(Arrays.asList(savedHabits.split(",")));

        goalList = findViewById(R.id.goalList);
        renderList();

        EditText etNewGoal = findViewById(R.id.etNewGoal);
        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String ng = etNewGoal.getText().toString().trim();
            if (!ng.isEmpty()) {
                habits.add(0, ng);
                etNewGoal.setText("");
                renderList();
            }
        });
        
        // Recommended Goals Logic
        LinearLayout recContainer = findViewById(R.id.recommendedContainer);
        String[] recommendations = {"No Fap", "Gym", "Read 10 Pages", "Cold Shower", "No Sugar", "Meditate", "Gallon of Water"};
        
        for (String rec : recommendations) {
            Button btnRec = new Button(this);
            btnRec.setText("+" + rec);
            btnRec.setTextColor(Color.WHITE);
            btnRec.setTextSize(12f);
            
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setColor(Color.parseColor("#222222"));
            gd.setCornerRadius(30f);
            btnRec.setBackground(gd);
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 100);
            lp.setMargins(0, 0, 16, 0);
            btnRec.setLayoutParams(lp);
            
            btnRec.setOnClickListener(v -> {
                if (!habits.contains(rec)) {
                    habits.add(0, rec);
                    renderList();
                    Toast.makeText(this, "Added: " + rec, Toast.LENGTH_SHORT).show();
                }
            });
            recContainer.addView(btnRec);
        }

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            List<String> validHabits = new ArrayList<>();
            for (String h : habits) {
                if (!h.trim().isEmpty()) {
                    validHabits.add(h.trim());
                }
            }
            if (validHabits.size() < 4) {
                Toast.makeText(this, "You must have at least 4 non-empty goals.", Toast.LENGTH_LONG).show();
                return;
            }
            StringBuilder saveStr = new StringBuilder();
            for(int i = 0; i < validHabits.size(); i++){
                saveStr.append(validHabits.get(i));
                if(i < validHabits.size() - 1) saveStr.append(",");
            }
            prefs.edit().putString("habits_list", saveStr.toString()).apply();
            finish();
        });
    }

    private void renderList() {
        goalList.removeAllViews();
        for (int i = 0; i < habits.size(); i++) {
            final int index = i;
            String goal = habits.get(i);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 8, 0, 8);
            row.setGravity(Gravity.CENTER_VERTICAL);

            EditText et = new EditText(this);
            et.setText(goal);
            et.setTextColor(Color.WHITE);
            et.setTextSize(16f);
            et.setPadding(32, 32, 32, 32);
            et.setBackgroundResource(R.drawable.glass_card);
            et.setMaxLines(1);
            et.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
            
            et.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    habits.set(index, s.toString().trim());
                }
                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
            
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            et.setLayoutParams(tp);

            Button btnRemove = new Button(this);
            btnRemove.setText("X");
            btnRemove.setTextColor(Color.WHITE);
            btnRemove.setBackgroundColor(Color.TRANSPARENT);
            btnRemove.setOnClickListener(v -> {
                if (habits.size() <= 4) {
                    Toast.makeText(this, "Minimum 4 goals required.", Toast.LENGTH_SHORT).show();
                    return;
                }
                habits.remove(index);
                renderList();
            });

            row.addView(et);
            row.addView(btnRemove);
            goalList.addView(row);
        }
    }
}
