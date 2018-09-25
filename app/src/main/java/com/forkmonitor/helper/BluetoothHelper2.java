package com.forkmonitor.helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.forkmonitor.Constants;
import com.forkmonitor.event.GattCharacteristicChangeEvent;
import com.forkmonitor.event.GattCharacteristicNotificationConfigEvent;
import com.forkmonitor.event.GattCharacteristicReadEvent;
import com.forkmonitor.event.GattCharacteristicWriteEvent;
import com.forkmonitor.event.GattConnectedEvent;
import com.forkmonitor.event.GattDisconnectedEvent;
import com.forkmonitor.event.GattServicesDiscoveredEvent;
import com.forkmonitor.exception.BluetoothCharactericticNotFoundException;
import com.forkmonitor.exception.BluetoothNotInitializedException;
import com.forkmonitor.exception.BluetoothServiceNotFoundException;
import com.forkmonitor.utils.BluetoothUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Stofanak on 19/08/2018.
 *
 * TODO: Check invalid bluetooth address
 */
public class BluetoothHelper2 {

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private Handler mHandler;

    private Runnable mDisconnectRunnable = new Runnable() {
        @Override public void run() {
            Timber.d("BLE - request timeout - CLOSE connection");
            closeConnection();
        }
    };

    private Runnable mCloseConnectionTimeoutRunnable = new Runnable() {
        @Override public void run() {
            Timber.d("BLE - Close connection timeout DESTROY GATT CLIENT");
            onConnectionClosed();
        }
    };

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            clearRequestTimeoutAction();
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                if(mBluetoothGatt == null) {
                    Timber.d("BLE - assign BluetoothGatt from callback");
                    mBluetoothGatt = gatt;
                }
                Timber.i("BLE - Connected");
                EventBus.getDefault().post(new GattConnectedEvent());
                discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Timber.i("BLE - Disconnected");
                clearRequestTimeoutAction();
                clearCloseConnectionTimeoutAction();
                onConnectionClosed();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            clearRequestTimeoutAction();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Timber.i("BLE - Discover services callback - success");
                EventBus.getDefault().post(new GattServicesDiscoveredEvent());
            } else {
                Timber.w("BLE - Discover services callback - failed: %d", status);
                closeConnection();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            clearRequestTimeoutAction();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                final String charMessage = BluetoothUtils.getCharacteristicStringValue(characteristic);
                Timber.d("BLE - Characteristic read: %s", charMessage);
                EventBus.getDefault().post(new GattCharacteristicReadEvent(characteristic, charMessage));
            } else {
                Timber.w("BLE - Characteristic read callback failed");
                closeConnection();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            clearRequestTimeoutAction();
            final String charMessage = BluetoothUtils.getCharacteristicStringValue(characteristic);
            Timber.d("BLE - Characteristic change: %s", charMessage);
            EventBus.getDefault().post(new GattCharacteristicChangeEvent(characteristic, charMessage));
        }

        @Override public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            clearRequestTimeoutAction();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Timber.d("Characteristic write callback success");
                EventBus.getDefault().post(new GattCharacteristicWriteEvent(characteristic, status));
            } else {
                Timber.w("BLE - Characteristic write callback failed");
                closeConnection();
            }
        }

        @Override public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            clearRequestTimeoutAction();
            Timber.d("BLE - Descriptor write callback - status: %d", status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Characteristic notification configuration callback
                if (descriptor.getUuid().toString().equals(Constants.BLUETOOTH_CLIENT_CHARACTERISTIC_CONFIG_UUID)) {
                    Timber.d("BLE - Set characteristic notification callback - success");
                    EventBus.getDefault().post(new GattCharacteristicNotificationConfigEvent(status));
                }
            } else {
                Timber.w("BLE - Set characteristic notification callback failed");
                closeConnection();
            }
        }
    };

    /**
     * Contructor
     */
    public BluetoothHelper2() {
        this.mHandler = new Handler();
    }

    public boolean initialize(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Timber.i("BLE - Initialize.");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Timber.e("BLE - Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Timber.e("BLE - Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final Context context, final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Timber.w("BLE - BluetoothAdapter not initialized or unspecified address");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Timber.w("BLE - Device not found.  Unable to connect");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        Timber.d("BLE - Connect request");
        mConnectionState = STATE_CONNECTING;
        postRequestTimeoutAction();
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
        Timber.w("BLE - connect - mBluetoothGatt is initialized: %s", String.valueOf(mBluetoothGatt != null));
        return true;
    }

    /**
     * Call bluetooth LE service discovery request
     */
    private void discoverServices() {
        if (mBluetoothAdapter == null) {
            Timber.w("BLE - discoverServices - BluetoothAdapter not initialized");
            return;
        }
        if (mBluetoothGatt == null) {
            Timber.w("BLE - discoverServices - mBluetoothGatt not initialized");
            return;
        }
        Timber.d("BLE - Discover services request");
        postRequestTimeoutAction();
        mBluetoothGatt.discoverServices();
    }



    public void closeConnection() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Timber.w("BLE - closeConnection - BluetoothAdapter not initialized");
            return;
        }
        Timber.d("BLE - close connection.");
        clearRequestTimeoutAction();
        postCloseConnectionTimeoutAction();
        mBluetoothGatt.disconnect();
    }

    private void onConnectionClosed() {
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mConnectionState = STATE_DISCONNECTED;
        EventBus.getDefault().post(new GattDisconnectedEvent());
    }

    /**
     * Request a read on a given characteristic specified by service UUID and characterictic UUID.
     * The read result is reported asynchronously
     * through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param serviceUUID The service to read characteristic from.
     * @param characteristicUUID The characteristic to read from.
     */
    public void readCharacteristic(final String serviceUUID, final String characteristicUUID)
            throws BluetoothNotInitializedException, BluetoothServiceNotFoundException, BluetoothCharactericticNotFoundException {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Timber.w("BLE - readCharacteristic - BluetoothAdapter not initialized");
            throw new BluetoothNotInitializedException("BluetoothAdapter not initialized");
        }

        Timber.d("BLE - read characteristic request %s", characteristicUUID);
        clearRequestTimeoutAction();

        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(serviceUUID));
        if(mCustomService == null){
            final String msg = String.format("BLE - custom service: %s not found", serviceUUID);
            Timber.w(msg);
            closeConnection();
            throw new BluetoothServiceNotFoundException(msg);
        }

        postRequestTimeoutAction();

        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString(characteristicUUID));
        if(!mBluetoothGatt.readCharacteristic(mReadCharacteristic)) {
            final String msg = String.format("BLE - Failed to read characteristic: %s ", characteristicUUID);
            Timber.w(msg);
            closeConnection();
            throw new BluetoothServiceNotFoundException(msg);
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        Timber.d("BLE - Register characteristic for notifications");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Timber.w("BLE - BluetoothAdapter not initialized");
            return;
        }
        postRequestTimeoutAction();
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(Constants.BLUETOOTH_CLIENT_CHARACTERISTIC_CONFIG_UUID));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    /**
     * Writes value to specified characteristic
     *
     * @param characteristic Characteristic to act on.
     * @param value value to write to characteristic
     */
    public void writeToCharacteristic(BluetoothGattCharacteristic characteristic, final String value) {
        Timber.d("BLE - Write value to characteristic: %s", value);
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Timber.w("BLE - writeToCharacteristic - BluetoothAdapter not initialized");
            return;
        }

        postRequestTimeoutAction();
        characteristic.setValue(value);
        final boolean writeResult = mBluetoothGatt.writeCharacteristic(characteristic);

        Timber.d("BLE - Characteristic write request - value: '%s' - result: %s", value, String.valueOf(writeResult));
    }

    public void requestConnectionDisconnectAfterTimeout() {
        postRequestTimeoutAction();
    }

    public int getConnectionState() {
        return mConnectionState;
    }

    private void postRequestTimeoutAction() {
        mHandler.postDelayed(mDisconnectRunnable, Constants.BLUETOOTH_MAX_REQUEST_TIMEOUT_MS);
    }

    private void clearRequestTimeoutAction() {
        mHandler.removeCallbacks(mDisconnectRunnable);
    }

    private void postCloseConnectionTimeoutAction() {
        mHandler.postDelayed(mCloseConnectionTimeoutRunnable, Constants.BLUETOOTH_MAX_REQUEST_TIMEOUT_MS);
    }

    private void clearCloseConnectionTimeoutAction() {
        mHandler.removeCallbacks(mCloseConnectionTimeoutRunnable);
    }
}
