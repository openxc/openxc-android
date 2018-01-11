package com.openxc.sources;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.common.base.MoreObjects;
import com.openxc.messages.SimpleVehicleMessage;

import java.util.Arrays;
import java.util.List;

/**
 * Generate location measurements based on native GPS updates.
 *
 * This source listens for GPS location updates from the built-in Android location
 * framework and passes them to the OpenXC vehicle measurement framework as if
 * they originated from the vehicle. This source is useful to seamlessly use
 * location in an application regardless of it the vehicle has built-in GPS.
 *
 * The ACCESS_FINE_LOCATION permission is required to use this source.
 */
public class PhoneSensorSource extends ContextualVehicleDataSource
        implements SensorEventListener, LocationListener, Runnable {
    private final static String TAG = "PhoneSensorSource";
    private final static int DEFAULT_INTERVAL = 5000;

    private Looper mLooper;

    private static LocationManager locationManager;


    private static SensorManager sensorService;
    private Sensor sensor;
    private float ax,ay,az;
    private float rx,ry,rz;
    private float gx,gy,gz;
    private float mx,my,mz;
    private float light;
    private float proximity;
    private float humidity;
    private float pressure;
    private float temperature;
    private double altitude, heading, speed;
    private Context thecontext;

    String devmodel,devname,osver,headphonesConn;

    public PhoneSensorSource(SourceCallback callback, Context context) {
        super(callback, context);

        thecontext = context;

        devmodel = Build.MODEL;
        devname = Build.PRODUCT;
        osver = Build.VERSION.RELEASE;

        System.out.println("MODEL: "+android.os.Build.MODEL
                +"\nDEVICE: "+android.os.Build.DEVICE
                +"\nBRAND: "+android.os.Build.BRAND
                +"\nDISPLAY: "+android.os.Build.DISPLAY
                +"\nBOARD: "+android.os.Build.BOARD
                +"\nHOST: "+android.os.Build.HOST
                +"\nMANUFACTURER: "+android.os.Build.MANUFACTURER
                +"\nPRODUCT: "+android.os.Build.PRODUCT);

        PackageManager PM= context.getPackageManager();
        boolean gyro = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        System.out.println("gyro allowed:"+gyro);
        sensorService = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);


        List<Sensor> listSensor = sensorService.getSensorList(Sensor.TYPE_ALL);
        for(int i=0; i<listSensor.size(); i++)
        {
            System.out.println("Sensor : " + listSensor.get(i).getName());
        }

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);


    }

    public PhoneSensorSource(Context context) {
        this(null, context);
    }

    @Override
    public void run() {
        Looper.prepare();

        Log.w(TAG, "Phone Sensor Looper started");

        try {
            sensorService.registerListener(this,
                    sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
            sensorService.registerListener(this,
                    sensorService.getDefaultSensor(Sensor.TYPE_LIGHT),
                    SensorManager.SENSOR_DELAY_NORMAL);
            sensorService.registerListener(this,
                    sensorService.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                    SensorManager.SENSOR_DELAY_NORMAL);
            sensorService.registerListener(this,
                    sensorService.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                    SensorManager.SENSOR_DELAY_NORMAL);
            sensorService.registerListener(this,
                    sensorService.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    SensorManager.SENSOR_DELAY_NORMAL);
            sensorService.registerListener(this,
                    sensorService.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                    SensorManager.SENSOR_DELAY_NORMAL);
            sensorService.registerListener(this,
                    sensorService.getDefaultSensor(Sensor.TYPE_PRESSURE),
                    SensorManager.SENSOR_DELAY_NORMAL);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                sensorService.registerListener(this,
                        sensorService.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
                        SensorManager.SENSOR_DELAY_NORMAL);
                sensorService.registerListener(this,
                        sensorService.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY),
                        SensorManager.SENSOR_DELAY_NORMAL);
            }

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, 10, this);

        } catch(IllegalArgumentException e) {
            Log.w(TAG, "Problem registering Sensor");
        }

        mLooper = Looper.myLooper();
        Looper.loop();
    }

    @Override
    public void stop() {
        super.stop();
        System.out.println("Phone Sensor service stopped");
        onPipelineDeactivated();
        sensorService.unregisterListener(this);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //System.out.println("Sensor update:"+event.sensor.getName());
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float[] accelValues = event.values;
            ax = accelValues[0];
            ay = accelValues[1];
            az = accelValues[2];

            handleMessage(new SimpleVehicleMessage("phone_accelerometer",
                    Arrays.toString(accelValues)));

        }
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

            float[] rotationValues = event.values;
            rx = rotationValues[0];
            ry = rotationValues[1];
            rz = rotationValues[2];

            handleMessage(new SimpleVehicleMessage("phone_rotation",
                    Arrays.toString(rotationValues)));
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            float[] gyroValues = event.values;
            gx = gyroValues[0];
            gy = gyroValues[1];
            gz = gyroValues[2];

            handleMessage(new SimpleVehicleMessage("phone_gyroscope", Arrays.toString(gyroValues)));
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            float[] magnetoValues = event.values;
            mx = magnetoValues[0];
            my = magnetoValues[1];
            mz = magnetoValues[2];

            handleMessage(new SimpleVehicleMessage("phone_magnetometer", Arrays.toString(magnetoValues)));
        }
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            //   System.out.println("new light: " + event.values[0]);
            light = event.values[0];
            handleMessage(new SimpleVehicleMessage("phone_light_level", light));
        }
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            //   System.out.println("new light: " + event.values[0]);
            proximity = event.values[0];
            handleMessage(new SimpleVehicleMessage("phone_proximity", proximity));
        }
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            //   System.out.println("new light: " + event.values[0]);
            pressure = event.values[0];
            handleMessage(new SimpleVehicleMessage("phone_atmospheric_pressure", pressure));
        }
        if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            //   System.out.println("new light: " + event.values[0]);
            humidity = event.values[0];
            handleMessage(new SimpleVehicleMessage("phone_relative_humidity", humidity));
        }
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            //   System.out.println("new light: " + event.values[0]);
            temperature = event.values[0];
            handleMessage(new SimpleVehicleMessage("phone_ambient_temperature", temperature));
        }

        AudioManager am1 = (AudioManager)thecontext.getSystemService(Context.AUDIO_SERVICE);
        if (am1.isWiredHeadsetOn()) {
            headphonesConn="true";
            handleMessage(new SimpleVehicleMessage("phone_headphones_connected", headphonesConn));
        } else {
            headphonesConn="false";
            handleMessage(new SimpleVehicleMessage("phone_headphones_connected", headphonesConn));
        }


    }

    @Override
    public void onStatusChanged(String provider, int status,
            Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public boolean isConnected() {

        return (sensorService != null);
    }

    @Override
    public void onPipelineActivated() {
        Log.i(TAG, "Enabling phone sensor collection");
        new Thread(this).start();
    }

    @Override
    public void onPipelineDeactivated() {
        Log.i(TAG, "Disabled phone sensor collection");
        if(mLooper != null) {
            mLooper.quit();
        }
        locationManager.removeUpdates(this);
        sensorService.unregisterListener(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("updateInterval",DEFAULT_INTERVAL)
            .toString();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        System.out.println("accuracy changed");
    }

    @Override
    public void onLocationChanged(Location location) {
        altitude = location.getAltitude();
        heading = location.getBearing();
        speed = location.getSpeed();
        handleMessage(new SimpleVehicleMessage("phone_altitude", altitude));
        handleMessage(new SimpleVehicleMessage("phone_heading", heading));
        handleMessage(new SimpleVehicleMessage("phone_speed", speed));
    }
}
