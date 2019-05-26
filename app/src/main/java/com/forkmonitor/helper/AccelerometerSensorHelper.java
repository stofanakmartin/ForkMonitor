package com.forkmonitor.helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import timber.log.Timber;

/**
 * Created by Stofanak on 29/04/2019.
 */
public class AccelerometerSensorHelper implements SensorEventListener {

    private static int NUMBER_OF_MEASUREMENTS = 65;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private float[][] measurementBuffer;
    private int currentMeasureIndex = 0;

    public AccelerometerSensorHelper(final Context context) {
        this.sensorManager = sensorManager;
        this.measurementBuffer = new float[NUMBER_OF_MEASUREMENTS][3];
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        for(int i = 0; i < NUMBER_OF_MEASUREMENTS; i++) {
            measurementBuffer[i][0] = 0;
            measurementBuffer[i][1] = 0;
            measurementBuffer[i][2] = 0;
        }
    }

    public void startTracking() {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopTracking() {
        sensorManager.unregisterListener(this, accelerometerSensor);
    }

    public float[] getAveragedMeasurement() {
        return calculateAverageFromBuffer();
    }

    @Override public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) {
            return;
        }

//        Timber.d("%f %f %f", event.values[0],event.values[1], event.values[2]);

        measurementBuffer[currentMeasureIndex][0] = event.values[0];
        measurementBuffer[currentMeasureIndex][1] = event.values[1];
        measurementBuffer[currentMeasureIndex][2] = event.values[2];
        currentMeasureIndex = (currentMeasureIndex + 1) % NUMBER_OF_MEASUREMENTS;
//        if (currentMeasureIndex == 0) {
//            Timber.d("%d accelerometer measurements complete", NUMBER_OF_MEASUREMENTS);
//        }
    }

    private float[] calculateAverageFromBuffer() {
        final float[] cumulativeValue = new float[3];
        final float[] averagedResult = new float[3];
        for (int i = 0; i < measurementBuffer.length; i++) {
            cumulativeValue[0] += Math.abs(measurementBuffer[i][0]);
            cumulativeValue[1] += Math.abs(measurementBuffer[i][1]);
            cumulativeValue[2] += Math.abs(measurementBuffer[i][2]);
        }
        averagedResult[0] = cumulativeValue[0]/NUMBER_OF_MEASUREMENTS;
        averagedResult[1] = cumulativeValue[1]/NUMBER_OF_MEASUREMENTS;
        averagedResult[2] = cumulativeValue[2]/NUMBER_OF_MEASUREMENTS;

        Timber.d("Accelerometer - calculated average: %f, %f, %f", averagedResult[0], averagedResult[1], averagedResult[2]);

        return averagedResult;
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // NOT IMPLEMENTED
    }
}
