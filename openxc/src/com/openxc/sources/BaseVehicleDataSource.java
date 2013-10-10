package com.openxc.sources;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import com.openxc.BinaryMessages;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.remote.RawMeasurement;

/**
 * A common parent for all vehicle data sources.
 *
 * This class encapsulates funcationaliy common to most data sources. It accepts
 * and stores a SourceCallback reference (required by the
 * {@link com.openxc.sources.VehicleDataSource} interface) and implements a
 * {@link #handleMessage(RawMeasurement)} method for subclass to call
 * with each new measurement, regardless of its origin.
 */
public class BaseVehicleDataSource implements VehicleDataSource {
    private final static String TAG = "BaseVehicleDataSource";
    private SourceCallback mCallback;
    private final Lock mCallbackLock = new ReentrantLock();
    private final Condition mCallbackChanged = mCallbackLock.newCondition();

    public BaseVehicleDataSource() { }

    /**
     * Construct a new instance and set the callback.
     *
     * @param callback An object implementing the
     *      SourceCallback interface that should receive data from this
     *      source.
     */
    public BaseVehicleDataSource(SourceCallback callback) {
        setCallback(callback);
    }

    /**
     * Set the current source callback to the given value.
     *
     * @param callback a valid callback or null if you wish to stop the source
     *      from sending updates.
     */
    public void setCallback(SourceCallback callback) {
        mCallbackLock.lock();
        mCallback = callback;
        mCallbackChanged.signal();
        mCallbackLock.unlock();
    }

    protected void disconnected() {
        if(mCallback != null) {
            mCallback.sourceDisconnected(this);
        }
    }

    protected void connected() {
        if(mCallback != null) {
            mCallback.sourceConnected(this);
        }
    }

    public boolean isConnected() {
        // TODO this is kind of weird, and probably says that this API needs
        // refactoring - we don't want to keep the Pipeline awake because it
        // thinks the sources like the RemoteListenerSource and
        // ApplicationSource (which are always in the pipeline) are "connected"
        // to a vehicle. maybe this isConnected method should only apply to the
        // VehicleInterface type of sources, but we don't currently track those
        // separately.
        return false;
    }

    /**
     * Clear the callback so no further updates are sent.
     *
     * Subclasses should be sure to call super.stop() so they also stop sending
     * updates when killed by a user.
     */
    public void stop() {
        disconnected();
        setCallback(null);
    }

    /**
     * Pass a new measurement to the callback, if set.
     *
     * @param measurement the new measurement object.
     */
    protected void handleMessage(RawMeasurement measurement) {
        if(mCallback != null && measurement != null) {
            mCallback.receive(measurement);
        }
    }

    protected void handleMessage(String serializedMeasurement) {
        try {
          handleMessage(new RawMeasurement(serializedMeasurement));
        } catch(UnrecognizedMeasurementTypeException e) {
        }
    }

    protected void handleMessage(BinaryMessages.VehicleMessage message) {
        try {
          handleMessage(new RawMeasurement(message));
        } catch(UnrecognizedMeasurementTypeException e) {
            Log.d(TAG, "Unable to handle binary message", e);
        }
    }

    /**
     * Return a string suitable as a tag for logging.
     */
    protected String getTag() {
      return TAG;
    }
}
