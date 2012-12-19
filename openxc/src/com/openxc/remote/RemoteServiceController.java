package com.openxc.remote;

import android.os.RemoteException;
import android.util.Log;

import com.openxc.controllers.VehicleController;

/**
 *
 * A VehicleController that sends commands to a VehicleService operating in a
 * remote process.
 *
 * This is a workaround for the fact that we can't extend any other interfaces
 * with an Android remote service interface.
 *
 * Ideally the VehicleServiceInterface would extend VehicleController, since it
 * does implement set(RawMeasurement), but the generated code for the interface
 * loses whatever other interfaces you try and add.
 */
public class RemoteServiceController implements VehicleController {
    private final static String TAG = "RemoteServiceController";
    private VehicleServiceInterface mRemoteService;

    public RemoteServiceController(VehicleServiceInterface remoteService) {
        mRemoteService = remoteService;
    }

    public boolean set(RawMeasurement command) {
        if(mRemoteService == null) {
            Log.w(TAG, "Not connected to the VehicleService");
            return false;
        }

        try {
            return mRemoteService.set(command);
        } catch(RemoteException e) {
            Log.w(TAG, "Unable to send command to remote vehicle service",
                    e);
            return false;
        }
    }
}
