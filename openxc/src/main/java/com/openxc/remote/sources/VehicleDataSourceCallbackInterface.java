package com.openxc.remote.sources;

public interface VehicleDataSourceCallbackInterface {
    /**
     * Receive a data point with a name and a value (Double, Integer, Boolean or
     * String).
     *
     * The implementation of this method should not block, lest the vehicle data
     * source get behind in processing data from a source potentially external
     * to the system.
     *
     * @param name The name of the element.
     * @param value The String value of the element.
     */
    public void receive(String name, Object value);

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
     * @param name The name of the element.
     * @param value The String value of the element.
     * @param event The String event of the element.
     */
    public void receive(String name, Object value, Object event);
}
