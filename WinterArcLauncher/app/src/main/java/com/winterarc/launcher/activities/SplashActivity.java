package com.winterarc.launcher.activities;
import com.winterarc.launcher.R;
import com.winterarc.launcher.services.MotivationService;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Random;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvFlake = findViewById(R.id.tvFlake);
        TextView tvTitle = findViewById(R.id.tvTitle);
        ConstraintLayout root = findViewById(R.id.splashRoot);

        // Big Snowflake Animation
        RotateAnimation rotate = new RotateAnimation(0, 360, 
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(3000);

        ScaleAnimation scale = new ScaleAnimation(0.8f, 1.1f, 0.8f, 1.1f, 
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(2000);
        
        AlphaAnimation alphaTitle = new AlphaAnimation(0.0f, 1.0f);
        alphaTitle.setDuration(2000);

        AnimationSet bigFlakeSet = new AnimationSet(true);
        bigFlakeSet.addAnimation(rotate);
        bigFlakeSet.addAnimation(scale);
        
        tvFlake.startAnimation(bigFlakeSet);
        tvTitle.startAnimation(alphaTitle);

        // Antigravity style particle effect
        Random random = new Random();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        
        String[] particles = {"/", "\\", "-", "•", "·"};
        int[] colors = {Color.parseColor("#00D2D3"), Color.parseColor("#6C5CE7"), Color.WHITE, Color.parseColor("#444444")};

        for (int i = 0; i < 60; i++) {
            TextView particle = new TextView(this);
            particle.setText(particles[random.nextInt(particles.length)]);
            particle.setTextColor(colors[random.nextInt(colors.length)]);
            particle.setTextSize(random.nextInt(12) + 8);
            particle.setAlpha(random.nextFloat() * 0.8f + 0.2f);
            
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            
            // Random start position scattered across the whole screen
            int startX = random.nextInt(screenWidth);
            int startY = random.nextInt(screenHeight);
            particle.setX(startX);
            particle.setY(startY);
            
            root.addView(particle, params);

            // Subtle drift animation simulating the floating antigravity particles
            int driftX = random.nextInt(200) - 100; // Drift left or right
            int driftY = random.nextInt(200) - 100; // Drift up or down
            
            TranslateAnimation drift = new TranslateAnimation(
                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, driftX, 
                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, driftY);
            
            drift.setDuration(random.nextInt(4000) + 3000);
            drift.setInterpolator(new LinearInterpolator());
            drift.setRepeatMode(Animation.REVERSE);
            drift.setRepeatCount(Animation.INFINITE);
            
            // Subtle rotation for lines
            RotateAnimation pRotate = new RotateAnimation(0, random.nextInt(180) - 90, 
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            pRotate.setDuration(random.nextInt(4000) + 3000);
            pRotate.setRepeatMode(Animation.REVERSE);
            pRotate.setRepeatCount(Animation.INFINITE);
            
            AnimationSet pSet = new AnimationSet(true);
            pSet.addAnimation(drift);
            pSet.addAnimation(pRotate);
            pSet.setStartOffset(random.nextInt(2000));
            
            particle.startAnimation(pSet);
        }

        android.content.SharedPreferences prefs = getSharedPreferences("WinterArcPrefs", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("isFirstTime", true);

        TextView tvIntro = findViewById(R.id.tvIntro);
        android.widget.Button btnAccept = findViewById(R.id.btnAccept);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFirstTime) {
                // Show onboarding
                tvIntro.setVisibility(android.view.View.VISIBLE);
                btnAccept.setVisibility(android.view.View.VISIBLE);
                
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(1000);
                tvIntro.startAnimation(fadeIn);
                btnAccept.startAnimation(fadeIn);

                btnAccept.setOnClickListener(v -> {
                    // Play Sound
                    android.media.MediaPlayer mp = android.media.MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
                    if (mp != null) {
                        mp.start();
                        mp.setOnCompletionListener(android.media.MediaPlayer::release);
                    }

                    // Save state
                    prefs.edit().putBoolean("isFirstTime", false).apply();
                    
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                });
            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 4000);
    }
}
