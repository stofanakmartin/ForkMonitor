package com.example.vmec.forkmonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.app.NotificationManagerCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.example.vmec.forkmonitor.data.model.Post;
import com.example.vmec.forkmonitor.data.remote.APIService;
import com.example.vmec.forkmonitor.data.remote.ApiUtils;
import com.example.vmec.forkmonitor.event.LocationPublishEvent;
import com.example.vmec.forkmonitor.event.TrackingDataChangeEvent;
import com.example.vmec.forkmonitor.event.TruckLoadedStateChangeEvent;
import com.example.vmec.forkmonitor.helper.LocationHelper;
import com.example.vmec.forkmonitor.helper.LocationPolygonHelper;
import com.example.vmec.forkmonitor.helper.NotificationHelper;
import com.example.vmec.forkmonitor.preference.BooleanPreference;
import com.example.vmec.forkmonitor.preference.IntPreference;
import com.example.vmec.forkmonitor.preference.StringPreference;
import com.example.vmec.forkmonitor.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Stofanak on 28/08/2018.
 */
public class TrackingManager {

    private BluetoothTrackingManager mBluetoothTrackingManager;
    private LocationHelper mLocationHelper;
    private LocationPolygonHelper mLocationPolygonHelper;
    private APIService mAPIService;
//    private IntPreference mTruckLoadedStatePreference;
    private IntPreference mTruckStatusPreference;
    private BooleanPreference mIsLocationTrackingEnabled;
    private BooleanPreference mIsBluetoothTrackingEnabled;
    private StringPreference mLastCharacteristicMsgPreference;
    private IntPreference mUltrasoundValuePreference;
    private IntPreference mBluetoothDeviceBatteryLevelPreference;
    private NotificationHelper mNotificationHelper;
    NotificationManagerCompat mNotificationManager;
    private int mBatteryLevel = -1;

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

            if(mBatteryLevel < Constants.PHONE_LOW_BATTERY_LEVEL_VALUE) {
                final Notification notification = mNotificationHelper.getInfoNotification(ctxt, ctxt.getString(R.string.notification_info_title_battery_level), new SpannableStringBuilder("plug in charger"), NotificationHelper.NOTIFICATION_INFO_TYPE_ERROR);
                mNotificationManager.notify(1, notification);
            }
        }
    };

    public TrackingManager(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mLocationHelper = new LocationHelper(context);
        mBluetoothTrackingManager = new BluetoothTrackingManager(context);
        mBluetoothTrackingManager.initialize(context);
        mNotificationHelper = new NotificationHelper();
        mNotificationManager = NotificationManagerCompat.from(context);

        mAPIService = ApiUtils.getAPIService();
        mLocationPolygonHelper = new LocationPolygonHelper(context);
//        mTruckLoadedStatePreference = new IntPreference(sp, Constants.PREFERENCE_LAST_TRUCK_LOADED_STATE, Constants.TRUCK_STATUS_NOT_INITIALIZED);
        mLastCharacteristicMsgPreference = new StringPreference(sp, Constants.PREFERENCE_LAST_CHARACTERISTIC_MSG, StringUtils.EMPTY_STRING);
        mTruckStatusPreference = new IntPreference(sp, Constants.PREFERENCE_LAST_TRUCK_STATUS, Constants.TRUCK_STATUS_NOT_INITIALIZED);
        mIsLocationTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_LOCATION_TRACKING_ENABLED, false);
        mIsBluetoothTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_BLUETOOTH_TRACKING_ENABLED, false);
        mBluetoothDeviceBatteryLevelPreference = new IntPreference(sp, Constants.PREFERENCE_BLUETOOTH_BATTERY_LEVEL, -1);
        mUltrasoundValuePreference = new IntPreference(sp, Constants.PREFERENCE_ULTRASOUND_VALUE, -1);
        mIsLocationTrackingEnabled.set(false);
        mIsBluetoothTrackingEnabled.set(false);

        context.registerReceiver(this.batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public void startTracking(final Context context) {
        mLocationHelper.startTrackingLocation(context);
        mBluetoothTrackingManager.startTracking(context);
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new TrackingDataChangeEvent());
    }

    public void stopTracking() {
        mLocationHelper.stopTrackingLocation();
        mBluetoothTrackingManager.stopTracking();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().post(new TrackingDataChangeEvent());
    }

    public void sendPost(String name, double lat,double lng, double battery, double accuracy,
                         int status, int ultrasoundDistance, int arduinoBatteryLevel) {
        Timber.d("Send status request to server");

        mAPIService.savePost(name, lat, lng,battery, accuracy, status, ultrasoundDistance, arduinoBatteryLevel).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                Timber.d("Send post response");
                if(response.isSuccessful()) {
//                    counterS +=1;
                    //Log.d("rest", "success");

                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Timber.d("Send post FAILURE response");
                //Log.e("rest", "Unable to submit post to API.");
//                counterF +=1;
                //Log.d("rest", "fail");

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationPublishEvent event) {
        final Location location = event.getLocation();
        final int locationPolygonStatus = mLocationPolygonHelper.checkLocationStatus(location);

        if(locationPolygonStatus == 0) {
            final int truckStatus = mTruckStatusPreference.get();
            // TODO: TEMPORARY docasne sa posiela na server status = 1 - zmena location
//            sendPost(android.os.Build.SERIAL, location.getLatitude(), location.getLongitude(), 30, location.getAccuracy(), mLastCharacteristicMsgPreference.get());

            sendPost(android.os.Build.SERIAL, location.getLatitude(), location.getLongitude(),
                    mBatteryLevel, location.getAccuracy(), 1, mUltrasoundValuePreference.get(),
                    mBluetoothDeviceBatteryLevelPreference.get());
        }
        EventBus.getDefault().post(new TrackingDataChangeEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TruckLoadedStateChangeEvent event) {
        final Location lastLocation = mLocationHelper.getLastLocation();
        if(lastLocation != null) {
            // TODO bateriu posielat
            // TODO: TEMPORARY docasne sa posiela na server status = 2 - zmena bluetooth
//            sendPost(android.os.Build.SERIAL, lastLocation.getLatitude(), lastLocation.getLongitude(), 30, lastLocation.getAccuracy(), event.getTruckLoadedState());

            sendPost(android.os.Build.SERIAL, lastLocation.getLatitude(), lastLocation.getLongitude(),
                    mBatteryLevel, lastLocation.getAccuracy(), 2, mUltrasoundValuePreference.get(),
                    mBluetoothDeviceBatteryLevelPreference.get());
        }
    }
}
