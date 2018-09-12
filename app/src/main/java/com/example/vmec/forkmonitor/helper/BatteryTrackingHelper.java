package com.example.vmec.forkmonitor.helper;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationManagerCompat;
import android.text.SpannableStringBuilder;

import com.example.vmec.forkmonitor.Constants;
import com.example.vmec.forkmonitor.R;
import com.example.vmec.forkmonitor.utils.PowerUtils;

/**
 * Created by Stofanak on 10/09/2018.
 */
public class BatteryTrackingHelper {

    private int mBatteryLevel = -1;
    private NotificationManagerCompat mNotificationManager;
    private NotificationHelper mNotificationHelper;

    public BatteryTrackingHelper(final Context context) {
        mNotificationManager = NotificationManagerCompat.from(context);
        mNotificationHelper = new NotificationHelper();
    }

    private BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int rawlevel = intent.getIntExtra("level", -1);
            int scale = intent.getIntExtra("scale", -1);
            int level = -1;
            if (rawlevel >= 0 && scale > 0) {
                level = (rawlevel * 100) / scale;
            }
            mBatteryLevel = level;

            if(PowerUtils.isConnected(ctxt)) {
                if (mBatteryLevel > Constants.PHONE_FULL_BATTERY_LEVEL_VALUE) {
                    final String notificationTitle = ctxt.getString(R.string.notification_info_title_battery_level_full);
                    final SpannableStringBuilder description = new SpannableStringBuilder(ctxt.getString(R.string.notification_info_desc_battery_level_full));
                    final Notification notification = mNotificationHelper.getInfoNotification(ctxt, notificationTitle, description, NotificationHelper.NOTIFICATION_INFO_TYPE_ERROR);
                    mNotificationManager.notify(Constants.NOTIFICATION_ID_PHONE_BATTERY, notification);
                } else {
                    mNotificationManager.cancel(Constants.NOTIFICATION_ID_PHONE_BATTERY);
                }
            } else {
                if (mBatteryLevel <= Constants.PHONE_LOW_BATTERY_LEVEL_VALUE) {
                    final String notificationTitle = ctxt.getString(R.string.notification_info_title_battery_level_low);
                    final SpannableStringBuilder description = new SpannableStringBuilder(ctxt.getString(R.string.notification_info_desc_battery_level_low));
                    final Notification notification = mNotificationHelper.getInfoNotification(ctxt, notificationTitle, description, NotificationHelper.NOTIFICATION_INFO_TYPE_ERROR);
                    mNotificationManager.notify(Constants.NOTIFICATION_ID_PHONE_BATTERY, notification);
                } else {
                    mNotificationManager.cancel(Constants.NOTIFICATION_ID_PHONE_BATTERY);
                }
            }
        }
    };

    public void startTracking(final Context context) {
        context.registerReceiver(this.batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public void stopTracking(final Context context) {
        context.unregisterReceiver(this.batteryLevelReceiver);
    }

    public int getLastBatteryLevel() {
        return mBatteryLevel;
    }
}
