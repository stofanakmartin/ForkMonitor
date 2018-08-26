package com.example.vmec.forkmonitor.event;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by Stofanak on 26/08/2018.
 */
public class GattCharacteristicWriteEvent {
    private BluetoothGattCharacteristic characteristic;
    private int status;

    public GattCharacteristicWriteEvent(BluetoothGattCharacteristic characteristic, int status) {
        this.characteristic = characteristic;
        this.status = status;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public int getStatus() {
        return status;
    }
}
