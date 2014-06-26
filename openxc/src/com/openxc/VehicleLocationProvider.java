package com.openxc;

import java.lang.reflect.Method;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.common.base.Objects;

import com.openxc.VehicleManager;
import com.openxc.NoValueException;
import com.openxc.remote.VehicleServiceException;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.VehicleSpeed;

/**
 * Propagate GPS and vehicle speed updates from OpenXC to the normal Android
 * location system.
 *
 * If we have at least latitude, longitude and vehicle speed from the vehicle,
 * we send out a new location for the LocationManager.GPS_PROVIDER and
 * VEHICLE_LOCATION_PROVIDER providers in the Android location service.
 *
 * Developers can either use the standard Android location framework
 * with vehicle locations enabled in OpenXC, or the specific OpenXC
 * Latitude/Longitude measurements directly.
 */
public class VehicleLocationProvider implements Measurement.Listener {
    public final static String TAG = "VehicleLocationProvider";
    public final static String VEHICLE_LOCATION_PROVIDER = "vehicle";

    private VehicleManager mVehicleManager;
    private LocationManager mLocationManager;
    private boolean mOverwriteNativeStatus;
    private boolean mNativeGpsOverridden;

    public VehicleLocationProvider(Context context, VehicleManager vehicleManager) {
        mVehicleManager = vehicleManager;
        mLocationManager = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);
        setupMockLocations();

        try {
            mVehicleManager.addListener(Latitude.class, this);
            mVehicleManager.addListener(Longitude.class, this);
            mVehicleManager.addListener(VehicleSpeed.class, this);
        } catch(VehicleServiceException e) {
            Log.d(TAG, "Unable to register for GPS from vehicle," +
                    "mocked location may not work");
        } catch(UnrecognizedMeasurementTypeException e) {
            // TODO this is dumb that we know these measurements are good, but
            // we still could get an exception
        }
    }

    public void stop() {
        if(mVehicleManager != null) {
            try {
                mVehicleManager.removeListener(Latitude.class, this);
                mVehicleManager.removeListener(Longitude.class, this);
                mVehicleManager.removeListener(VehicleSpeed.class, this);
            } catch(VehicleServiceException e) {
            }
        }
    }

    public void receive(Measurement measurement) {
        // Not using the value, but using it to trigger polling for an update
        updateLocation();
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
        try {
            Latitude latitude = (Latitude) mVehicleManager.get(Latitude.class);
            Longitude longitude = (Longitude) mVehicleManager.get(
                    Longitude.class);
            VehicleSpeed speed = (VehicleSpeed) mVehicleManager.get(
                    VehicleSpeed.class);

            // Only enable overwriting the built-in Android GPS provider if we
            // actually receive a GPS update from the vehicle. This is to avoid
            // killing GPS just by having the OpenXC app installed (because it's
            // always running the serivce in the background).
            overwriteNativeProvider();

            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(latitude.getValue().doubleValue());
            location.setLongitude(longitude.getValue().doubleValue());
            location.setSpeed((float) speed.getValue().doubleValue());

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
        } catch(NoValueException e) {
        } catch(UnrecognizedMeasurementTypeException e) {
            // TODO this is dumb that we know these measurements are good, but
            // we still could get an exception
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
