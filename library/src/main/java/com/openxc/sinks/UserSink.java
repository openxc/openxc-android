package com.openxc.sinks;

import android.os.RemoteException;
import android.util.Log;

import com.google.common.base.MoreObjects;
import com.openxc.messages.VehicleMessage;
import com.openxc.remote.VehicleServiceInterface;

/**
 * Pass measurements from a user-level data sources back to the remote
 * VehicleService.
 *
 * This sink is a bit of a special case - it's used by the
 * {@link com.openxc.VehicleManager} to pass measurement updates from a user's
 * data sources back to the VehicleService, so it can propagate them to all
 * OpenXC apps.
 */
public class UserSink implements VehicleDataSink {
    private final static String TAG = UserSink.class.getSimpleName();
    private VehicleServiceInterface mService;

    /**
     * Registers a measurement listener with the remote service.
     */
    public UserSink(VehicleServiceInterface service) {
        mService = service;
    }

    @Override
    public void receive(VehicleMessage measurement) {
        if(mService != null) {
            try {
                mService.receive(measurement);
            } catch(RemoteException e) {
                Log.d(TAG, "Unable to send message to remote service", e);
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }

    @Override
    public void stop() { }
}
