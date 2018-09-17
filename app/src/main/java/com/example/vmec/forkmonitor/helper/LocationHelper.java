package com.example.vmec.forkmonitor.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.example.vmec.forkmonitor.Constants;
import com.example.vmec.forkmonitor.event.LocationPublishEvent;
import com.example.vmec.forkmonitor.preference.BooleanPreference;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Stofanak on 13/08/2018.
 */
public class LocationHelper {

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mLastLocation;
    private BooleanPreference mIsLocationTrackingEnabled;

    public LocationHelper(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mIsLocationTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_LOCATION_TRACKING_ENABLED, false);
    }

    public void startTrackingLocation(final Context context) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mLocationRequest = createLocationRequest();
        mLocationCallback = createLocationCallback();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new RuntimeException("Error - cannot start location tracking. Missing location permissions.");
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        mIsLocationTrackingEnabled.set(true);
    }

    public void stopTrackingLocation() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mIsLocationTrackingEnabled.set(false);
    }

    public Location getLastLocation() {
        return mLastLocation;
    }

    private LocationRequest createLocationRequest() {
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(Constants.LOCATION_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOCATION_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private LocationCallback createLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Timber.d("Location callback - result null");
                    return;
                }
                final List<Location> locations = locationResult.getLocations();
                Timber.d("LocationCallback number of location results: %d", locations.size());
                for (Location location : locations) {
                    mLastLocation = location;
                    EventBus.getDefault().post(new LocationPublishEvent(location));
                    Timber.d("LocationCallback callback - result: %s", location.toString());
                }
            };
        };
    }
}
