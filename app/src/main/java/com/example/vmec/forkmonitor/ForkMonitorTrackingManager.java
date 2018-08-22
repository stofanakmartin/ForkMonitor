package com.example.vmec.forkmonitor;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.example.vmec.forkmonitor.event.GattCharacteristicReadEvent;
import com.example.vmec.forkmonitor.event.GattConnectedEvent;
import com.example.vmec.forkmonitor.event.GattDisconnectedEvent;
import com.example.vmec.forkmonitor.event.GattServicesDiscoveredEvent;
import com.example.vmec.forkmonitor.event.LocationPublishEvent;
import com.example.vmec.forkmonitor.helper.BluetoothHelper;
import com.example.vmec.forkmonitor.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

import timber.log.Timber;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class ForkMonitorTrackingManager {

    //TODO: REMOVE reference to context
    private Context mContext;
    private Handler mHandler;
    private BluetoothHelper mBluetoothHelper;
    private int mUnsuccessfullStatusReadCounter = 0;
    private Runnable mBluetoothReadCharacteristicRunnable = new Runnable() {
        @Override public void run() {
            final int connectionStatus = mBluetoothHelper.getConnectionState();
            if(BluetoothHelper.STATE_DISCONNECTED == connectionStatus) {
                Timber.d("ForkMonitorTrackingManager - read bluetooth status - trying to reconnect");
                mBluetoothHelper.connect(mContext, Constants.BLUETOOTH_DEVICE_ADDRESS);
            } else {
                Timber.d("ForkMonitorTrackingManager - read bluetooth status - device is not disconnected from previous session - status %d", connectionStatus);
                if(3 == mUnsuccessfullStatusReadCounter) {
                    mBluetoothHelper.destroyConnection();
                    mUnsuccessfullStatusReadCounter = 0;
                } else {
                    mUnsuccessfullStatusReadCounter++;
                }
            }

            mHandler.postDelayed(this, Constants.BLUETOOTH_CHARACTERISTIC_READ_INTERVAL_MS);
        }
    };

    public ForkMonitorTrackingManager() {
        mHandler = new Handler();
        mBluetoothHelper = new BluetoothHelper();
    }

    public boolean initialize(final Context context) {
        mContext = context;
        EventBus.getDefault().register(this);
        final boolean bluetoothInitStatus = mBluetoothHelper.initialize(context);

        if(bluetoothInitStatus) {
            Timber.d("ForkMonitorTrackingManager BLUETOOTH INITIALIZED SUCCESSFULLY");
        } else {
            Timber.d("ForkMonitorTrackingManager BLUETOOTH FAILED TO INITIALIZE");
        }

        return bluetoothInitStatus;
    }

    public void destroy() {
        mBluetoothHelper.destroyConnection();
    }

    public void startTracking(final Context context) {
        mBluetoothHelper.connect(context, Constants.BLUETOOTH_DEVICE_ADDRESS);
        mHandler.postDelayed(mBluetoothReadCharacteristicRunnable, Constants.BLUETOOTH_CHARACTERISTIC_READ_INTERVAL_MS);
    }

    public void stopTracking() {
        mHandler.removeCallbacks(mBluetoothReadCharacteristicRunnable);
        mBluetoothHelper.disconnect();
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(GattConnectedEvent event) {
//        mBluetoothHelper.readCharacteristic(Constants.BLUETOOTH_FORK_MONITOR_SERVICE_UUID, Constants.BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID);
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(GattDisconnectedEvent event) {
//        mHandler.removeCallbacks(mBluetoothReadCharacteristicRunnable);
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattServicesDiscoveredEvent event) {
        mBluetoothHelper.readCharacteristic(Constants.BLUETOOTH_FORK_MONITOR_SERVICE_UUID, Constants.BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GattCharacteristicReadEvent event) {
        Timber.d("ForkMonitorTrackingManager characteristic received, DISCONNECT");
//        mBluetoothHelper.disconnect();
        mBluetoothHelper.destroyConnection();
        mUnsuccessfullStatusReadCounter = 0;
        // Set next characteristic reading action
//        mHandler.postDelayed(mBluetoothReadCharacteristicRunnable, Constants.BLUETOOTH_CHARACTERISTIC_READ_INTERVAL_MS);

        //TODO: DO some action with characteristic
    }
}
