package com.openxc.interfaces;

import java.util.List;

import android.util.Log;

import com.openxc.measurements.Measurement;
import com.openxc.remote.RawMeasurement;
import com.openxc.sinks.DataSinkException;

/**
 * Utilities for interacting with a number of VehicleInterface objects.
 *
 * These functions are shared by the {@link com.openxc.VehicleManager} an
 * {@link com.openxc.remote.VehicleService} which do not share a common
 * ancestor, so they are encapsulated in this mixin.
 */
public class VehicleInterfaceManagerUtils {
    private static final String TAG = "VehicleInterfaceManagerUtils";

    /**
     * Attempt to send the command on every VehicleInterface in the list until
     * at most one succeeds.
     *
     * @see send(List, RawMeasurement)
     */
    public static boolean send(List<VehicleInterface> interfaces,
            Measurement command) {
        return send(interfaces, command.toRaw());
    }

    /**
     * Attempt to send the command on every VehicleInterface in the list until
     * at most one succeeds.
     *
     * This method makes no guaratees about the order it will traverse the list.
     */
    public static boolean send(List<VehicleInterface> interfaces,
            RawMeasurement command) {
        command.untimestamp();
        for(VehicleInterface vehicleInterface : interfaces) {
            try {
                if(vehicleInterface.receive(command)) {
                    Log.d(TAG, "Sent " + command + " using interface " +
                            vehicleInterface);
                    return true;
                }
            } catch(DataSinkException e) {
                continue;
            }
        }
        Log.d(TAG, "No interfaces able to send " + command);
        return false;
    }
}
