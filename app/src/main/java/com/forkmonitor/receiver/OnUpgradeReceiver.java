package com.forkmonitor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.forkmonitor.service.TrackingService;

import timber.log.Timber;

/**
 * Created by Stofanak on 27/09/2018.
 */
public class OnUpgradeReceiver extends BroadcastReceiver {

    /**
     *
     * @param context
     * @param intent
     */
    @Override public void onReceive(Context context, Intent intent) {
        // TODO: Check permissions
        Timber.d("Application version upgrade broadcast");
        startTrackingService(context);
    }

    private void startTrackingService(final Context context) {
        final Intent trackingIntent = new Intent(context, TrackingService.class);
        context.startService(trackingIntent);
        Timber.d("Starting TrackingService");
    }
}
