package com.openxc.remote;

import com.openxc.remote.VehicleServiceListener;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.MessageKey;
import com.openxc.remote.ViConnectionListener;
import com.openxc.interfaces.VehicleInterfaceDescriptor;

/**
 * The AIDL interface for a VehicleService running in a separate process.
 *
 * This interface is used to bind to an AIDL interface in order to interact with
 * a centralized VehicleService that is reading data from a vehicle, trace
 * file or other source.
 *
 * Applications should use the in-process VehicleManager, as that builds the
 * proper Measurement types before returning - the data at this level is very
 * loosely typed in order to slip through the limited AIDL interface.
 */
interface VehicleServiceInterface {
    /**
     * Retreive the most recent value for the measurement.
     *
     * @param key the key of the message to retreive.
     * @return the last VehicleMessage received with this key, or null if neve
     *      received.
     */
    VehicleMessage get(in MessageKey key);

    /**
     * Set a new value for the measurement class on the vehicle.
     *
     * @param measurement The measurement to set on the vehicle.
     */
    boolean send(in VehicleMessage measurement);

    /**
     * Register to receive asynchronous updates when measurements are received.
     *
     * All instances of VehicleManager in application processes must register
     * themselves if they want to use the asynchronous interface.
     */
    void register(VehicleServiceListener listener);

    /**
     * Stop sending asynchronous measurement updates to a remote listener.
     *
     * Instances of VehicleManager should unregister themselves if they no
     * longer require real-time updates.
     */
    void unregister(VehicleServiceListener listener);

    /**
     * Receive a new measurement that originates from an application.
     *
     * Applications may have alternative data sources that cannot be
     * instantiated in the remote process (e.g. a trace file playback source).
     * As an application's source receive updates, it can pass them back into
     * the remote process using this method.
     */
    void receive(in VehicleMessage measurement);

    /**
     * @return number of messages received since instantiation.
     */
    int getMessageCount();

    void setVehicleInterface(String interfaceName, String resource);

    /**
     * Return list of tokens identifying the data sources that are enabled and
     * the connection status for each.
     */
    VehicleInterfaceDescriptor getVehicleInterfaceDescriptor();

    /**
     * The one vehicle interface specific function, control whether polling is
     * used to find Bluetooth vehicle interfaces.
     */
    void setBluetoothPollingStatus(boolean enabled);

    /**
     * Set the VehicleService to use or not use the device's built-in GPS for
     * location, to augment a vehicle that does no have GPS.
     */
    void setNativeGpsStatus(boolean enabled);

    void userPipelineActivated();
    void userPipelineDeactivated();

    void addViConnectionListener(in ViConnectionListener listener);

    boolean isViConnected();
}
