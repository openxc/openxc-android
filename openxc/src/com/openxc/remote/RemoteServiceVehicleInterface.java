package com.openxc.remote;

import android.os.RemoteException;
import android.util.Log;

import com.openxc.interfaces.VehicleInterface;
import com.openxc.messages.VehicleMessage;
import com.openxc.sources.SourceCallback;
import com.openxc.sinks.DataSinkException;

/**
 *
 * A VehicleInterface that sends commands to a VehicleService operating in a
 * remote process.
 *
 * This is a workaround for the fact that we can't extend any other interfaces
 * with an Android remote service interface.
 *
 * Ideally the VehicleServiceInterface would extend VehicleInterface, since it
 * does implement set(VehicleMessage), but the generated code for the interface
 * loses whatever other interfaces you try and add.
 */
public class RemoteServiceVehicleInterface implements VehicleInterface {
    private final static String TAG = "RemoteServiceVehicleInterface";
    private VehicleServiceInterface mRemoteService;

    public RemoteServiceVehicleInterface(
            VehicleServiceInterface remoteService) {
        mRemoteService = remoteService;
    }

    public void receive(VehicleMessage command) throws DataSinkException {
        if(!isConnected()) {
            throw new DataSinkException("Not connected to the VehicleService");
        }

        try {
            mRemoteService.send(command);
        } catch(RemoteException e) {
            throw new DataSinkException(
                    "Unable to send command to remote vehicle service");
        }
    }

    public boolean isConnected() {
        return mRemoteService != null;
    }

    public void stop() { }

    public void setCallback(SourceCallback callback) { }

    public boolean setResource(String other) { return false; }
}
