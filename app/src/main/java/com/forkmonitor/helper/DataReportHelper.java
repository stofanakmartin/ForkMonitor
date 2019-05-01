package com.forkmonitor.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.forkmonitor.Constants;
import com.forkmonitor.data.model.Post;
import com.forkmonitor.data.remote.APIService;
import com.forkmonitor.data.remote.ApiUtils;
import com.forkmonitor.data.remote.RetryableCallback;
import com.forkmonitor.preference.IntPreference;
import com.forkmonitor.utils.AppInfoUtils;
import com.forkmonitor.utils.TimeUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Stofanak on 12/09/2018.
 */
public class DataReportHelper {

    private APIService mAPIService;
    private IntPreference mSendDataSuccessCounterPreference;
    private IntPreference mSendDataErrorCounterPreference;
    private IntPreference mBleReadSuccessTotalCounterPreference;
    private IntPreference mBleReadFailTotalCounterPreference;
    private IntPreference mBleUltrasoundFailCounterPreference;
    private IntPreference mTruckLoadedStatePreference;
    private List<Call<Post>> requestQueue;
    private String mAppVersionName;

    public DataReportHelper(final Context context) {
        requestQueue = new LinkedList<>();
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mSendDataSuccessCounterPreference = new IntPreference(sp, Constants.PREFERENCE_SEND_DATA_SUCCESS_COUNTER, 0);
        mSendDataErrorCounterPreference = new IntPreference(sp, Constants.PREFERENCE_SEND_DATA_ERROR_COUNTER, 0);
        mBleReadSuccessTotalCounterPreference = new IntPreference(sp, Constants.PREFERENCE_BLE_SUCCESS_READ_TOTAL_COUNT, 0);
        mBleReadFailTotalCounterPreference = new IntPreference(sp, Constants.PREFERENCE_BLE_FAIL_READ_TOTAL_COUNT, 0);
        mBleUltrasoundFailCounterPreference = new IntPreference(sp, Constants.PREFERENCE_BLE_ULTRASOUND_FAIL_TOTAL_COUNT, 0);
        mTruckLoadedStatePreference = new IntPreference(sp, Constants.PREFERENCE_LAST_TRUCK_LOADED_STATE, Constants.TRUCK_STATUS_BLE_READ_FAILED);
        mSendDataSuccessCounterPreference.set(0);
        mSendDataErrorCounterPreference.set(0);
        mAPIService = ApiUtils.getAPIService();
        mAppVersionName = AppInfoUtils.getAppVersionName(context);
    }

    public void sendPost(String name, double lat, double lng, double battery, double accuracy,
                         int status, int ultrasoundDistance, int arduinoBatteryLevel, int bleNoChangeCounter,
                         String detectedActivityType) {
        Timber.d("Send status request to server");
        final String currentTimestamp = TimeUtils.getCurrentTimestampISO();

        final String additionalParam = String.format(Locale.US, "sendData-s:%d||e:%d||ble-s:%d||f:%d||e:%d||noChange:%d||timestamp:%s||appVer:%s||activity:%s",
                mSendDataSuccessCounterPreference.get(),
                mSendDataErrorCounterPreference.get(),
                mBleReadSuccessTotalCounterPreference.get(),
                mBleUltrasoundFailCounterPreference.get(),
                mBleReadFailTotalCounterPreference.get(),
                bleNoChangeCounter,
                currentTimestamp,
                mAppVersionName,
                detectedActivityType);

        final String statusWithTruckState = String.format(Locale.US, "%d%d", status, mTruckLoadedStatePreference.get());

        try {
            final int statusWithTruckStateInt = Integer.parseInt(statusWithTruckState, 10);
            status = statusWithTruckStateInt;
        } catch (NumberFormatException e) {
            Timber.e("Failed to parse status %s to int", statusWithTruckState);
            Timber.e(e);
        }


        final Call<Post> post = mAPIService.savePost(name, lat, lng,battery, accuracy, status,
                                                     ultrasoundDistance, arduinoBatteryLevel, additionalParam);
        post.enqueue(new RetryableCallback<Post>(post, Constants.SEND_REQUEST_RETRY_ATTEMPTS) {
            @Override public void onFinalResponse(Call<Post> call, Response<Post> response) {
                super.onFinalResponse(call, response);
                Timber.d("Send post response");
                if(response.isSuccessful()) {
                    final int successCounter = mSendDataSuccessCounterPreference.get() + 1;
                    mSendDataSuccessCounterPreference.set(successCounter);
                }
                requestQueue.remove(post);
                Timber.d("Request queue size: %d", requestQueue.size());
            }

            @Override public void onFinalFailure(Call<Post> call, Throwable t) {
                super.onFinalFailure(call, t);
                Timber.d("Send post FAILURE response");
                final int errorCounter = mSendDataErrorCounterPreference.get() + 1;
                mSendDataErrorCounterPreference.set(errorCounter);
                requestQueue.remove(post);
                Timber.d("Request queue size: %d", requestQueue.size());
            }
        });
        requestQueue.add(post);
    }
}
