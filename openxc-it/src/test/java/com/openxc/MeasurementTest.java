package com.openxc;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import static junit.framework.Assert.assertTrue;

import com.openxc.units.Meter;
import com.openxc.util.Range;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.NoRangeException;

public class MeasurementTest extends TestCase {
    Measurement<Meter> measurement;
    Range<Meter> range;

    @Override
    public void setUp() {
        range = new Range<Meter>(new Meter(0.0), new Meter(101.2));
        measurement = new Measurement<Meter>(new Meter(10.0), range);
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
}
