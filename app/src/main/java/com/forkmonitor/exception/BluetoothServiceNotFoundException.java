package com.forkmonitor.exception;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class BluetoothServiceNotFoundException extends RuntimeException {
    public BluetoothServiceNotFoundException() {
    }

    public BluetoothServiceNotFoundException(String message) {
        super(message);
    }
}
