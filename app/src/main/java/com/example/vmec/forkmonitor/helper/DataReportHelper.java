package com.example.vmec.forkmonitor.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.vmec.forkmonitor.Constants;
import com.example.vmec.forkmonitor.data.model.Post;
import com.example.vmec.forkmonitor.data.remote.APIService;
import com.example.vmec.forkmonitor.data.remote.ApiUtils;
import com.example.vmec.forkmonitor.preference.IntPreference;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
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

    public DataReportHelper(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mSendDataSuccessCounterPreference = new IntPreference(sp, Constants.PREFERENCE_SEND_DATA_SUCCESS_COUNTER, 0);
        mSendDataErrorCounterPreference = new IntPreference(sp, Constants.PREFERENCE_SEND_DATA_ERROR_COUNTER, 0);
        mBleReadSuccessTotalCounterPreference = new IntPreference(sp, Constants.PREFERENCE_BLE_SUCCESS_READ_TOTAL_COUNT, 0);
        mBleReadFailTotalCounterPreference = new IntPreference(sp, Constants.PREFERENCE_BLE_FAIL_READ_TOTAL_COUNT, 0);
        mBleUltrasoundFailCounterPreference = new IntPreference(sp, Constants.PREFERENCE_BLE_ULTRASOUND_FAIL_TOTAL_COUNT, 0);
        mSendDataSuccessCounterPreference.set(0);
        mSendDataErrorCounterPreference.set(0);
        mAPIService = ApiUtils.getAPIService();
    }

    public void sendPost(String name, double lat,double lng, double battery, double accuracy,
                         int status, int ultrasoundDistance, int arduinoBatteryLevel) {
        Timber.d("Send status request to server");

        final String additionalParam = String.format(Locale.US, "sendData-s:%d||e:%d||ble-s:%d||f:%d||e:%d",
                mSendDataSuccessCounterPreference.get(),
                mSendDataErrorCounterPreference.get(),
                mBleReadSuccessTotalCounterPreference.get(),
                mBleUltrasoundFailCounterPreference.get(),
                mBleReadFailTotalCounterPreference.get());

        mAPIService.savePost(name, lat, lng,battery, accuracy, status, ultrasoundDistance, arduinoBatteryLevel, additionalParam).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                Timber.d("Send post response");
                if(response.isSuccessful()) {
                    final int successCounter = mSendDataSuccessCounterPreference.get() + 1;
                    mSendDataSuccessCounterPreference.set(successCounter);
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Timber.d("Send post FAILURE response");
                final int errorCounter = mSendDataErrorCounterPreference.get() + 1;
                mSendDataErrorCounterPreference.set(errorCounter);
            }
        });
    }
}
