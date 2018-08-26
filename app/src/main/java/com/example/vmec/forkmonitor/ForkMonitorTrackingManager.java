package com.example.vmec.forkmonitor;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

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
    private BooleanPreference mIsBluetoothTrackingEnabled;
    private BooleanPreference mIsLocationTrackingEnabled;

    private Runnable mBluetoothReadCharacteristicRunnable = new Runnable() {
        @Override public void run() {
            Timber.d("Tracking interval fired - read bluetooth status");
            final int connectionStatus = mBluetoothHelper.getConnectionState();
            if(BluetoothHelper.STATE_DISCONNECTED == connectionStatus) {
                mBluetoothHelper.connect(mContext, Constants.BLUETOOTH_DEVICE_ADDRESS);
            } else {
                Timber.d("Read bluetooth status - device is not disconnected from previous session - status %d", connectionStatus);
                Timber.d("Read bluetooth status - Request DISCONNECT");
                mBluetoothHelper.disconnect();
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
        mHandler.postDelayed(mBluetoothReadCharacteristicRunnable, Constants.BLUETOOTH_CHARACTERISTIC_READ_INTERVAL_MS);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattConnectionDestroyedEvent event) {
        mHandler.removeCallbacks(mBluetoothReadCharacteristicRunnable);
        mHandler.postDelayed(mBluetoothReadCharacteristicRunnable, Constants.BLUETOOTH_CHARACTERISTIC_READ_INTERVAL_MS);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattServicesDiscoveredEvent event) {
        mBluetoothHelper.readCharacteristic(Constants.BLUETOOTH_FORK_MONITOR_SERVICE_UUID, Constants.BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattCharacteristicReadEvent event) {
        Timber.d("ForkMonitorTrackingManager characteristic received, DISCONNECT");
//        mBluetoothHelper.disconnect();

        final BluetoothGattCharacteristic characteristic = event.getCharacteristic();
        byte[] messageBytes = characteristic.getValue();
        String messageString = null;
        try {
            messageString = new String(messageBytes, "UTF-8");
            mLastCharacteristicPreference.set(messageString);
            Timber.d("Characteristic value: %s", messageString);
        } catch (UnsupportedEncodingException e) {
            mLastCharacteristicPreference.set(StringUtils.EMPTY_STRING);
            Timber.e("Unable to convert message bytes to string");
        }

        final int charaProp = characteristic.getProperties();

        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            Timber.d("");
            mBluetoothHelper.setCharacteristicNotification(characteristic, true);
        }

        EventBus.getDefault().post(new TrackingDataChangeEvent());

        //TODO: DO some action with characteristic
    }
}
