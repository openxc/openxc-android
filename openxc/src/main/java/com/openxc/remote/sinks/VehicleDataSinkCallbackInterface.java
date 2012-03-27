package com.openxc.remote.sinks;

public interface VehicleDataSinkCallbackInterface {
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
    public void receive(String name, Double value);

    /**
     * Receive a data point with a name, double value and double event value.
     *
     * This method is similar to {@link #receive(String, Double)} but also
     * accepts the optional event parameter for an OpenXC message.
     *
     * Identical to {@link #receive(String, Double)}, the implementation of this
     * method should not block, lest the vehicle data source get behind in
     * processing data from a source potentially external to the system.
     *
     * @param name The name of the element.
     * @param value The String value of the element.
     * @param event The String event of the element.
     */
    public void receive(String name, Double value, Double event);

    /**
     * Receive a data point with a name and integer value.
     *
     * The value is cast to a Double, with no loss of precision.
     *
     * @param name The name of the element.
     * @param value The Integer value of the element.
     */
    public void receive(String name, Integer value);

    /**
     * Receive a data point with a name and boolean value.
     *
     * The boolean is cast to a double (1 for true, 0 for false with no loss of
     * precision.
     *
     * @param name The name of the element.
     * @param value The Boolean value of the element.
     */
    public void receive(String name, Boolean value);

    /**
     * Receive a data point with a name and string value.
     *
     * The value is converted to a Double equal to the hash of the string.
     *
     * @param name The name of the element.
     * @param value The String value of the element.
     */
    public void receive(String name, String value);

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
    public void receive(String name, String value, String event);

    /**
     * Receive a data point with a name, string value and boolean event.
     *
     * The value and event are converted to Doubles equal to the hash of the
     * strings.
     *
     * @param name The name of the element.
     * @param value The String value of the element.
     * @param event The Boolean event of the element.
     */
    public void receive(String name, String value, Boolean event);
}
