package com.forkmonitor.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.SpannableStringBuilder;

import com.forkmonitor.R;
import com.forkmonitor.activity.MainActivity1;
import com.forkmonitor.utils.DeviceUtils;

/**
 * Created by Stofanak on 19/08/2018.
 */
public class NotificationHelper {
    public static final int NOTIFICATION_ID = 1338;
    public static final int NOTIFICATION_INFO_TYPE_SUCCESS = 1;
    public static final int NOTIFICATION_INFO_TYPE_ERROR = 2;

    public NotificationHelper() {
    }

    public Notification getServiceNotification(final Context context) {
        String title = getNotificationTitle(context);
        final SpannableStringBuilder content = getNotificationDescription(context);
        return getServiceNotification(context, title, content);
    }

    private String getNotificationTitle(final Context context) {
        return context.getString(R.string.tracking_service_notification_title);
    }

    private SpannableStringBuilder getNotificationDescription(final Context context) {
        return new SpannableStringBuilder("*TODO DESCRIPTION*");
    }

    private Notification getServiceNotification(final Context context, final String title, final SpannableStringBuilder content) {
        Intent openAppIntent = new Intent(context, MainActivity1.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openAppPending = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(openAppPending)
                .setWhen(0);

        if(DeviceUtils.isMinimumApiVersion(Build.VERSION_CODES.O)) {
            final String notificationChannelId = createServiceNotificationChannel(context);
            builder.setChannelId(notificationChannelId);
        }

        if (DeviceUtils.isMinimumApiVersion(Build.VERSION_CODES.LOLLIPOP)) {
            builder.setVisibility(Notification.VISIBILITY_SECRET);
            builder.setPriority(Notification.PRIORITY_MIN);
        }

        if (DeviceUtils.isMinimumApiVersion(Build.VERSION_CODES.JELLY_BEAN)) {
            return builder.build();
        }

        return builder.getNotification();
    }

    public Notification getInfoNotification(final Context context, final String title, final SpannableStringBuilder content, final int notificationInfoType) {
        Intent openAppIntent = new Intent(context, MainActivity1.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openAppPending = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_info_notification_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(openAppPending)
                .setAutoCancel(true)
                .setWhen(0);

        if(DeviceUtils.isMinimumApiVersion(Build.VERSION_CODES.O)) {
            final String notificationChannelId = createNotificationInfoChannel(context, notificationInfoType);
            builder.setChannelId(notificationChannelId);
        } else {
            if(notificationInfoType == NOTIFICATION_INFO_TYPE_SUCCESS) {
                builder.setLights(Color.GREEN, 2000, 100);
            } else {
                builder.setLights(Color.RED, 2000, 100);
            }
        }

        if (DeviceUtils.isMinimumApiVersion(Build.VERSION_CODES.LOLLIPOP)) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            builder.setPriority(Notification.PRIORITY_MAX);
        }

        if (DeviceUtils.isMinimumApiVersion(Build.VERSION_CODES.JELLY_BEAN)) {
            return builder.build();
        }

        return builder.getNotification();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createServiceNotificationChannel(final Context context) {
        //TODO: REPLACE THESE TEMPORARY STRINGS
        final String channelId = "Fork app service";
        final String channelName = "Fork app foreground service";
        final NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        final android.app.NotificationManager nm = (android.app.NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(channel);
        return channelId;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationInfoChannel(final Context context, final int notificationInfoType) {
        //TODO: REPLACE THESE TEMPORARY STRINGS
        final String channelId = context.getString(R.string.app_name) + "status";
        final String channelName = channelId;
        final NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        if (notificationInfoType == NOTIFICATION_INFO_TYPE_ERROR) {
            channel.setLightColor(Color.RED);
        } else if (notificationInfoType == NOTIFICATION_INFO_TYPE_SUCCESS) {
            channel.setLightColor(Color.GREEN);
        }
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        final android.app.NotificationManager nm = (android.app.NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(channel);
        return channelId;
    }
}
