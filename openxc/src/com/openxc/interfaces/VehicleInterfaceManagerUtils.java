package com.openxc.interfaces;

import java.util.List;

import android.util.Log;

import com.openxc.measurements.Measurement;
import com.openxc.remote.RawMeasurement;
import com.openxc.sinks.DataSinkException;

public class VehicleInterfaceManagerUtils {
    private static final String TAG = "VehicleInterfaceManagerUtils";

    /**
     *
     * Converts the command to a RawMeasurement before calling
     * {@link #send(List, RawMeasurement)}.
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
