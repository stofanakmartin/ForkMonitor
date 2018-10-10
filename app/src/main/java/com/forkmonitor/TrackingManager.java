package com.forkmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;

import com.forkmonitor.event.ArduinoBatteryChangeEvent;
import com.forkmonitor.event.BLEDataReceivedEvent;
import com.forkmonitor.event.BLEFailedToReadStatusEvent;
import com.forkmonitor.event.LocationPublishEvent;
import com.forkmonitor.event.TrackingDataChangeEvent;
import com.forkmonitor.helper.BatteryTrackingHelper;
import com.forkmonitor.helper.BluetoothTrackingHelper2;
import com.forkmonitor.helper.DataReportHelper;
import com.forkmonitor.helper.LocationHelper;
import com.forkmonitor.helper.LocationPolygonHelper;
import com.forkmonitor.preference.BooleanPreference;
import com.forkmonitor.preference.IntPreference;
import com.forkmonitor.preference.StringPreference;
import com.forkmonitor.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Stofanak on 28/08/2018.
 */
public class TrackingManager {

    private BluetoothTrackingHelper2 mBluetoothTrackingHelper;
    private LocationHelper mLocationHelper;
    private LocationPolygonHelper mLocationPolygonHelper;

    private IntPreference mTruckLoadedStatePreference;
    private IntPreference mStatusPreference;
    private BooleanPreference mIsLocationTrackingEnabled;
    private BooleanPreference mIsBluetoothTrackingEnabled;
    private StringPreference mLastCharacteristicMsgPreference;
    private IntPreference mUltrasoundValuePreference;
    private IntPreference mArduinoBatteryLevelPreference;
    private IntPreference mBleReadFailCounterPreference;
    private IntPreference mBleReadSuccessTotalCounterPreference;
    private IntPreference mBleReadFailTotalCounterPreference;
    private IntPreference mBleUltrasoundFailCounterPreference;
    private DataReportHelper mDataReportHelper;
    private BatteryTrackingHelper mPhoneBatteryStateTracker;
    private int mBleReadFailedCounter = 0;
    private int mBleNoChangeCounter = 0;
    private Handler mHandler;

    private Runnable mSendNoDataChangeRunnable = new Runnable() {
        @Override public void run() {
            Timber.d("Send no data change request status");
            sendDataReport(Constants.REPORT_STATUS_BLUETOOTH_NO_CHANGE);
        }
    };


