package com.forkmonitor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

/**
 * Created by Stofanak on 01/05/2019.
 */
public class ActivityRecognitionReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            final ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity detectedActivity = result.getMostProbableActivity();
            EventBus.getDefault().post(detectedActivity);
        } else {
            Timber.d("Detected activity NO RESULT");
        }
    }
}
