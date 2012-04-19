package com.openxc.remote.sources;

import java.util.concurrent.BlockingQueue;

import java.util.Map;

import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.VehicleSpeed;

import com.openxc.remote.RawMeasurement;
import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import com.openxc.remote.sinks.FileRecorderSink;
import com.openxc.remote.sinks.VehicleDataSinkInterface;

import android.content.Context;

import android.location.Location;
import android.location.LocationManager;

import android.os.RemoteCallbackList;

import android.util.Log;

/**
 * A callback receiver for the vehicle data source.
 *
 * The selected vehicle data source is initialized with this callback object
 * and calls its receive() methods with new values as they come in - it's
 * important that receive() not block in order to get out of the way of new
 * meausrements coming in on a physical vehcile interface.
 */
public class DefaultVehicleDataSourceCallback
    extends AbstractVehicleDataSourceCallback {

    public final static String TAG = "DefaultVehicleDataSourceCallback";
    public final static String VEHICLE_LOCATION_PROVIDER = "vehicle";

    private Context mContext;
    private int mMessagesReceived = 0;
    private LocationManager mLocationManager;
    private BlockingQueue<String> mNotificationQueue;
    private Map<String, RawMeasurement> mMeasurements;
    private Map<String, RemoteCallbackList<
        RemoteVehicleServiceListenerInterface>> mListeners;
    private VehicleDataSinkInterface mDataSink;

    public DefaultVehicleDataSourceCallback(Context context,
            Map<String, RawMeasurement> measurements,
            Map<String, RemoteCallbackList<
            RemoteVehicleServiceListenerInterface>> listeners,
            BlockingQueue<String> notificationQueue) {
        mContext = context;
        mListeners = listeners;
        mMeasurements = measurements;
        mNotificationQueue = notificationQueue;
        mLocationManager = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);
        setupMockLocations();
    }

    private void updateLocation() {
        if(mLocationManager == null ||
                !mMeasurements.containsKey(Latitude.ID) ||
                !mMeasurements.containsKey(Longitude.ID) ||
                !mMeasurements.containsKey(VehicleSpeed.ID)) {
            return;
                }

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(mMeasurements.get(Latitude.ID)
                .getValue().doubleValue());
        location.setLongitude(mMeasurements.get(Longitude.ID)
                .getValue().doubleValue());
        location.setSpeed(mMeasurements.get(VehicleSpeed.ID)
                .getValue().floatValue());
        location.setTime(System.currentTimeMillis());

        try {
            mLocationManager.setTestProviderLocation(
                    LocationManager.GPS_PROVIDER, location);
            location.setProvider(VEHICLE_LOCATION_PROVIDER);
            mLocationManager.setTestProviderLocation(
                    VEHICLE_LOCATION_PROVIDER, location);
        } catch(SecurityException e) {
            Log.w(TAG, "Unable to use mocked locations, " +
                    "insufficient privileges", e);
        }
    }

    private void receive(final String measurementId,
            final RawMeasurement measurement) {
        mMeasurements.put(measurementId, measurement);
        if(mListeners.containsKey(measurementId)) {
            try  {
                mNotificationQueue.put(measurementId);
            } catch(InterruptedException e) {}
        }
    }

    private void receiveRaw(final String measurementId,
            Object value) {
        receiveRaw(measurementId, value, null);
    }

    public void enableRecording(boolean enabled) {
        if(enabled && mDataSink == null) {
            mDataSink = new FileRecorderSink(mContext);
            Log.i(TAG, "Initialized vehicle data sink " + mDataSink);
        } else if(mDataSink != null) {
            mDataSink.stop();
            mDataSink = null;
        }
    }

    public int getMessageCount() {
        return mMessagesReceived;
    }

    private void receiveRaw(final String measurementId,
            Object value, Object event) {
        if(mDataSink != null) {
            mDataSink.receive(measurementId, value, event);
        }
        mMessagesReceived++;
    }

    public void receive(String measurementId, Object value) {
        RawMeasurement measurement =
            RawMeasurement.measurementFromObjects(value);
        receive(measurementId, measurement);
        receiveRaw(measurementId, value);

        if(measurementId.equals(Latitude.ID) ||
                measurementId.equals(Longitude.ID)) {
            updateLocation();
                }
    }

    public void receive(String measurementId, Object value,
            Object event) {
        RawMeasurement measurement =
            RawMeasurement.measurementFromObjects(value, event);
        receive(measurementId, measurement);
        receiveRaw(measurementId, value, event);
    }

    /**
     * Setup Android location framework to accept vehicle GPS.
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
    private void setupMockLocations() {
        try {
            mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER,
                    false, false, false, false, false, true, false, 0, 5);
            mLocationManager.setTestProviderEnabled(
                    LocationManager.GPS_PROVIDER, true);

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
