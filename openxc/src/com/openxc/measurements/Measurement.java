package com.openxc.measurements;

import com.openxc.remote.RawMeasurement;

import com.openxc.units.Unit;
import com.openxc.util.Range;

/**
 * The Measurement is the base for all OpenXC measurements.
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
     * @return the age of the data in milliseconds.
     */
    public long getAge();

    /**
     * Set the birth timestamp for this measurement.
     *
     * @param timestamp the new timestamp, in milliseconds since the epoch.
     */
    public void setTimestamp(long timestamp);

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

    public RawMeasurement toRaw();

    /**
     * Return the creation time of this measurement;
     *
     * @return the creation time in milliseconds since the epoch of this
     * measurement.
     */
    public long getBirthtime();

    public String getGenericName();

    /**
     * Return the value of this measurement as a type good for serialization.
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
