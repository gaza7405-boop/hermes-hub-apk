package com.hermes.hub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class ConnectionKeepAliveService extends Service {
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HermesHub::KeepAliveLock");
        wakeLock.acquire();
        Log.d("HermesKeepAlive", "WakeLock acquired to keep network alive.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // الاستمرار في العمل في الخلفية دائماً
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
