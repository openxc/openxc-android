package com.openxc.remote.sources;

/**
 * An interface for classes receiving data from a vehicle data source.
 *
 * Vehicle data sources provide updates via a callback for each individual
 * measurement received. Those wishing to receive the updates must register an
 * object extending this class and implementing the {@link #receive(String,
 * Object)} and {@link #receive(String, Object, Object)} methods.
 */
public abstract class AbstractVehicleDataSourceCallback implements
        VehicleDataSourceCallbackInterface {

    abstract public void receive(String name, Object value);
    abstract public void receive(String name, Object value, Object event);
}
