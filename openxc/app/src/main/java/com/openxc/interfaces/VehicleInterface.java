package com.openxc.interfaces;

import com.openxc.sinks.VehicleDataSink;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.VehicleDataSource;

/**
 * A connection to a physical vehicle interface that is capable of full duplex
 * communication.
 *
 * This is a simple combination of two other interfaces to represent an
 * interface capable of full duplex communication.
 *
 * The methods of {@link VehicleDataSource} are used to receive data from the
 * vehicle, and the methods of {@link VehicleDataSink} are used to send data
 * back to the vehicle.
 *
 * Implementation of this interface are expected to have a constructor that
 * accepts (Context context, String resource) where "context" is an active Android
 * application context (used to be able to refer to Android services and system
 * managers) and "resource" is a String, the format of which is defined by the
 * implementer (e.g. a URI, a MAC address, etc).
 */
public interface VehicleInterface extends VehicleDataSource, VehicleDataSink {
    /**
     * Change the resource used by the instance to connect to the interface,
     * restarting any neccessary services.
     *
     * @param resource The new resource to use for the interface.
     * @return true if the resource was different and the interface was
     *      restarted.
     */
    public boolean setResource(String resource) throws DataSourceException;

    /**
     * Return true if the interface is actively connected to the vehicle.
     */
    public boolean isConnected();
}
