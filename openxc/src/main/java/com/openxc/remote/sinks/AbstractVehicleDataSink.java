package com.openxc.remote.sinks;

import com.openxc.remote.RawMeasurement;

/**
 * The interface for all output targets for raw vehicle measurements.
 *
 * Data sinks are registered with the
 * {@link com.openxc.remote.RemoteVehicleService} and receive all raw messages
 * from the vehicle data source as they arrive. Common applications of this
 * class are trace file recording, web streaming or custom CAN message handling.
 *
 * The RemoteVehicleService pipes all data received from a vehicle data source
 * to a list of objects registered with this interface. Those wishing to receive
 * the updates must register an object extending this class and implementing the
 * {@link #receive(String, Object)} and {@link #receive(String, Object, Object)}
 * methods.
 */
public abstract class AbstractVehicleDataSink implements VehicleDataSink {
    /**
     * Receive a data point with a name, a value and a event value.
     *
     * This method is similar to {@link #receive(String, Object)} but also
     * accepts the optional event parameter for an OpenXC message.
     *
     * Just like in {@link #receive(String, Object)}, the implementation of this
     * method should not block, lest the vehicle data source get behind in
     * processing data from a source potentially external to the system.
     *
     * If you override this method be sure to call super.receive to make sure
     * the RawMeasurement is created and passed to receive(String,
     * RawMeasurement), unless you don't want that to happen.
     *
     * @param name The name of the element.
     * @param value The String value of the element.
     * @param event The String event of the element.
     */
    public void receive(String measurementId, Object value, Object event) {
        RawMeasurement measurement =
            RawMeasurement.measurementFromObjects(value, event);
        receive(measurementId, measurement);
    }

    public void receive(String measurementId, Object value) {
        receive(measurementId, value, null);
    }

    /**
     * TODO The reason some sinks will need the raw objects vs. the RawMeasurement is
     * that once we construct the RawMeasurement, the actual object values are
     * pseduo serialized to a Double in order to pass through the AIDL
     * interface. Actually, why do we need to do that? Can we use a generic
     * Object pointer in the raw measurement and smartly cast....ah no, because
     * when we read from the Parcel we have to know in advance what the type is.
     * This tells me that the subclasses of rawmeasurement would be really good,
     * but I remember I struggled with that for a day or two. Perhaps worth
     * revisiting because these double receive() methods are weird.
     */
    public void receive(String measurementId,
            RawMeasurement measurement) {
        // do nothing unless you override it
    }

    /**
     * Release any acquired resources in preparation for exiting.
     */
    public void stop() {
        // do nothing unless you need it
    }
}
