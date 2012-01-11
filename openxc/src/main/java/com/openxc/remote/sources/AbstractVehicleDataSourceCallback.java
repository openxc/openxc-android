package com.openxc.remote.sources;

/**
 * An interface for classes receiving data from a vehicle data source.
 *
 * Vehicle data sources provide updates via a callback for each individual
 * measurement received. Those wishing to receive the updates must register an
 * object extending this class and implemetning the {@link #receive(String,
 * Double)} and {@link #receive(String, Double, Double)} methods.
 *
 * Measurements received with String or Boolean values instead of Double are
 * coered to Double by the other receive() methods in this class - those can
 * optionally be overriden if the application has different requirements.
 */
public abstract class AbstractVehicleDataSourceCallback implements
        VehicleDataSourceCallbackInterface {

    public void receive(String name, Integer value) {
        receive(name, new Double(value));
    }

    public void receive(String name, Boolean value) {
        receive(name, booleanToDouble(value));
    }


    public void receive(String name, String value) {
        receive(name, new Double(value.toUpperCase().hashCode()));
    }

    public void receive(String name, String value, String event) {
        receive(name, new Double(value.toUpperCase().hashCode()),
               new Double(event.toUpperCase().hashCode()));
    }

    public void receive(String name, String value, Boolean event) {
        receive(name, new Double(value.toUpperCase().hashCode()),
                booleanToDouble(event));
    }

    private Double booleanToDouble(Boolean value) {
        return new Double(value.booleanValue() ? 1 : 0);
    }

    abstract public void receive(String name, Double value);
    abstract public void receive(String name, Double value, Double event);
}
