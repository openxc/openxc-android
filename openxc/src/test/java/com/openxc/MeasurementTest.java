package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import com.openxc.units.Meter;
import com.openxc.util.Range;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.NoValueException;
import com.openxc.measurements.NoRangeException;

public class MeasurementTest {
    Measurement<Meter> measurement;
    Range<Meter> range;

    @Before
    public void setUp() {
        range = new Range<Meter>(new Meter(0.0), new Meter(101.2));
        measurement = new Measurement<Meter>(new Meter(10.0), range);
    }

    @Test
    public void testHasValue() {
        assertThat(measurement.isNone(), equalTo(false));
    }

    @Test
    public void testGet() throws NoValueException {
        assertThat(measurement.getValue().doubleValue(), equalTo(10.0));
    }

    @Test
    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }

    @Test
    public void testGetRange() throws NoRangeException {
        assertThat(measurement.getRange(), equalTo(range));
    }

    @Test
    public void testAgeIsPositive() throws NoValueException {
        assertThat(measurement.getAge(), greaterThan(0.0));
    }
}
