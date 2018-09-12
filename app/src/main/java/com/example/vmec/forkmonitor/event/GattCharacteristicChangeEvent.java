package com.example.vmec.forkmonitor.event;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class GattCharacteristicChangeEvent {
    private BluetoothGattCharacteristic characteristic;
    private String characteristicMsg;

    public GattCharacteristicChangeEvent(final BluetoothGattCharacteristic characteristic, final String msg) {
        this.characteristic = characteristic;
        this.characteristicMsg = msg;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public String getCharacteristicMsg() {
        return characteristicMsg;
    }
}
