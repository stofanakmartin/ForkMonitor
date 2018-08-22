package com.example.vmec.forkmonitor.exception;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class BluetoothNotInitializedException extends RuntimeException {
    public BluetoothNotInitializedException() {
    }

    public BluetoothNotInitializedException(String message) {
        super(message);
    }
}
