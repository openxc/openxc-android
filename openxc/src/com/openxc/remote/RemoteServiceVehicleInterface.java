package com.openxc.remote;

import android.os.RemoteException;
import android.util.Log;

import com.openxc.interfaces.VehicleInterface;
import com.openxc.sources.SourceCallback;

/**
 *
 * A VehicleInterface that sends commands to a VehicleService operating in a
 * remote process.
 *
 * This is a workaround for the fact that we can't extend any other interfaces
 * with an Android remote service interface.
 *
 * Ideally the VehicleServiceInterface would extend VehicleInterface, since it
 * does implement set(RawMeasurement), but the generated code for the interface
 * loses whatever other interfaces you try and add.
 */
public class RemoteServiceVehicleInterface implements VehicleInterface {
    private final static String TAG = "RemoteServiceVehicleInterface";
    private VehicleServiceInterface mRemoteService;

    public RemoteServiceVehicleInterface(
            VehicleServiceInterface remoteService) {
        mRemoteService = remoteService;
    }

    public boolean receive(RawMeasurement command) {
        if(!isConnected()) {
            Log.w(TAG, "Not connected to the VehicleService");
            return false;
        }

        try {
            return mRemoteService.send(command);
        } catch(RemoteException e) {
            Log.w(TAG, "Unable to send command to remote vehicle service",
                    e);
            return false;
        }
    }

    public boolean isConnected() {
        return mRemoteService != null;
    }

    public void stop() { }

    public void setCallback(SourceCallback callback) { }

    public boolean setResource(String other) { return false; }
}
