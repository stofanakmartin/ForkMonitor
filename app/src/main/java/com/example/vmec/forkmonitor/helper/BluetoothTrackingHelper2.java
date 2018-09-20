package com.example.vmec.forkmonitor.helper;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;

import com.example.vmec.forkmonitor.Constants;
import com.example.vmec.forkmonitor.event.BLEDataReceivedEvent;
import com.example.vmec.forkmonitor.event.BLEFailedToReadStatusEvent;
import com.example.vmec.forkmonitor.event.GattCharacteristicChangeEvent;
import com.example.vmec.forkmonitor.event.GattCharacteristicNotificationConfigEvent;
import com.example.vmec.forkmonitor.event.GattCharacteristicReadEvent;
import com.example.vmec.forkmonitor.event.GattCharacteristicWriteEvent;
import com.example.vmec.forkmonitor.event.GattConnectedEvent;
import com.example.vmec.forkmonitor.event.GattDisconnectedEvent;
import com.example.vmec.forkmonitor.event.GattServicesDiscoveredEvent;
import com.example.vmec.forkmonitor.event.TrackingDataChangeEvent;
import com.example.vmec.forkmonitor.preference.BooleanPreference;
import com.example.vmec.forkmonitor.preference.IntPreference;
import com.example.vmec.forkmonitor.preference.StringPreference;
import com.example.vmec.forkmonitor.utils.BluetoothUtils;
import com.example.vmec.forkmonitor.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.Time;
import java.util.regex.Pattern;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class BluetoothTrackingHelper2 {

    //TODO: TRY TO REMOVE reference to context
    private Context mContext;
    private Handler mHandler;
    private BluetoothHelper2 mBluetoothHelper;
    private StringPreference mLastCharacteristicMsgPreference;
    private BooleanPreference mIsBluetoothTrackingEnabled;
    private BooleanPreference mIsBluetoothDeviceConnectedPreference;
//    private IntPreference mTruckLoadedStatePreference;
    private IntPreference mTruckStatusPreference;
//    private IntPreference mBluetoothDeviceBatteryLevelPreference;
//    private IntPreference mUltrasoundValuePreference;
    private StringPreference mBleNamePreference;
    private StringPreference mBleHwAddressPreference;
    private String mTmpCharacteristicMsgBuffer = StringUtils.EMPTY_STRING;
    private BluetoothGattCharacteristic mDeviceStatusCharacteristic;
    private boolean mIsBluetoothReadSuccessfully = false;


    private Runnable mBluetoothReadCharacteristicRunnable = new Runnable() {
        @Override public void run() {
            Timber.d("Tracking interval fired - read bluetooth status");
            final int connectionStatus = mBluetoothHelper.getConnectionState();
            mIsBluetoothReadSuccessfully = false;
            if(BluetoothHelper2.STATE_DISCONNECTED == connectionStatus) {
                mBluetoothHelper.connect(mContext, mBleHwAddressPreference.get());
            } else if (BluetoothHelper2.STATE_CONNECTED == connectionStatus) {
                Timber.d("Read bluetooth device status");
                mBluetoothHelper.writeToCharacteristic(mDeviceStatusCharacteristic, Constants.BLUETOOTH_DEVICE_COMMUNICATION_START_MSG);
            }
        }
    };

    public BluetoothTrackingHelper2(final Context context) {
        mHandler = new Handler();
        mBluetoothHelper = new BluetoothHelper2();
    }

    public boolean initialize(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mLastCharacteristicMsgPreference = new StringPreference(sp, Constants.PREFERENCE_LAST_CHARACTERISTIC_MSG, StringUtils.EMPTY_STRING);
        mIsBluetoothTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_BLUETOOTH_TRACKING_ENABLED, false);
//        mTruckLoadedStatePreference = new IntPreference(sp, Constants.PREFERENCE_LAST_TRUCK_LOADED_STATE, Constants.TRUCK_STATUS_NOT_INITIALIZED);
        mTruckStatusPreference = new IntPreference(sp, Constants.PREFERENCE_LAST_STATUS, Constants.TRUCK_STATUS_UNKNOWN);
        mIsBluetoothDeviceConnectedPreference = new BooleanPreference(sp, Constants.PREFERENCE_IS_BLUETOOTH_DEVICE_CONNECTED, false);
//        mBluetoothDeviceBatteryLevelPreference = new IntPreference(sp, Constants.PREFERENCE_BLUETOOTH_BATTERY_LEVEL, 0);
//        mUltrasoundValuePreference = new IntPreference(sp, Constants.PREFERENCE_ULTRASOUND_VALUE, -1);
        mBleHwAddressPreference = new StringPreference(sp, Constants.PREFERENCE_DEVICE_CONFIG_BLE_HW_ADDRESS, StringUtils.EMPTY_STRING);
        mBleNamePreference = new StringPreference(sp, Constants.PREFERENCE_DEVICE_CONFIG_BLE_NAME, StringUtils.EMPTY_STRING);

        final boolean bluetoothInitStatus = mBluetoothHelper.initialize(context);

        if(bluetoothInitStatus) {
            Timber.d("ForkMonitorTrackingManager BLUETOOTH INITIALIZED SUCCESSFULLY");
        } else {
            Timber.d("ForkMonitorTrackingManager BLUETOOTH FAILED TO INITIALIZE");
        }

        return bluetoothInitStatus;
    }

    public void startTracking(final Context context) {
        mContext = context;
        EventBus.getDefault().register(this);
        mBluetoothHelper.connect(context, mBleHwAddressPreference.get());
        mIsBluetoothTrackingEnabled.set(true);
    }

    public void stopTracking() {
        mContext = null;
        mHandler.removeCallbacks(mBluetoothReadCharacteristicRunnable);
        mBluetoothHelper.closeConnection();
        mIsBluetoothTrackingEnabled.set(false);
        mIsBluetoothDeviceConnectedPreference.set(false);

        //TODO: IS IT NOT EARLY?
        EventBus.getDefault().unregister(this);
    }

    private void setNextCharacteristicReadingAction() {
        Timber.d("Set next BLE reading action in %d ms", Constants.BLUETOOTH_CHARACTERISTIC_READ_INTERVAL_MS);
        mHandler.postDelayed(mBluetoothReadCharacteristicRunnable, Constants.BLUETOOTH_CHARACTERISTIC_READ_INTERVAL_MS);
    }

    private void onCharacteristicRead(GattCharacteristicReadEvent event) {
        final BluetoothGattCharacteristic characteristic = event.getCharacteristic();

        if(characteristic.getUuid().toString().equals(Constants.BLUETOOTH_DEVICE_NAME_CHARACTERISTIC_UUID)) {
            final String deviceName = BluetoothUtils.getCharacteristicStringValue(characteristic);

            if(deviceName.equals(mBleNamePreference.get())) {
                mBluetoothHelper.readCharacteristic(Constants.BLUETOOTH_FORK_MONITOR_SERVICE_UUID, Constants.BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID);
            } else {

                Timber.e("Bluetooth device name does not match. Current device name: %s", deviceName);
                mBluetoothHelper.closeConnection();
            }
        } else if(characteristic.getUuid().toString().equals(Constants.BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID)) {
            final int characteristicProperties = characteristic.getProperties();

            if ((characteristicProperties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0
                    && (characteristicProperties | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                mDeviceStatusCharacteristic = characteristic;
                mBluetoothHelper.setCharacteristicNotification(characteristic, true);
            } else {
                Timber.e("Bluetooth characteristic does not support NOTIFICATION or WRITE feature");
                mBluetoothHelper.closeConnection();
                mTruckStatusPreference.set(Constants.STATUS_BLUETOOTH_DEVICE_NOT_MATCH);
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
                mIsBluetoothReadSuccessfully = true;
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
            EventBus.getDefault().post(
                    new BLEDataReceivedEvent(Constants.ULTRASOUND_VALUE_NOT_RECEIVED_ERROR,
                                        Constants.BATTERY_VALUE_NOT_RECEIVED_ERROR));
            return;
        }

        final String[] status = deviceStatus.split(Pattern.quote("||"));
        if(status.length != 2) {
            Timber.w("Bluetooth status is not in expected format: ultrasound_value||battery_level");
            EventBus.getDefault().post(
                    new BLEDataReceivedEvent(Constants.ULTRASOUND_VALUE_NOT_RECEIVED_ERROR,
                                        Constants.BATTERY_VALUE_NOT_RECEIVED_ERROR));
            return;
        }

        // status[0] - ultrasound distance
        // status[1] - arduino powerbank battery level
        final String ultrasoundStatus = status[0];
        final String arduinoBatteryStatus = status[1];
        int ultrasoundValue = Constants.ULTRASOUND_VALUE_UNKWOWN;
        int arduinoBatteryValue = Constants.BATTERY_VALUE_UNKWOWN;

        try {
            if(!ultrasoundStatus.equalsIgnoreCase("fail")) {
                ultrasoundValue = Integer.parseInt(status[0]);
            } else {
                ultrasoundValue = Constants.ULTRASOUND_VALUE_FAIL;
            }
        } catch(NumberFormatException ex) {
            Timber.w("Bluetooth ultrasound value is not a number");
            ultrasoundValue = Constants.ULTRASOUND_VALUE_PARSE_ERROR;
        }

        try {
            arduinoBatteryValue = Integer.parseInt(arduinoBatteryStatus);
        } catch(NumberFormatException ex) {
            Timber.w("Arduino battery level value is not a number");
            arduinoBatteryValue = Constants.BATTERY_VALUE_PARSE_ERROR;
        }

        EventBus.getDefault().post(new BLEDataReceivedEvent(ultrasoundValue, arduinoBatteryValue));
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

        if(!mIsBluetoothReadSuccessfully) {
            EventBus.getDefault().post(new BLEFailedToReadStatusEvent());
        }
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
