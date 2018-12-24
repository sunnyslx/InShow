package com.idx.inshowapp.utils;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by stefan on 18-12-8.
 */

public class SensorUtil implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;

    public int orientation = 0;

    public SensorUtil(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
        // 参数三，检测的精准度
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);// SENSOR_DELAY_GAME
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final double G = 9.81;
        final double SQRT2 = 1.414213;
        if (event.sensor == null) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            if (z >= G / SQRT2) { //screen is more likely lying on the table
                if (x >= G / 2) {
                    orientation = 1;
                } else if (x <= -G / 2) {
                    orientation = 2;
                } else if (y <= -G / 2) {
                    orientation = 3;
                } else {
                    orientation = 0;
                }
            } else {
                if (x >= G / SQRT2) {
                    orientation = 1;
                } else if (x <= -G / SQRT2) {
                    orientation = 2;
                } else if (y <= -G / SQRT2) {
                    orientation = 3;
                } else {
                    orientation = 0;
                }
            }
        }
    }
}
