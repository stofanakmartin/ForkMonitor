package com.example.vmec.forkmonitor.helper;

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

import com.example.vmec.forkmonitor.activity.MainActivity1;
import com.example.vmec.forkmonitor.R;
import com.example.vmec.forkmonitor.utils.DeviceUtils;

/**
 * Created by Stofanak on 19/08/2018.
 */
public class NotificationHelper {
    public static final int NOTIFICATION_ID = 1338;

    public NotificationHelper() {
    }

    public Notification getNotification(final Context context) {
        String title = getNotificationTitle(context);
        final SpannableStringBuilder content = getNotificationDescription(context);
        return getNotification(context, title, content);
    }

    private String getNotificationTitle(final Context context) {
        return context.getString(R.string.tracking_service_notification_title);
    }

    private SpannableStringBuilder getNotificationDescription(final Context context) {
        return new SpannableStringBuilder("*TODO DESCRIPTION*");
    }

    private Notification getNotification(final Context context, final String title, final SpannableStringBuilder content) {
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
            final String notificationChannelId = createNotificationChannel(context);
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

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(final Context context) {
        //TODO: REPLACE THESE TEMPORARY STRINGS
        final String channelId = "Fork app service";
        final String channelName = "Fork app foreground service";
        final NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        final android.app.NotificationManager nm = (android.app.NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(channel);
        return channelId;
    }
}
