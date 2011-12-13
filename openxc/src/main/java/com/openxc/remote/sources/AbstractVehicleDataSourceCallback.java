package com.openxc.remote.sources;

/**
 * An interface for classes receiving data from a vehicle data source.
 *
 * Vehicle data sources provide updates via a callback for each individual
 * measurement received. Those wishing to receive the updates must register an
 * object extending this class and implemetning the {@link receive(String,
 * Double)} and {@link receive(String, Double, Double)} methods.
 *
 * Measurements received with String or Boolean values instead of Double are
 * coered to Double by the other receive() methods in this class - those can
 * optionally be overriden if the application has different requirements.
 */
public abstract class AbstractVehicleDataSourceCallback implements
        VehicleDataSourceCallbackInterface {

    /**
     * Receive a data point with a name and integer value.
     *
     * The value is cast to a Double, with no loss of precision.
     *
     * @param name The name of the element.
     * @param value The Integer value of the element.
     */
    public void receive(String name, Integer value) {
        receive(name, new Double(value));
    }

    /**
     * Receive a data point with a name and boolean value.
     *
     * The boolean is cast to a double (1 for true, 0 for false with no loss of
     * precision.
     *
     * @param name The name of the element.
     * @param value The Boolean value of the element.
     */
    public void receive(String name, Boolean value) {
        receive(name, new Double(value.booleanValue() ? 1 : 0));
    }


    /**
     * Receive a data point with a name and string value.
     *
     * The value is converted to a Double equal to the hash of the string.
     *
     * @param name The name of the element.
     * @param value The String value of the element.
     */
    public void receive(String name, String value) {
        receive(name, new Double(value.toUpperCase().hashCode()));
    }

    /**
     * Receive a data point with a name, string value and string event.
     *
     * The value and event are converted to Doubles equal to the hash of the
     * strings.
     *
     * @param name The name of the element.
     * @param value The String value of the element.
     * @param event The String event of the element.
     */
    public void receive(String name, String value, String event) {
        receive(name, new Double(value.toUpperCase().hashCode()),
               new Double(event.toUpperCase().hashCode()));
    }

    /**
     * Receive a data point with a name and double value.
     *
     * The implementation of this method should not block, lest the vehicle data
     * source get behind in processing data from a source potentially external
     * to the system.
     *
     * @param name The name of the element.
     * @param value The String value of the element.
     */
    abstract public void receive(String name, Double value);

    /**
     * Receive a data point with a name, double value and double event value.
     *
     * This method is similar to {@link receive(String, Double)} but also
     * accepts the optional event parameter for an OpenXC message.
     *
     * Identical to {@link receive(String, Double)}, the implementation of this
     * method should not block, lest the vehicle data source get behind in
     * processing data from a source potentially external to the system.
     *
     * @param name The name of the element.
     * @param value The String value of the element.
     * @param event The String event of the element.
     */
    abstract public void receive(String name, Double value, Double event);
}
