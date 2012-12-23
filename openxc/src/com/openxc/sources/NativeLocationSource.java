package com.openxc.sources;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.remote.RawMeasurement;

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
public class NativeLocationSource extends ContextualVehicleDataSource
        implements LocationListener, Runnable {
    private final static String TAG = "NativeLocationSource";
    private final static int NATIVE_GPS_UPDATE_INTERVAL = 5000;

    private LocationManager mLocationManager;

    public NativeLocationSource(SourceCallback callback, Context context) {
        super(callback, context);
        mLocationManager = (LocationManager) getContext().getSystemService(
                    Context.LOCATION_SERVICE);
        new Thread(this).start();
    }

    public NativeLocationSource(Context context) {
        this(null, context);
    }

    public void run() {
        Looper.prepare();

        // try to grab a rough location from the network provider before
        // registering for GPS, which may take a while to initialize
        Location lastKnownLocation = mLocationManager
            .getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);
        if(lastKnownLocation != null) {
            onLocationChanged(lastKnownLocation);
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    NATIVE_GPS_UPDATE_INTERVAL, 0,
                    this);
            Log.d(TAG, "Requested GPS updates");
        } catch(IllegalArgumentException e) {
            Log.w(TAG, "GPS location provider is unavailable");
        }
        Looper.loop();
    }

    public void stop() {
        super.stop();
        Log.i(TAG, "Disabled native GPS passthrough");
        mLocationManager.removeUpdates(this);
    }

    public void onLocationChanged(final Location location) {
        handleMessage(new RawMeasurement(Latitude.ID, location.getLatitude()));
        handleMessage(new RawMeasurement(Longitude.ID, location.getLongitude()));
    }

    public void onStatusChanged(String provider, int status,
            Bundle extras) {}
    public void onProviderEnabled(String provider) {}
    public void onProviderDisabled(String provider) {}

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("updateInterval", NATIVE_GPS_UPDATE_INTERVAL)
            .toString();
    }
}
