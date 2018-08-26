package com.example.vmec.forkmonitor.event;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class GattCharacteristicChangeEvent {
    private BluetoothGattCharacteristic characteristic;

    public GattCharacteristicChangeEvent(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }
}
