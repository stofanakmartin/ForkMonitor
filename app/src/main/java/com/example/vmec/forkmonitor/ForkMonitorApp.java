package com.example.vmec.forkmonitor;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Stofanak on 14/08/2018.
 */
public class ForkMonitorApp extends Application {

    @Override public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
//            Timber.plant(new CrashReportingTree());
        }
    }
}
