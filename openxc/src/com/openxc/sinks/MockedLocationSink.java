package com.openxc.sinks;

import java.lang.reflect.Method;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.remote.RawMeasurement;

/**
 * Propagate vehicle location updates through the Android location interface.
 *
 * If we have at least latitude, longitude and vehicle speed from
 * the vehicle, we send out a mocked location for the
 * LocationManager.GPS_PROVIDER and VEHICLE_LOCATION_PROVIDER
 * providers.
 *
 * Developers can either use the standard Android location framework
 * with mocked locations enabled, or the specific OpenXC
 * Latitude/Longitude measurements.
 */
public class MockedLocationSink extends ContextualVehicleDataSink {
    public final static String TAG = "MockedLocationSink";
    public final static String VEHICLE_LOCATION_PROVIDER = "vehicle";

    private LocationManager mLocationManager;
    private boolean mOverwriteNativeStatus;
    private boolean mNativeGpsOverridden;

    public MockedLocationSink(Context context) {
        super(context);
        mLocationManager = (LocationManager) getContext().getSystemService(
                Context.LOCATION_SERVICE);
        setupMockLocations();
    }

    public boolean receive(RawMeasurement measurement) throws DataSinkException {
        super.receive(measurement);
        if(measurement.getName().equals(Latitude.ID) ||
                measurement.getName().equals(Longitude.ID)) {
            updateLocation();
            return true;
        }
        return false;
    }

    /**
     * Enable or disable overwriting Android's native GPS values with those from
     * the vehicle.
     *
     * If enabled, the GPS_PROVIDER from Android will respond with data taken
     * from the vehicle interface. The native GPS values will be used until GPS
     * data is actually received from the vehicle, so if the specific car you're
     * plugged into doesn't have GPS then the values will not be overwritten,
     * regardless of this setting.
     */
    public void setOverwritingStatus(boolean enabled) {
        mOverwriteNativeStatus = enabled;
        mNativeGpsOverridden = false;
        if(mLocationManager != null && !enabled) {
            try {
                mLocationManager.removeTestProvider(
                        LocationManager.GPS_PROVIDER);
            } catch(IllegalArgumentException e) {
                Log.d(TAG, "Unable to remove GPS test provider - " +
                        "probably wasn't added yet");
            }
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("enabled", mOverwriteNativeStatus)
            .toString();
    }

    private void makeLocationComplete(Location location) {
        if(android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.JELLY_BEAN) {
            // TODO When android 4.2 hits the maven repository we can simplify
            // this and call the makeComplete method directly, until then we use
            // reflection to load it without getting a compilation error.
            Method makeCompleteMethod = null;
            try {
                makeCompleteMethod = Location.class.getMethod("makeComplete");
                makeCompleteMethod.invoke(location);
            } catch(NoSuchMethodException e) {
            } catch(Exception e) {
            }
        } else {
            location.setTime(System.currentTimeMillis());
        }
    }

    private void updateLocation() {
        if(mLocationManager == null ||
                !containsMeasurement(Latitude.ID) ||
                !containsMeasurement(Longitude.ID) ||
                !containsMeasurement(VehicleSpeed.ID)) {
            return;
        }

        // Only enable overwriting the built-in Android GPS provider if we
        // actually receive a GPS update from the vehicle. This is to avoid
        // killing GPS just by having the OpenXC app installed (because it's
        // always running the serivce in the background).
        overwriteNativeProvider();

        Location location = new Location(LocationManager.GPS_PROVIDER);
        try {
            location.setLatitude(((Number)get(Latitude.ID).getValue()).doubleValue());
            location.setLongitude(((Number)get(Longitude.ID).getValue()).doubleValue());
            location.setSpeed(((Number)(get(VehicleSpeed.ID).getValue())).floatValue());
        } catch(ClassCastException e) {
            Log.e(TAG, "Expected a Number, but got something " +
                    "else -- not updating location", e);
        }

        makeLocationComplete(location);
        try {
            if(mOverwriteNativeStatus) {
                mLocationManager.setTestProviderLocation(
                        LocationManager.GPS_PROVIDER, location);
            }
            location.setProvider(VEHICLE_LOCATION_PROVIDER);
            mLocationManager.setTestProviderLocation(
                    VEHICLE_LOCATION_PROVIDER, location);
        } catch(SecurityException e) {
            Log.w(TAG, "Unable to use mocked locations, " +
                    "insufficient privileges", e);
        }
    }

    private void overwriteNativeProvider() {
        if(mOverwriteNativeStatus && !mNativeGpsOverridden) {
            try {
                mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER,
                        false, false, false, false, false, true, false, 0, 5);

                mLocationManager.setTestProviderEnabled(
                        LocationManager.GPS_PROVIDER, true);
            } catch(SecurityException e) {
                Log.w(TAG, "Unable to use mocked locations, " +
                        "insufficient privileges", e);
            }
        }
    }

    private void setupMockLocations() {
        try {
            if(mLocationManager.getProvider(
                        VEHICLE_LOCATION_PROVIDER) == null) {
                mLocationManager.addTestProvider(VEHICLE_LOCATION_PROVIDER,
                        false, false, false, false, false, true, false, 0, 5);
            }
            mLocationManager.setTestProviderEnabled(
                    VEHICLE_LOCATION_PROVIDER, true);
        } catch(SecurityException e) {
            Log.w(TAG, "Unable to use mocked locations, " +
                    "insufficient privileges", e);
            mLocationManager = null;
        }
    }
}
