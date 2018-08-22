package com.example.vmec.forkmonitor.utils;

import android.bluetooth.BluetoothAdapter;

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
}
