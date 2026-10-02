package com.winterarc.launcher.services;
import com.winterarc.launcher.R;
import com.winterarc.launcher.activities.MainActivity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class MotivationService extends Service {

    private static final String CHANNEL_ID = "WinterArcChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private android.os.Handler handler;
    private Runnable quoteRunnable;
    
    private final String[] quotes = {
        "\"He who has a why to live for can bear almost any how.\" - Nietzsche",
        "\"The man who loves walking will walk further than the man who loves the destination.\"",
        "\"Discipline equals freedom.\" - Jocko Willink",
        "\"Do not pray for an easy life, pray for the strength to endure a difficult one.\" - Bruce Lee",
        "\"No man has the right to be an amateur in the matter of physical training.\" - Socrates",
        "\"Conquer yourself rather than the world.\" - Descartes",
        "\"We suffer more often in imagination than in reality.\" - Seneca",
        "\"A gem cannot be polished without friction, nor a man perfected without trials.\" - Seneca",
        "\"If you want to conquer the anxiety of life, live in the moment.\" - Marcus Aurelius",
        "\"It is a shame for a man to grow old without seeing the beauty and strength of which his body is capable.\" - Socrates"
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, buildNotification());
        
        if (handler == null) {
            handler = new android.os.Handler(android.os.Looper.getMainLooper());
        } else {
            handler.removeCallbacks(quoteRunnable);
        }
        
        quoteRunnable = new Runnable() {
            @Override
            public void run() {
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.notify(1, buildNotification());
                }
                // Rotate every 15 minutes (900000 ms)
                handler.postDelayed(this, 900000); 
            }
        };
        // Schedule first rotation
        handler.postDelayed(quoteRunnable, 900000);
        
        return START_STICKY;
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        String quote = quotes[new java.util.Random().nextInt(quotes.length)];

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("❄ WINTER ARC ACTIVE")
                .setContentText(quote)
                .setStyle(new Notification.BigTextStyle().bigText(quote))
                .setSmallIcon(R.drawable.ic_snowflake)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && quoteRunnable != null) {
            handler.removeCallbacks(quoteRunnable);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Winter Arc Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
