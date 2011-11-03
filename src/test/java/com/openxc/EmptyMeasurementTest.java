package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import com.openxc.units.Meter;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.NoValueException;
import com.openxc.measurements.NoRangeException;

public class EmptyMeasurementTest {
    Measurement<Meter> measurement;

    @Before
    public void setUp() {
        measurement = new Measurement<Meter>();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testNoValue() {
        assertThat(measurement.hasValue(), equalTo(false));
    }

    @Test(expected=NoValueException.class)
    public void testEmptyAge() throws NoValueException {
        measurement.getAge();
    }

    public void testVariance() {
        assertThat(measurement.getVariance().doubleValue(), equalTo(0.0));
    }

    @Test
    public void testEmptyRangeCheck() {
        assertFalse(measurement.hasRange());
    }

    @Test(expected=NoRangeException.class)
    public void testEmptyRange() throws NoRangeException {
        measurement.getRange();
    }

    @Test(expected=NoValueException.class)
    public void testGet() throws NoValueException {
        measurement.getValue();
    }
}
