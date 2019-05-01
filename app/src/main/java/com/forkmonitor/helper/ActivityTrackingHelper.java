package com.forkmonitor.helper;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.forkmonitor.receiver.ActivityRecognitionReceiver;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;

/**
 * Created by Stofanak on 01/05/2019.
 */
public class ActivityTrackingHelper {

    private ActivityRecognitionClient recognitionClient;
    private PendingIntent trackingPendingIntent;
    private Task trackingTask;
    private DetectedActivity lastDetectedActivity;
    private BroadcastReceiver activityBroadcastReceiver;

    public ActivityTrackingHelper(final Context context) {
        recognitionClient = ActivityRecognition.getClient(context);

        final Intent intent = new Intent(context, ActivityRecognitionReceiver.class);
        trackingPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public void startTracking() {
        trackingTask = recognitionClient.requestActivityUpdates(180_000L, trackingPendingIntent);
        trackingTask.addOnSuccessListener(new OnSuccessListener() {
            @Override public void onSuccess(Object o) {
                Timber.d("Activity Tracking - requestActivityUpdates SUCCESS");
            }
        });
        trackingTask.addOnFailureListener(new OnFailureListener() {
            @Override public void onFailure(@NonNull Exception e) {
                Timber.d("Activity Tracking - requestActivityUpdates FAILED");
            }
        });

        EventBus.getDefault().register(this);
    }

    public void stopTracking() {
        recognitionClient.removeActivityUpdates(trackingPendingIntent);
        EventBus.getDefault().unregister(this);
        Timber.d("Activity Tracking - STOP");
    }

    public DetectedActivity getLastDetectedActivity() {
        return lastDetectedActivity;
    }

    public String getLastDetectedActivityType() {
        if (lastDetectedActivity != null) {
            switch (lastDetectedActivity.getType()) {
                case DetectedActivity.IN_VEHICLE:
                    return "IN_VEHICLE";
                case DetectedActivity.ON_BICYCLE:
                    return "ON_BICYCLE";
                case DetectedActivity.ON_FOOT:
                    return "ON_FOOT";
                case DetectedActivity.RUNNING:
                    return "RUNNING";
                case DetectedActivity.STILL:
                    return "STILL";
                case DetectedActivity.TILTING:
                    return "TILTING";
                case DetectedActivity.UNKNOWN:
                    return "UNKNOWN";
                case DetectedActivity.WALKING:
                    return "WALKING";
            }
        }
        return "NOT_DETECTED";
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DetectedActivity activity) {
        lastDetectedActivity = activity;
        Timber.d("Detected activity: %s", getLastDetectedActivityType());
    }
}
