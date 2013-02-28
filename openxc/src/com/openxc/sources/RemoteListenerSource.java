package com.openxc.sources;

import android.os.RemoteException;
import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.remote.RawMeasurement;
import com.openxc.remote.VehicleServiceInterface;
import com.openxc.remote.VehicleServiceListener;

/**
 * Pass measurements from a VehicleService to an in-process callback.
 *
 * This source is a bit of a special case - it's used by the
 * {@link com.openxc.VehicleManager} to inject measurement updates from a
 * {@link com.openxc.remote.VehicleService} into an in-process data
 * pipeline. By using the same workflow as on the remote process side, we can
 * share code between remote and in-process data sources and sinks. This makes
 * adding new sources and sinks possible for end users, since the
 * VehicleService doesn't need to have every possible implementation.
 */
public class RemoteListenerSource extends BaseVehicleDataSource {
    private final static String TAG = "RemoteListenerSource";
    private VehicleServiceInterface mService;

    /**
     * Registers a measurement listener with the remote service.
     */
    public RemoteListenerSource(VehicleServiceInterface service) {
        mService = service;

        try {
            mService.register(mRemoteListener);
        } catch(RemoteException e) {
            Log.w(TAG, "Unable to register to receive " +
                    "measurement callbacks", e);
        }
    }

    public void stop() {
        super.stop();
        try {
            mService.unregister(mRemoteListener);
        } catch(RemoteException e) {
            Log.w(TAG, "Unable to register to receive " +
                    "measurement callbacks", e);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).toString();
    }

    private VehicleServiceListener mRemoteListener =
        new VehicleServiceListener.Stub() {
            public void receive(RawMeasurement rawMeasurement) {
                handleMessage(rawMeasurement);
            }
        };
}
