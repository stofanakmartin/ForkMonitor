package com.example.vmec.forkmonitor;

import android.app.Application;
import android.content.SharedPreferences;

import timber.log.Timber;

/**
 * Created by Stofanak on 14/08/2018.
 */
public class ForkMonitorApp extends Application {

    private static ForkMonitorApp instance;

    @Override public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new ForkDebugTree());
        } else {
//            Timber.plant(new CrashReportingTree());
        }
//        Timber.plant(new FileLoggingTree(this));
    }

    /**
     * @return shared preferences based on PREF_NAME
     */
    public SharedPreferences getSP() {
        return getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
    }

    /**
     * @return shared preferences editor based on PREF_NAME
     */
    public SharedPreferences.Editor editSP() {
        return getSP().edit();
    }
}
