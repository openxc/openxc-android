package com.openxc.measurements;

import com.openxc.units.Unit;
import com.openxc.util.Range;

/**
 * The MeasurementInterface is the base for all OpenXC measurements.
 *
 * A Measurement has at least a value and an age, and optionally a range of
 * valid values.
 */
public interface Measurement {
    public interface Listener {
        public void receive(Measurement measurement);
    }

    /**
     * Retreive the age of this measurement.
     *
     * @return the age of the data in seconds.
     */
    public double getAge();

    /**
     * Set the birth timestamp for this measurement.
     *
     */
    public void setTimestamp(double timestamp);

    /**
     * Determine if this measurement has a valid range.
     *
     * @return true if the measurement has a non-null range.
     */
    public boolean hasRange();

    /**
     * Retrieve the valid range of the measurement.
     *
     * @return the Range of the measurement
     * @throws NoRangeException if the measurement doesn't have a range.
     */
    public Range<? extends Unit> getRange() throws NoRangeException;

    /**
     * Return the value of this measurement.
     *
     * @return The wrapped value (an instance of TheUnit)
     */
    public Unit getValue();

    public String serialize();

    public String getGenericName();

    /**
     * Return the value of this measurement as a type good for serialization.
     *
     * This is required because we go through this process with each
     * measurement:
     *
     *     - Incoming JSON is parsed to String, boolean, double, and int.
     *     - All types are converted to double by RawMeasurement in order to fit
     *     over AIDL (TODO this seems really, really wrong and adds a ton of
     *     complication - I know I spent 2 or 3 days on trying to do templated
     *     classes over AIDL and it just didn't seem to work, but there must be
     *     a better way)
     *     - Converted to full Measurement in VehicleManager (the specific
     *     Measurement classes know how to convert from a double back to the
     *     real value.
     *     - Output the original values (as if they were directly from JSON)
     *     form the Measurement for the data sinks in the VehicleManager.
     *
     * @return something easily serializable - e.g. String, Double, Boolean.
     */
    public Object getSerializedValue();

    /**
     * Return an optional event associated with this measurement.
     *
     * TODO argh, no easy way to get a type for this without having two template
     * parameters. can we have an optional template parameter in Java?
     */
    public Object getEvent();

    /**
     * Return the event of this measurement as a type good for serialization.
     *
     * @see #getSerializedValue()
     */
    public Object getSerializedEvent();
}
