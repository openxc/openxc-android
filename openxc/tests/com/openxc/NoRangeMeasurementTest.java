package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import junit.framework.TestCase;

import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.NoRangeException;
import com.openxc.units.Meter;

public class NoRangeMeasurementTest extends TestCase {
    BaseMeasurement<Meter> measurement;

    @Override
    public void setUp() {
        measurement = new BaseMeasurement<Meter>(new Meter(10.0));
    }

    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }

    public void testEmptyRange() throws NoRangeException {
        try {
        measurement.getRange();
        } catch(NoRangeException e) {
            return;
        }
        fail();
    }

    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(10.0));
    }
}