    public TrackingManager(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mLocationHelper = new LocationHelper(context);
        mBluetoothTrackingHelper = new BluetoothTrackingHelper2(context);
        //TODO: DO SOMETHING WITH IT
        final boolean isBleInitialized = mBluetoothTrackingHelper.initialize(context);
        mPhoneBatteryStateTracker = new BatteryTrackingHelper(context);

        mDataReportHelper = new DataReportHelper(context);
        mLocationPolygonHelper = new LocationPolygonHelper(context);
        mTruckLoadedStatePreference = new IntPreference(sp, Constants.PREFERENCE_LAST_TRUCK_LOADED_STATE, Constants.TRUCK_STATUS_BLE_READ_FAILED);
        mLastCharacteristicMsgPreference = new StringPreference(sp, Constants.PREFERENCE_LAST_CHARACTERISTIC_MSG, StringUtils.EMPTY_STRING);
        mStatusPreference = new IntPreference(sp, Constants.PREFERENCE_LAST_STATUS, Constants.STATUS_NOT_INITIALIZED);
        mIsLocationTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_LOCATION_TRACKING_ENABLED, false);
        mIsBluetoothTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_BLUETOOTH_TRACKING_ENABLED, false);
        mArduinoBatteryLevelPreference = new IntPreference(sp, Constants.PREFERENCE_BLUETOOTH_BATTERY_LEVEL, Constants.BATTERY_VALUE_UNKWOWN);
        mUltrasoundValuePreference = new IntPreference(sp, Constants.PREFERENCE_ULTRASOUND_VALUE, -1);
        mBleReadFailCounterPreference = new IntPreference(sp, Constants.PREFERENCE_BLE_FAIL_READ_COUNT, 0);
        mBleReadSuccessTotalCounterPreference = new IntPreference(sp, Constants.PREFERENCE_BLE_SUCCESS_READ_TOTAL_COUNT, 0);
        mBleReadFailTotalCounterPreference = new IntPreference(sp, Constants.PREFERENCE_BLE_FAIL_READ_TOTAL_COUNT, 0);
        mBleUltrasoundFailCounterPreference = new IntPreference(sp, Constants.PREFERENCE_BLE_ULTRASOUND_FAIL_TOTAL_COUNT, 0);
        mIsLocationTrackingEnabled.set(false);
        mIsBluetoothTrackingEnabled.set(false);
        mBleReadSuccessTotalCounterPreference.set(0);
        mBleReadFailTotalCounterPreference.set(0);
        mBleUltrasoundFailCounterPreference.set(0);
        mStatusPreference.set(Constants.STATUS_NOT_INITIALIZED);
        mTruckLoadedStatePreference.set(Constants.TRUCK_STATUS_BLE_READ_FAILED);
        mHandler = new Handler();

        final DeviceConfigManager dcm = new DeviceConfigManager(context);
        dcm.initBluetoothConfiguration(context);
    }

    public void startTracking(final Context context) {
        Timber.d("Start tracking");
        mLocationHelper.startTrackingLocation(context);
        mBluetoothTrackingHelper.startTracking(context);
        mPhoneBatteryStateTracker.startTracking(context);
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new TrackingDataChangeEvent());
        postNoDataTimeoutAction();
    }

    public void stopTracking(final Context context) {
        Timber.d("Stop tracking");
        mLocationHelper.stopTrackingLocation();
        mBluetoothTrackingHelper.stopTracking();
        mPhoneBatteryStateTracker.stopTracking(context);
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().post(new TrackingDataChangeEvent());
        clearNoDataTimeoutAction();
    }

    private void evaluateArduinoBattery(final int arduinoBatteryLevel) {
        EventBus.getDefault().post(new ArduinoBatteryChangeEvent(arduinoBatteryLevel));
    }

    private void evaluateReceivedBluetoothData(final BLEDataReceivedEvent event) {
        final int ultrasoundValue = event.getUltrasoundValue();
        final int arduinoBatteryLevel = event.getArduinoBatteryLevel();
        final int lastTruckLoadedState = mTruckLoadedStatePreference.get();
        int oldStatus = mStatusPreference.get();
        int newStatus = oldStatus;
        int newTruckLoadedState = lastTruckLoadedState;

        mUltrasoundValuePreference.set(ultrasoundValue);
        mArduinoBatteryLevelPreference.set(arduinoBatteryLevel);
        evaluateArduinoBattery(arduinoBatteryLevel);

        if (ultrasoundValue >= Constants.ULTRASOUND_LOADED_UNLOADED_THRESHOLD_VALUE) {
            // UNLOADED
            newTruckLoadedState = Constants.TRUCK_STATUS_UNLOADED;
            newStatus = Constants.TRUCK_STATUS_UNLOADED;
        } else if (ultrasoundValue >= 0) {
            // LOADED
            newTruckLoadedState = Constants.TRUCK_STATUS_LOADED;
            newStatus = Constants.TRUCK_STATUS_LOADED;
        } else {
            // Fail status
            newStatus = Constants.STATUS_BLE_ULTRASOUND_FAIL;
            mBleUltrasoundFailCounterPreference.set(mBleUltrasoundFailCounterPreference.get() + 1);
        }

        mStatusPreference.set(newStatus);

        if(lastTruckLoadedState != newTruckLoadedState
            || (oldStatus != newStatus && oldStatus == Constants.STATUS_BLE_ULTRASOUND_FAIL)) {
            mBleNoChangeCounter = 0;
            onTruckLoadedStateChange(newTruckLoadedState, Constants.REPORT_STATUS_BLUETOOTH_LOADED_STATE_CHANGE);
        } else {
            mBleNoChangeCounter++;
        }
    }

    private void onTruckLoadedStateChange(final int newTruckLoadedState, final int dataReportStatus) {
        mTruckLoadedStatePreference.set(newTruckLoadedState);
        sendDataReport(dataReportStatus);
//        if(newTruckLoadedState != Constants.TRUCK_STATUS_UNKNOWN) {
//            sendDataReport(dataReportStatus);
//        }
    }

    private void sendDataReport(final int dataReportStatus) {
        final Location lastLocation = mLocationHelper.getLastLocation();
        if(lastLocation != null) {
            clearNoDataTimeoutAction();
            mDataReportHelper.sendPost(android.os.Build.SERIAL, lastLocation.getLatitude(), lastLocation.getLongitude(),
                    mPhoneBatteryStateTracker.getLastBatteryLevel(), lastLocation.getAccuracy(),
                    dataReportStatus, mUltrasoundValuePreference.get(),
                    mArduinoBatteryLevelPreference.get(), mBleNoChangeCounter);
            postNoDataTimeoutAction();
        } else {
            Timber.w("Cannot send data report - no location");
        }
    }

    private void clearNoDataTimeoutAction() {
        mHandler.removeCallbacks(mSendNoDataChangeRunnable);
    }

    private void postNoDataTimeoutAction() {
        mHandler.postDelayed(mSendNoDataChangeRunnable, Constants.SEND_DATA_NO_CHANGE_INTERVAL);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationPublishEvent event) {
        final Location location = event.getLocation();
        final int locationPolygonStatus = mLocationPolygonHelper.checkLocationStatus(location);

        if(locationPolygonStatus == 0) {
            clearNoDataTimeoutAction();
            mDataReportHelper.sendPost(android.os.Build.SERIAL, location.getLatitude(), location.getLongitude(),
                    mPhoneBatteryStateTracker.getLastBatteryLevel(), location.getAccuracy(),
                    Constants.REPORT_STATUS_LOCATION_POLYGON_CHANGE,
                    mUltrasoundValuePreference.get(),
                    mArduinoBatteryLevelPreference.get(),
                    mBleNoChangeCounter);
            postNoDataTimeoutAction();
        }
        EventBus.getDefault().post(new TrackingDataChangeEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BLEDataReceivedEvent event) {
        mBleReadFailedCounter = 0;
        mBleReadFailCounterPreference.set(0);
        mBleReadSuccessTotalCounterPreference.set(mBleReadSuccessTotalCounterPreference.get() + 1);
        evaluateReceivedBluetoothData(event);
        EventBus.getDefault().post(new TrackingDataChangeEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BLEFailedToReadStatusEvent event) {
        mBleNoChangeCounter++;
        mBleReadFailedCounter++;
        mBleReadFailCounterPreference.set(mBleReadFailedCounter);
        mBleReadFailTotalCounterPreference.set(mBleReadFailTotalCounterPreference.get() + 1);
        if(mBleReadFailedCounter == Constants.BLUETOOTH_READ_FAILURE_COUNT_LIMIT) {
            mUltrasoundValuePreference.set(Constants.ULTRASOUND_VALUE_UNKWOWN);
            mArduinoBatteryLevelPreference.set(Constants.BATTERY_VALUE_UNKWOWN);
            mTruckLoadedStatePreference.set(Constants.TRUCK_STATUS_BLE_READ_FAILED);
            mStatusPreference.set(Constants.TRUCK_STATUS_BLE_READ_FAILED);
            onTruckLoadedStateChange(Constants.TRUCK_STATUS_BLE_READ_FAILED, Constants.REPORT_STATUS_BLUETOOTH_READ_FAIL);
        }
        Timber.d("BLE - failed to read status %d times in row", mBleReadFailedCounter);
    }
}
