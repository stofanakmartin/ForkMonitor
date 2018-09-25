package com.forkmonitor.event;

/**
 * Created by Stofanak on 29/08/2018.
 */
public class TruckLoadedStateChangeEvent {
    private int truckLoadedState;

    public TruckLoadedStateChangeEvent(int truckLoadedStatus) {
        this.truckLoadedState = truckLoadedStatus;
    }

    public int getTruckLoadedState() {
        return truckLoadedState;
    }
}
