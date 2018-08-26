package com.example.vmec.forkmonitor;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;

import com.example.vmec.forkmonitor.event.GattCharacteristicChangeEvent;
import com.example.vmec.forkmonitor.event.GattCharacteristicReadEvent;
import com.example.vmec.forkmonitor.event.GattConnectionDestroyedEvent;
import com.example.vmec.forkmonitor.event.GattDisconnectedEvent;
import com.example.vmec.forkmonitor.event.GattServicesDiscoveredEvent;
import com.example.vmec.forkmonitor.event.TrackingDataChangeEvent;
import com.example.vmec.forkmonitor.helper.BluetoothHelper;
import com.example.vmec.forkmonitor.preference.BooleanPreference;
import com.example.vmec.forkmonitor.preference.StringPreference;
import com.example.vmec.forkmonitor.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class ForkMonitorTrackingManager {

    //TODO: REMOVE reference to context
    private Context mContext;
    private Handler mHandler;
    private BluetoothHelper mBluetoothHelper;
    private StringPreference mLastCharacteristicPreference;
    private StringPreference mBluetoothDeviceNamePreference;
    private BooleanPreference mIsBluetoothTrackingEnabled;
    private BooleanPreference mIsLocationTrackingEnabled;
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
                mBluetoothHelper.writeToCharacteristic(mDeviceStatusCharacteristic, "start");
            }
        }
    };

    public ForkMonitorTrackingManager() {
        mHandler = new Handler();
        mBluetoothHelper = new BluetoothHelper();
    }

    public boolean initialize(final Context context) {
        mContext = context;
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mLastCharacteristicPreference = new StringPreference(sp, Constants.PREFERENCE_LAST_CHARACTERISTIC_MSG, StringUtils.EMPTY_STRING);
        mIsBluetoothTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_BLUETOOTH_TRACKING_ENABLED, false);
        mIsLocationTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_LOCATION_TRACKING_ENABLED, false);
        mBluetoothDeviceNamePreference = new StringPreference(sp, Constants.PREFERENCE_BLUETOOTH_DEVICE_NAME, StringUtils.EMPTY_STRING);

        EventBus.getDefault().register(this);
        final boolean bluetoothInitStatus = mBluetoothHelper.initialize(context);

        if(bluetoothInitStatus) {
            Timber.d("ForkMonitorTrackingManager BLUETOOTH INITIALIZED SUCCESSFULLY");
        } else {
            Timber.d("ForkMonitorTrackingManager BLUETOOTH FAILED TO INITIALIZE");
        }

        return bluetoothInitStatus;
    }

    public void startTracking(final Context context) {
        mBluetoothHelper.connect(context, Constants.BLUETOOTH_DEVICE_ADDRESS);
        mIsBluetoothTrackingEnabled.set(true);
    }

    public void stopTracking() {
        mHandler.removeCallbacks(mBluetoothReadCharacteristicRunnable);
        mBluetoothHelper.disconnect();
        mIsBluetoothTrackingEnabled.set(false);
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(GattConnectedEvent event) {
//        mBluetoothHelper.readCharacteristic(Constants.BLUETOOTH_FORK_MONITOR_SERVICE_UUID, Constants.BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID);
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattDisconnectedEvent event) {
        mHandler.removeCallbacks(mBluetoothReadCharacteristicRunnable);
        mDeviceStatusCharacteristic = null;
        setNextCharacteristicReadingAction();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattConnectionDestroyedEvent event) {
        mHandler.removeCallbacks(mBluetoothReadCharacteristicRunnable);
        mDeviceStatusCharacteristic = null;
        setNextCharacteristicReadingAction();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattServicesDiscoveredEvent event) {
        // Read device name characteristic first
        mBluetoothHelper.readCharacteristic(Constants.BLUETOOTH_DEVICE_NAME_SERVICE_UUID, Constants.BLUETOOTH_DEVICE_NAME_CHARACTERISTIC_UUID);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattCharacteristicReadEvent event) {
        Timber.d("Characteristic read received");

        final BluetoothGattCharacteristic characteristic = event.getCharacteristic();

        if(characteristic.getUuid().toString().equals(Constants.BLUETOOTH_DEVICE_NAME_CHARACTERISTIC_UUID)) {
            final String deviceName = getCharacteristicStringValue(characteristic);
            mBluetoothDeviceNamePreference.set(deviceName);
            if(deviceName.equals(Constants.BLUETOOTH_DEVICE_NAME)) {
                mBluetoothHelper.readCharacteristic(Constants.BLUETOOTH_FORK_MONITOR_SERVICE_UUID, Constants.BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID);
            } else {
                Timber.e("Bluetooth device name does not match. Current device name: %s", deviceName);
            }
        } else if(characteristic.getUuid().toString().equals(Constants.BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID)) {
            final int characteristicProperties = characteristic.getProperties();

            if ((characteristicProperties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0
                    && (characteristicProperties | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                mDeviceStatusCharacteristic = characteristic;
                mBluetoothHelper.setCharacteristicNotification(characteristic, true);
                mBluetoothHelper.writeToCharacteristic(characteristic, "start");
            } else {
                Timber.e("Bluetooth characteristic does not support NOTIFICATION or WRITE feature");
            }
        }
//        mBluetoothHelper.disconnect();
        EventBus.getDefault().post(new TrackingDataChangeEvent());

        //TODO: DO some action with characteristic
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattCharacteristicChangeEvent event) {
        Timber.d("Characteristic change received");

        final BluetoothGattCharacteristic characteristic = event.getCharacteristic();
        final String charMessage = getCharacteristicStringValue2(characteristic);

        if (TextUtils.isEmpty(charMessage)) {
            mLastCharacteristicPreference.set(StringUtils.EMPTY_STRING);
            Timber.w("Received empty message or no data");
        } else {
            if(charMessage.toLowerCase().contains("end")) {
                final String finalMessage = mTmpCharacteristicMsgBuffer + charMessage;
                mTmpCharacteristicMsgBuffer = StringUtils.EMPTY_STRING;
                mLastCharacteristicPreference.set(finalMessage);

                mBluetoothHelper.writeToCharacteristic(characteristic, "end");
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

        //TODO: DO some action with characteristic
    }

    private void setNextCharacteristicReadingAction() {
        mHandler.postDelayed(mBluetoothReadCharacteristicRunnable, Constants.BLUETOOTH_CHARACTERISTIC_READ_INTERVAL_MS);
    }

    // TODO: MOVE TO UTILS
    public String getCharacteristicStringValue(final BluetoothGattCharacteristic characteristic) {
        byte[] messageBytes = characteristic.getValue();
        String messageString = null;
        try {
            messageString = new String(messageBytes, "UTF-8");
            Timber.d("Characteristic value: %s", messageString);
        } catch (UnsupportedEncodingException e) {
            Timber.e("Unable to convert message bytes to string");
        }
        return messageString;
    }

    // TODO: MOVE TO UTILS
    public String getCharacteristicStringValue2(final BluetoothGattCharacteristic characteristic) {
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", byteChar));
            }
            return stringBuilder.toString();
        }
        return null;
    }
}
