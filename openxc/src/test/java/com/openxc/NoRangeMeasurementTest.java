package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;

import com.openxc.units.Meter;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.NoValueException;
import com.openxc.measurements.NoRangeException;

public class NoRangeMeasurementTest {
    Measurement<Meter> measurement;

    @Before
    public void setUp() {
        measurement = new Measurement<Meter>(new Meter(10.0));
    }

    @Test
    public void testHasValue() {
        assertThat(measurement.isNone(), equalTo(true));
    }

    @Test
    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }

    @Test(expected=NoRangeException.class)
    public void testEmptyRange() throws NoRangeException {
        measurement.getRange();
    }

    @Test
    public void testGet() throws NoValueException {
        assertThat(measurement.getValue().doubleValue(), equalTo(10.0));
    }
}
