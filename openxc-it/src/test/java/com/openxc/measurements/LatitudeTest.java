package com.openxc.measurements;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.openxc.units.Degree;

public class LatitudeTest extends TestCase {
    Latitude measurement;

    @Override
    public void setUp() {
        measurement = new Latitude(new Degree(42.0));
    }

    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(42.0));
    }

    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }
}
