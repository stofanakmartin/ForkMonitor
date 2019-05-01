package com.forkmonitor.helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;

/**
 * Created by Stofanak on 29/04/2019.
 */
public class AccelerometerSensorHelper implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor stationarySensor;
    private TriggerEventListener triggerEventListener;

    public AccelerometerSensorHelper(final Context context) {
        this.sensorManager = sensorManager;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        stationarySensor = sensorManager.getDefaultSensor(Sensor.TYPE_STATIONARY_DETECT);
//
//        triggerEventListener = new TriggerEventListener() {
//            @Override
//            public void onTrigger(TriggerEvent event) {
//                // Do work
//            }
//        };

//        sensorManager.requestTriggerSensor(triggerEventListener, sensor);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    }

//    public String getAccelerometerReading() {
//        sensor.
//    }

    @Override public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        //event.values[0];
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // NOT IMPLEMENTED
    }
}
