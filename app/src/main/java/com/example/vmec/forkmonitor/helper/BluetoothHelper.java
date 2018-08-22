package com.example.vmec.forkmonitor.helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.example.vmec.forkmonitor.event.GattCharacteristicReadEvent;
import com.example.vmec.forkmonitor.event.GattConnectedEvent;
import com.example.vmec.forkmonitor.event.GattDisconnectedEvent;
import com.example.vmec.forkmonitor.event.GattServicesDiscoveredEvent;
import com.example.vmec.forkmonitor.event.LocationPublishEvent;
import com.example.vmec.forkmonitor.exception.BluetoothCharactericticNotFoundException;
import com.example.vmec.forkmonitor.exception.BluetoothNotInitializedException;
import com.example.vmec.forkmonitor.exception.BluetoothServiceNotFoundException;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

import timber.log.Timber;

/**
 * Created by Stofanak on 19/08/2018.
 *
 * TODO: Check invalid bluetooth address
 */
public class BluetoothHelper {

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                EventBus.getDefault().post(new GattConnectedEvent());

                Timber.i("BluetoothHelper connected to GATT server.");

                Timber.d("BluetoothHelper service discover request");
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                mBluetoothGatt.close();
                mBluetoothDeviceAddress = null;
                mBluetoothGatt = null;
                mConnectionState = STATE_DISCONNECTED;
                EventBus.getDefault().post(new GattDisconnectedEvent());
                Timber.i("BluetoothHelper disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                EventBus.getDefault().post(new GattServicesDiscoveredEvent());
                Timber.i("BluetoothHelper service discover callback.");
            } else {
                Timber.w("BluetoothHelper onServicesDiscovered status received: %s", status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                EventBus.getDefault().post(new GattCharacteristicReadEvent(characteristic));
                Timber.d("BluetoothHelper Gatt characteristic read callback");
            } else {
                Timber.d("BluetoothHelper Gatt characteristic read failed");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Timber.d("BluetoothHelper characteristic changed");
        }
    };

    public boolean initialize(final Context context) {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Timber.i("BluetoothHelper initialize.");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Timber.e("Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Timber.e("Unable to obtain a BluetoothAdapter.");
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
            Timber.w("BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Timber.d("Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Timber.w("Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
        Timber.d("Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Timber.w("BluetoothAdapter not initialized");
            return;
        }
        Timber.d("BluetoothHelper - Disconnect connection.");
        mBluetoothGatt.disconnect();
    }

    public void destroyConnection() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Timber.w("BluetoothAdapter not initialized");
            return;
        }
        Timber.d("BluetoothHelper Destroy connection.");
        mBluetoothGatt.disconnect();
//        mBluetoothGatt.close();
//        mBluetoothDeviceAddress = null;
//        mBluetoothGatt = null;
//        mConnectionState = STATE_DISCONNECTED;
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
            Timber.w("BluetoothAdapter not initialized");
            throw new BluetoothNotInitializedException("BluetoothAdapter not initialized");
        }

        Timber.d("BluetoothHelper Read characteristic request.");

        /*check if the service is available on the device*/
//        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("00001110-0000-1000-8000-00805f9b34fb"));
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(serviceUUID));
        if(mCustomService == null){
            final String msg = String.format("Custom BLE Service: %s not found", serviceUUID);
            Timber.w(msg);
            throw new BluetoothServiceNotFoundException(msg);
        }
        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString(characteristicUUID));
        if(!mBluetoothGatt.readCharacteristic(mReadCharacteristic)){
            final String msg = String.format("Failed to read characteristic: %s ", characteristicUUID);
            Timber.w("Failed to read characteristic");
            throw new BluetoothServiceNotFoundException(msg);
        }
    }

    public int getConnectionState() {
        return mConnectionState;
    }
}
