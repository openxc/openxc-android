package com.openxc.measurements;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.openxc.units.Kilometer;

public class FineOdometerTest extends TestCase {
    FineOdometer measurement;

    @Override
    public void setUp() {
        measurement = new FineOdometer(new Kilometer(1.0));
    }

    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(1.0));
    }

    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }
}
