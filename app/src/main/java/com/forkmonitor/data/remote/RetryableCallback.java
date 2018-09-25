package com.forkmonitor.data.remote;

import android.os.Handler;

import com.forkmonitor.Constants;
import com.forkmonitor.utils.ApiUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by Stofanak on 17/09/2018.
 */
public abstract class RetryableCallback<T> implements Callback<T> {

    private int totalRetries = 3;
    private static final String TAG = RetryableCallback.class.getSimpleName();
    private final Call<T> call;
    private int retryCount = 0;
    private Handler handler;

    public RetryableCallback(Call<T> call, int totalRetries) {
        this.call = call;
        this.totalRetries = totalRetries;
        handler = new Handler();
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (!ApiUtils.isCallSuccess(response))
            if (retryCount++ < totalRetries) {
                Timber.v("Retrying API Call -  (" + retryCount + " / " + totalRetries + ")");
                retry();
            } else {
                onFinalResponse(call, response);
            }
        else {
            onFinalResponse(call, response);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
//        Timber.e(TAG, t.getMessage());
        if (retryCount++ < totalRetries) {
            Timber.v("Retrying API Call -  (" + retryCount + " / " + totalRetries + ")");
            retry();
        } else
            onFinalFailure(call, t);
    }

    public void onFinalResponse(Call<T> call, Response<T> response) {

    }

    public void onFinalFailure(Call<T> call, Throwable t) {
    }

    private void retry() {
        final RetryableCallback<T> that = this;
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                call.clone().enqueue(that);
            }
        }, Constants.SEND_REQUEST_RETRY_INTERVAL);
    }
}
