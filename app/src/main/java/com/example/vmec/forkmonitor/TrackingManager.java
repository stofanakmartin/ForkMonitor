package com.example.vmec.forkmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.text.TextUtils;

import com.example.vmec.forkmonitor.data.model.Post;
import com.example.vmec.forkmonitor.data.remote.APIService;
import com.example.vmec.forkmonitor.data.remote.ApiUtils;
import com.example.vmec.forkmonitor.event.LocationPublishEvent;
import com.example.vmec.forkmonitor.event.TrackingDataChangeEvent;
import com.example.vmec.forkmonitor.event.TruckLoadedStateChangeEvent;
import com.example.vmec.forkmonitor.helper.LocationHelper;
import com.example.vmec.forkmonitor.helper.LocationPolygonHelper;
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

    public TrackingManager(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mLocationHelper = new LocationHelper(context);
        mBluetoothTrackingManager = new BluetoothTrackingManager(context);
        mBluetoothTrackingManager.initialize(context);

        mAPIService = ApiUtils.getAPIService();
        mLocationPolygonHelper = new LocationPolygonHelper(context);
//        mTruckLoadedStatePreference = new IntPreference(sp, Constants.PREFERENCE_LAST_TRUCK_LOADED_STATE, Constants.TRUCK_STATUS_NOT_INITIALIZED);
        mLastCharacteristicMsgPreference = new StringPreference(sp, Constants.PREFERENCE_LAST_CHARACTERISTIC_MSG, StringUtils.EMPTY_STRING);
        mTruckStatusPreference = new IntPreference(sp, Constants.PREFERENCE_LAST_TRUCK_STATUS, Constants.TRUCK_STATUS_NOT_INITIALIZED);
        mIsLocationTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_LOCATION_TRACKING_ENABLED, false);
        mIsBluetoothTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_BLUETOOTH_TRACKING_ENABLED, false);
        mIsLocationTrackingEnabled.set(false);
        mIsBluetoothTrackingEnabled.set(false);
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

    public void sendPost(String name, double lat,double lng, double battery, double accuracy, int status) {
        Timber.d("Send status request to server");
        mAPIService.savePost(name, lat, lng,battery, accuracy,status);

        mAPIService.savePost(name, lat, lng,battery, accuracy,status).enqueue(new Callback<Post>() {
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
            // TODO bateriu posielat
            // TODO: TEMPORARY docasne sa posiela na server hodnota charakteristiky z bluetooth
//            sendPost(android.os.Build.SERIAL, location.getLatitude(), location.getLongitude(), 30, location.getAccuracy(), mLastCharacteristicMsgPreference.get());
            try {
                int characteristicValue = -1;
                if(!TextUtils.isEmpty(mLastCharacteristicMsgPreference.get())) {
                    characteristicValue = Integer.parseInt(mLastCharacteristicMsgPreference.get());
                }
                sendPost(android.os.Build.SERIAL, location.getLatitude(), location.getLongitude(), 30, location.getAccuracy(), characteristicValue);
            } catch (NumberFormatException e) {
                Timber.w("Cannot convert last characteristic message to integer");
            }
        }
        EventBus.getDefault().post(new TrackingDataChangeEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TruckLoadedStateChangeEvent event) {
        final Location lastLocation = mLocationHelper.getLastLocation();
        if(lastLocation != null) {
            // TODO bateriu posielat
            // TODO: TEMPORARY docasne sa posiela na server hodnota charakteristiky z bluetooth
//            sendPost(android.os.Build.SERIAL, lastLocation.getLatitude(), lastLocation.getLongitude(), 30, lastLocation.getAccuracy(), event.getTruckLoadedState());
            sendPost(android.os.Build.SERIAL, lastLocation.getLatitude(), lastLocation.getLongitude(), 30, lastLocation.getAccuracy(), event.getTruckLoadedState());
        }
    }
}
