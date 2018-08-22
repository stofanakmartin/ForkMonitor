package com.example.vmec.forkmonitor.event;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class GattCharacteristicReadEvent {
    private BluetoothGattCharacteristic characteristic;

    public GattCharacteristicReadEvent(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }
}
