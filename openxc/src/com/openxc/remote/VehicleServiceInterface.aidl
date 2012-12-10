package com.openxc.remote;

import com.openxc.remote.VehicleServiceListener;
import com.openxc.remote.RawMeasurement;

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
     * @param measurementType must match the ID field of a known Measurement
     *                        subclass.
     * @return a RawMeasurement which may or may not have a value. This function
     *         will never return null, even if no value is available.
     */
    RawMeasurement get(String measurementType);

    /**
     * Set a new value for the measurement class on the vehicle.
     *
     * @param measurement The measurement to set on the vehicle.
     */
    void set(in RawMeasurement measurement);

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
    void receive(in RawMeasurement measurement);

    /**
     * Re-initialize the list of sources back to the defaults.
     *
     * The default sources are the USB device source and a source that listens
     * for application-generated updates via the
     * {@link #receive(RawMeasurement)} method.
     */
    void initializeDefaultSources();

    /**
     * Remove all existing data sources from the pipeline.
     *
     * No further measurements will be received after this method is called
     * until either the default sources are re-initialized using
     * {@link #initializeDefaultSources}.
     */
    void clearSources();

    /**
     * @return number of messages received since instantiation.
     */
    int getMessageCount();

    List<String> getSourceSummaries();
    List<String> getSinkSummaries();
}
