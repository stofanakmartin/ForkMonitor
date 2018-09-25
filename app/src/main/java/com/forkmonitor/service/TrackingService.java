package com.forkmonitor.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.text.SpannableStringBuilder;

import com.forkmonitor.Constants;
import com.forkmonitor.R;
import com.forkmonitor.TrackingManager;
import com.forkmonitor.event.ArduinoBatteryChangeEvent;
import com.forkmonitor.helper.NotificationHelper;
import com.forkmonitor.helper.WakeLockHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;

/**
 * Created by Stofanak on 14/08/2018.
 */
public class TrackingService extends Service {

    private NotificationManagerCompat mNotificationManager;
    private NotificationHelper mNotificationHelper;
    private TrackingManager mTrackingManager;

    @Nullable @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);

        mNotificationManager = NotificationManagerCompat.from(this);
        mNotificationHelper = new NotificationHelper();

        Timber.d("Tracking manager started.");

        aquireWakelock();

        startForegroundService();

        mTrackingManager = new TrackingManager(this);
        mTrackingManager.startTracking(this);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Timber.d("Tracking manager destroyed.");
        mTrackingManager.stopTracking(this);
        releaseWakelock();
    }

    private void startForegroundService() {
        final Notification notification = mNotificationHelper.getServiceNotification(this);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ArduinoBatteryChangeEvent event) {
        if(event.isLow()) {
            final String notificationTitle = getString(R.string.notification_info_title_arduino_battery_level_low);
            final SpannableStringBuilder description = new SpannableStringBuilder(getString(R.string.notification_info_desc_arduino_battery_level_low));
            final Notification notification = mNotificationHelper.getInfoNotification(this, notificationTitle, description, NotificationHelper.NOTIFICATION_INFO_TYPE_ERROR);
            mNotificationManager.notify(Constants.NOTIFICATION_ID_ARDUINO_LOW_BATTERY, notification);
        } else {
            mNotificationManager.cancel(Constants.NOTIFICATION_ID_ARDUINO_LOW_BATTERY);
        }
    }
}
