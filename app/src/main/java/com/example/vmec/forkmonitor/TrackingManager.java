package com.example.vmec.forkmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.example.vmec.forkmonitor.data.model.Post;
import com.example.vmec.forkmonitor.data.remote.APIService;
import com.example.vmec.forkmonitor.data.remote.ApiUtils;
import com.example.vmec.forkmonitor.event.LocationPublishEvent;
import com.example.vmec.forkmonitor.event.TruckLoadedStatusChangeEvent;
import com.example.vmec.forkmonitor.helper.LocationHelper;
import com.example.vmec.forkmonitor.helper.LocationPolygonHelper;
import com.example.vmec.forkmonitor.preference.IntPreference;

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

    public TrackingManager(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mLocationHelper = new LocationHelper();
        mBluetoothTrackingManager = new BluetoothTrackingManager(context);
        mBluetoothTrackingManager.initialize(context);

        mAPIService = ApiUtils.getAPIService();
        mLocationPolygonHelper = new LocationPolygonHelper(context);
//        mTruckLoadedStatePreference = new IntPreference(sp, Constants.PREFERENCE_LAST_TRUCK_LOADED_STATE, Constants.TRUCK_STATUS_NOT_INITIALIZED);
        mTruckStatusPreference = new IntPreference(sp, Constants.PREFERENCE_LAST_TRUCK_STATUS, Constants.TRUCK_STATUS_NOT_INITIALIZED);

    }

    public void startTracking(final Context context) {
        mLocationHelper.startTrackingLocation(context);
        mBluetoothTrackingManager.startTracking(context);
        EventBus.getDefault().register(this);
    }

    public void stopTracking() {
        mLocationHelper.stopTrackingLocation();
        mBluetoothTrackingManager.stopTracking();
        EventBus.getDefault().unregister(this);
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
            sendPost(android.os.Build.SERIAL, location.getLatitude(), location.getLongitude(), 30, location.getAccuracy(), truckStatus);  //TODO bateriu posielat
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TruckLoadedStatusChangeEvent event) {
        final Location lastLocation = mLocationHelper.getLastLocation();
        if(lastLocation != null) {
            sendPost(android.os.Build.SERIAL, lastLocation.getLatitude(), lastLocation.getLongitude(), 30, lastLocation.getAccuracy(), event.getTruckLoadedStatus());  //TODO bateriu posielat
        }
    }
}
