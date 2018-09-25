package com.forkmonitor.event;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class GattCharacteristicReadEvent {
    private BluetoothGattCharacteristic characteristic;
    private String characteristicMsg;

    public GattCharacteristicReadEvent(BluetoothGattCharacteristic characteristic, final String characteristicMsg) {
        this.characteristic = characteristic;
        this.characteristicMsg = characteristicMsg;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public String getCharacteristicMsg() {
        return characteristicMsg;
    }
}
