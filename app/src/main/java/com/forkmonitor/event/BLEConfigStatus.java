package com.forkmonitor.event;

/**
 * Created by Stofanak on 10/09/2018.
 */
public class BLEConfigStatus {
    private boolean isSuccessfull;

    public BLEConfigStatus(boolean isSuccessfull) {
        this.isSuccessfull = isSuccessfull;
    }

    public boolean isSuccessfull() {
        return isSuccessfull;
    }
}
