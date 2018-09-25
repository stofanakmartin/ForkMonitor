package com.forkmonitor.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;

import java.io.UnsupportedEncodingException;

import timber.log.Timber;

/**
 * Created by Stofanak on 18/08/2018.
 */
public class BluetoothUtils {


    public static boolean isBluetoothOn() {
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                return false;
            }
        }
        return true;
    }

    public static String getCharacteristicStringValue(final BluetoothGattCharacteristic characteristic) {
        byte[] messageBytes = characteristic.getValue();
        String messageString = null;
        try {
            messageString = new String(messageBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Timber.e("Unable to convert message bytes to string");
        }
        return messageString;
    }
}
