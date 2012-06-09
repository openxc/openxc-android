package com.openxc.measurements;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.openxc.units.KilometersPerHour;

public class VehicleSpeedTest extends TestCase {
    VehicleSpeed measurement;

    @Override
    public void setUp() {
        measurement = new VehicleSpeed(new KilometersPerHour(1.0));
    }

    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(1.0));
    }

    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }
}
