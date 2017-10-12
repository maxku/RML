package com.maxku.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;


public class TimerService extends Service {

    private AlarmReceiver alarm;
    SensorManager sensorManager;
    Sensor sensorAccelerometer;
    SensorEventListener sensorEventListener;
    CountDownTimer timer;
    float Acc_0;
    float Acc_1;
    float Acc_2;
    boolean first = true;
    long lastUpdate = 0;

    static int FOUND_DELAY = 10000;
    static int CONST_DELAY = 3000;

    long delay = CONST_DELAY;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        alarm = new AlarmReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = this.getApplicationContext();
        alarm.RestartAlarm(context);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event)
            {
                long curTime = System.currentTimeMillis();

                if ((curTime - lastUpdate) > delay) {
                    lastUpdate = curTime;
                    delay = CONST_DELAY;

                    if (Math.abs(event.values[0] - Acc_0) > 2 || Math.abs(event.values[1] - Acc_1) > 2
                            || Math.abs(event.values[2] - Acc_2) > 2) {
                        Context context = getApplicationContext();
                        Acc_0 = event.values[0];
                        Acc_1 = event.values[1];
                        Acc_2 = event.values[2];
                        if (!first) {
                            delay = FOUND_DELAY;
                            alarm.RestartAlarm(context);
                        }
                        first = false;
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(sensorEventListener, sensorAccelerometer, sensorManager.SENSOR_DELAY_NORMAL);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Context context = this.getApplicationContext();
        alarm.CancelAlarm(context);
        sensorManager.unregisterListener(sensorEventListener);
        sensorEventListener = null;
    }
}

