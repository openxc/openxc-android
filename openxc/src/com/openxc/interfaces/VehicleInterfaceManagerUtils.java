package com.openxc.interfaces;

import java.util.List;

import android.util.Log;

import com.openxc.messages.VehicleMessage;
import com.openxc.sinks.DataSinkException;

/**
 * Utilities for interacting with a number of VehicleInterface objects.
 *
 * These functions are shared by the {@link com.openxc.VehicleManager} and
 * {@link com.openxc.remote.VehicleService} which do not share a common
 * ancestor, so they are encapsulated in this mixin.
 */
public class VehicleInterfaceManagerUtils {
    private static final String TAG = "VehicleInterfaceManagerUtils";

    /**
     * Attempt to send the command on every VehicleInterface in the list until
     * at most one succeeds.
     *
     * This method makes no guaratees about the order it will traverse the list.
     */
    public static boolean send(List<VehicleInterface> interfaces,
            VehicleMessage command) {
        command.untimestamp();
        for(VehicleInterface vehicleInterface : interfaces) {
            if(vehicleInterface.isConnected()) {
                try {
                    vehicleInterface.receive(command);
                    Log.d(TAG, "Sent " + command + " using interface " +
                            vehicleInterface);
                    return true;
                } catch(DataSinkException e) {
                    Log.v(TAG, "Interface " + vehicleInterface
                            + " unable to send command", e);
                }
            }
        }
        return false;
    }

    public static VehicleInterfaceDescriptor getDescriptor(
            VehicleInterface vi) {
        return new VehicleInterfaceDescriptor(vi.getClass(),
                vi.isConnected());
    }
}
