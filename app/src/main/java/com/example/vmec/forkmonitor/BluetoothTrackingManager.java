package com.example.vmec.forkmonitor;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;

import com.example.vmec.forkmonitor.event.GattCharacteristicChangeEvent;
import com.example.vmec.forkmonitor.event.GattCharacteristicNotificationConfigEvent;
import com.example.vmec.forkmonitor.event.GattCharacteristicReadEvent;
import com.example.vmec.forkmonitor.event.GattCharacteristicWriteEvent;
import com.example.vmec.forkmonitor.event.GattConnectedEvent;
import com.example.vmec.forkmonitor.event.GattConnectionDestroyedEvent;
import com.example.vmec.forkmonitor.event.GattDisconnectedEvent;
import com.example.vmec.forkmonitor.event.GattServicesDiscoveredEvent;
import com.example.vmec.forkmonitor.event.TrackingDataChangeEvent;
import com.example.vmec.forkmonitor.event.TruckLoadedStateChangeEvent;
import com.example.vmec.forkmonitor.helper.BluetoothHelper;
import com.example.vmec.forkmonitor.preference.BooleanPreference;
import com.example.vmec.forkmonitor.preference.IntPreference;
import com.example.vmec.forkmonitor.preference.StringPreference;
import com.example.vmec.forkmonitor.utils.BluetoothUtils;
import com.example.vmec.forkmonitor.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class BluetoothTrackingManager {

    //TODO: REMOVE reference to context
    private Context mContext;
    private Handler mHandler;
    private BluetoothHelper mBluetoothHelper;
    private StringPreference mLastCharacteristicMsgPreference;
    private StringPreference mBluetoothDeviceNamePreference;
    private BooleanPreference mIsBluetoothTrackingEnabled;
    private BooleanPreference mIsBluetoothDeviceConnectedPreference;
    private IntPreference mTruckLoadedStatePreference;
    private IntPreference mTruckStatusPreference;
    private String mTmpCharacteristicMsgBuffer = StringUtils.EMPTY_STRING;
    private BluetoothGattCharacteristic mDeviceStatusCharacteristic;


    private Runnable mBluetoothReadCharacteristicRunnable = new Runnable() {
        @Override public void run() {
            Timber.d("Tracking interval fired - read bluetooth status");
            final int connectionStatus = mBluetoothHelper.getConnectionState();
            if(BluetoothHelper.STATE_DISCONNECTED == connectionStatus) {
                mBluetoothHelper.connect(mContext, Constants.BLUETOOTH_DEVICE_ADDRESS);
            } else if (BluetoothHelper.STATE_CONNECTED == connectionStatus) {
                Timber.d("Read bluetooth device status");
                mBluetoothHelper.writeToCharacteristic(mDeviceStatusCharacteristic, Constants.BLUETOOTH_DEVICE_COMMUNICATION_START_MSG);
            }
        }
    };

    public BluetoothTrackingManager(final Context context) {
        mHandler = new Handler();
        mBluetoothHelper = new BluetoothHelper();
    }

    public boolean initialize(final Context context) {
        mContext = context;
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mLastCharacteristicMsgPreference = new StringPreference(sp, Constants.PREFERENCE_LAST_CHARACTERISTIC_MSG, StringUtils.EMPTY_STRING);
        mIsBluetoothTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_BLUETOOTH_TRACKING_ENABLED, false);
        mBluetoothDeviceNamePreference = new StringPreference(sp, Constants.PREFERENCE_BLUETOOTH_DEVICE_NAME, StringUtils.EMPTY_STRING);
        mTruckLoadedStatePreference = new IntPreference(sp, Constants.PREFERENCE_LAST_TRUCK_LOADED_STATE, Constants.TRUCK_STATUS_NOT_INITIALIZED);
        mTruckStatusPreference = new IntPreference(sp, Constants.PREFERENCE_LAST_TRUCK_STATUS, Constants.TRUCK_STATUS_NOT_INITIALIZED);
        mIsBluetoothDeviceConnectedPreference = new BooleanPreference(sp, Constants.PREFERENCE_IS_BLUETOOTH_DEVICE_CONNECTED, false);
        mIsBluetoothDeviceConnectedPreference.set(false);

        final boolean bluetoothInitStatus = mBluetoothHelper.initialize(context);

        if(bluetoothInitStatus) {
            Timber.d("ForkMonitorTrackingManager BLUETOOTH INITIALIZED SUCCESSFULLY");
        } else {
            Timber.d("ForkMonitorTrackingManager BLUETOOTH FAILED TO INITIALIZE");
        }

        return bluetoothInitStatus;
    }

    public void startTracking(final Context context) {
        EventBus.getDefault().register(this);
        mBluetoothHelper.connect(context, Constants.BLUETOOTH_DEVICE_ADDRESS);
        mIsBluetoothTrackingEnabled.set(true);
    }

    public void stopTracking() {
        mHandler.removeCallbacks(mBluetoothReadCharacteristicRunnable);
        mBluetoothHelper.disconnect();
        mIsBluetoothTrackingEnabled.set(false);
        mIsBluetoothDeviceConnectedPreference.set(false);

        //TODO: IS IT NOT EARLY?
        EventBus.getDefault().unregister(this);
    }

    private void setNextCharacteristicReadingAction() {
        mHandler.postDelayed(mBluetoothReadCharacteristicRunnable, Constants.BLUETOOTH_CHARACTERISTIC_READ_INTERVAL_MS);
    }

    private void onCharacteristicRead(GattCharacteristicReadEvent event) {
        final BluetoothGattCharacteristic characteristic = event.getCharacteristic();

        if(characteristic.getUuid().toString().equals(Constants.BLUETOOTH_DEVICE_NAME_CHARACTERISTIC_UUID)) {
            final String deviceName = BluetoothUtils.getCharacteristicStringValue(characteristic);
            mBluetoothDeviceNamePreference.set(deviceName);
            if(deviceName.equals(Constants.BLUETOOTH_DEVICE_NAME)) {
                mBluetoothHelper.readCharacteristic(Constants.BLUETOOTH_FORK_MONITOR_SERVICE_UUID, Constants.BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID);
            } else {
                Timber.e("Bluetooth device name does not match. Current device name: %s", deviceName);
                mBluetoothHelper.disconnect();
            }
        } else if(characteristic.getUuid().toString().equals(Constants.BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID)) {
            final int characteristicProperties = characteristic.getProperties();

            if ((characteristicProperties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0
                    && (characteristicProperties | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                mDeviceStatusCharacteristic = characteristic;
                mBluetoothHelper.setCharacteristicNotification(characteristic, true);
            } else {
                Timber.e("Bluetooth characteristic does not support NOTIFICATION or WRITE feature");
                mBluetoothHelper.disconnect();
            }
        }
        EventBus.getDefault().post(new TrackingDataChangeEvent());
    }

    private void onCharacteristicChange(GattCharacteristicChangeEvent event) {
        final BluetoothGattCharacteristic characteristic = event.getCharacteristic();
        final String charMessage = BluetoothUtils.getCharacteristicStringValue(characteristic);

        Timber.d("Characteristic change received - message: %s", charMessage);

        if (TextUtils.isEmpty(charMessage)) {
            mLastCharacteristicMsgPreference.set(StringUtils.EMPTY_STRING);
            Timber.w("Received empty message or no data");
        } else {
            if(charMessage.toLowerCase().contains(Constants.BLUETOOTH_DEVICE_COMMUNICATION_END_MSG)) {
                final String finalMessage = mTmpCharacteristicMsgBuffer + charMessage;
                final String finalMsgCleared = finalMessage.replace(Constants.BLUETOOTH_DEVICE_COMMUNICATION_END_MSG, StringUtils.EMPTY_STRING);
                evaluateDeviceValue(finalMsgCleared);
                mTmpCharacteristicMsgBuffer = StringUtils.EMPTY_STRING;
                mLastCharacteristicMsgPreference.set(finalMsgCleared);
                mBluetoothHelper.writeToCharacteristic(characteristic, Constants.BLUETOOTH_DEVICE_COMMUNICATION_END_MSG);
                setNextCharacteristicReadingAction();
            } else {
                // Append characteristic value to tmp buffer
                mTmpCharacteristicMsgBuffer += charMessage;
                // Set timeout - when no more request are going to be received in specified period
                // of time the connection is going to be closed
                mBluetoothHelper.requestConnectionDisconnectAfterTimeout();
            }
        }

        EventBus.getDefault().post(new TrackingDataChangeEvent());
    }

    private void evaluateDeviceValue(final String deviceStatus) {
        if(TextUtils.isEmpty(deviceStatus)) {
            Timber.w("Empty device status message - nothing to process");
            return;
        }

        try {
            final int ultrasoundValue = Integer.parseInt(deviceStatus);
            final int lastTruckLoadedState = mTruckLoadedStatePreference.get();
            int newTruckStatus = mTruckStatusPreference.get();
            int newTruckLoadedState = lastTruckLoadedState;

            if(ultrasoundValue == Constants.ULTRASOUND_NOT_READ_VALUE) {
                // N/A
                newTruckStatus = Constants.TRUCK_STATUS_ERROR_VALUE;
            } else if (ultrasoundValue >= Constants.ULTRASOUND_LOADED_UNLOADED_THRESHOLD_VALUE) {
                // UNLOADED
                newTruckLoadedState = Constants.TRUCK_STATUS_UNLOADED;
                newTruckStatus = Constants.TRUCK_STATUS_UNLOADED;
            } else {
                // LOADED
                newTruckLoadedState = Constants.TRUCK_STATUS_LOADED;
                newTruckStatus = Constants.TRUCK_STATUS_LOADED;
            }

            mTruckStatusPreference.set(newTruckStatus);

            if(lastTruckLoadedState != newTruckLoadedState) {
                mTruckLoadedStatePreference.set(newTruckLoadedState);
                EventBus.getDefault().post(new TruckLoadedStateChangeEvent(newTruckLoadedState));
            }
        } catch(NumberFormatException ex) {
            Timber.w("Bluetooth ultrasound value is not a number");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattConnectedEvent event) {
        mIsBluetoothDeviceConnectedPreference.set(true);
        EventBus.getDefault().post(new TrackingDataChangeEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattDisconnectedEvent event) {
        mHandler.removeCallbacks(mBluetoothReadCharacteristicRunnable);
        mDeviceStatusCharacteristic = null;
        setNextCharacteristicReadingAction();
        mIsBluetoothDeviceConnectedPreference.set(false);
        EventBus.getDefault().post(new TrackingDataChangeEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattConnectionDestroyedEvent event) {
        mHandler.removeCallbacks(mBluetoothReadCharacteristicRunnable);
        mDeviceStatusCharacteristic = null;
        setNextCharacteristicReadingAction();
        mIsBluetoothDeviceConnectedPreference.set(false);
        EventBus.getDefault().post(new TrackingDataChangeEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattServicesDiscoveredEvent event) {
        // Read device name characteristic first
        mBluetoothHelper.readCharacteristic(Constants.BLUETOOTH_DEVICE_NAME_SERVICE_UUID, Constants.BLUETOOTH_DEVICE_NAME_CHARACTERISTIC_UUID);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattCharacteristicReadEvent event) {
        Timber.d("Characteristic read event");
        onCharacteristicRead(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattCharacteristicChangeEvent event) {
        Timber.d("Characteristic change event");
        onCharacteristicChange(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattCharacteristicNotificationConfigEvent event) {
        Timber.d("Characteristic notification configuration callback received");

        if(mDeviceStatusCharacteristic == null) {
            Timber.e("Device status characteristic not initialized");
            return;
        }
        mBluetoothHelper.writeToCharacteristic(mDeviceStatusCharacteristic, Constants.BLUETOOTH_DEVICE_COMMUNICATION_START_MSG);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattCharacteristicWriteEvent event) {
        Timber.d("Characteristic write callback received");

        final BluetoothGattCharacteristic characteristic = event.getCharacteristic();
        final String writtenValue = new String(characteristic.getValue());
        if(writtenValue.equals(Constants.BLUETOOTH_DEVICE_COMMUNICATION_START_MSG)) {
            // Set timeout - when no more request are going to be received in specified period
            // of time the connection is going to be closed
            mBluetoothHelper.requestConnectionDisconnectAfterTimeout();
        }
    }
}
