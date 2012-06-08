package com.openxc;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

import static junit.framework.Assert.assertTrue;

import com.openxc.units.Meter;
import com.openxc.util.Range;
import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.NoRangeException;

public class MeasurementTest extends TestCase {
    BaseMeasurement<Meter> measurement;
    Range<Meter> range;

    @Override
    public void setUp() {
        range = new Range<Meter>(new Meter(0.0), new Meter(101.2));
        measurement = new BaseMeasurement<Meter>(new Meter(10.0), range);
    }

    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(10.0));
    }

    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }

    public void testGetRange() throws NoRangeException {
        assertThat(measurement.getRange(), equalTo(range));
    }

    public void testAgeIsPositive() {
        assertThat(measurement.getAge(), greaterThan(0.0));
    }

    public void testEquality() {
        BaseMeasurement<Meter> anotherMeasurement =
            new BaseMeasurement<Meter>(new Meter(10.0), range);
        BaseMeasurement<Meter> inequalMeasurement  =
            new BaseMeasurement<Meter>(new Meter(12.0), range);
        assertThat(measurement, equalTo(anotherMeasurement));
        assertThat(measurement, not(equalTo(inequalMeasurement)));
    }

    public void testSerialize() {
        assertThat(measurement.serialize(), equalTo(
                    "{\"name\": \"" + measurement.ID + "\", \"value\": "
                    + measurement.getValue() + "}"));
    }

    public void testDeserialize() {
        assertThat(BaseMeasurement.deserialize(measurement.serialize()),
                equalTo(measurement));
    }
}
