package com.forkmonitor.utils;

import android.content.Context;
import android.location.LocationManager;

import timber.log.Timber;

/**
 * Created by Stofanak on 13/09/2018.
 */
public class LocationUtils {

    public static boolean isGpsLocationEnabled(final Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        try {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            Timber.e(ex);
            return false;
        }
    }
}
