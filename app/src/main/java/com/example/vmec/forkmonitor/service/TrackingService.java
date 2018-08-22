package com.example.vmec.forkmonitor.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.example.vmec.forkmonitor.ForkMonitorTrackingManager;
import com.example.vmec.forkmonitor.helper.LocationHelper;
import com.example.vmec.forkmonitor.helper.NotificationHelper;
import com.example.vmec.forkmonitor.helper.WakeLockHelper;

import timber.log.Timber;

/**
 * Created by Stofanak on 14/08/2018.
 */
public class TrackingService extends Service {

    private LocationHelper mLocationManager;
    private NotificationHelper mNotificationManager;
    private ForkMonitorTrackingManager mTrackingManager;

    @Nullable @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override public void onCreate() {
        super.onCreate();

        mNotificationManager = new NotificationHelper();

        Timber.d("Tracking manager started.");

        aquireWakelock();

        startForegroundService();

        mLocationManager = new LocationHelper();
        //TODO: Temporarily disabled
//        mLocationManager.startTrackingLocation(this);
        mTrackingManager = new ForkMonitorTrackingManager();
        mTrackingManager.initialize(this);
        mTrackingManager.startTracking(this);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        Timber.d("Tracking manager destroyed.");
        mLocationManager.stopTrackingLocation();
        mTrackingManager.stopTracking();
        releaseWakelock();
    }

    private void startForegroundService() {
        final Notification notification = mNotificationManager.getNotification(this);
        startForeground(NotificationHelper.NOTIFICATION_ID, notification);
    }

    private void aquireWakelock() {
        final PowerManager.WakeLock wakeLock = WakeLockHelper.getLock(this);
        wakeLock.acquire();
    }

    private void releaseWakelock() {
        final PowerManager.WakeLock wakeLock = WakeLockHelper.getLock(this);
        wakeLock.release();
    }
}
