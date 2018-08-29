package com.example.vmec.forkmonitor.event;

/**
 * Created by Stofanak on 29/08/2018.
 */
public class TruckLoadedStatusChangeEvent {
    private int truckLoadedStatus;

    public TruckLoadedStatusChangeEvent(int truckLoadedStatus) {
        this.truckLoadedStatus = truckLoadedStatus;
    }

    public int getTruckLoadedStatus() {
        return truckLoadedStatus;
    }
}
