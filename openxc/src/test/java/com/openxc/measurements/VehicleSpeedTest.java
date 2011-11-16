package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import com.openxc.units.MetersPerSecond;
import com.openxc.measurements.NoValueException;

public class VehicleSpeedTest {
    VehicleSpeed measurement;

    @Before
    public void setUp() {
        measurement = new VehicleSpeed(new MetersPerSecond(1.0));
    }

    @Test
    public void testDefaultConstructor() {
        assertThat(new VehicleSpeed().isNone(), equalTo(true));
    }

    @Test
    public void testGet() throws NoValueException {
        assertThat(measurement.getValue().doubleValue(), equalTo(1.0));
    }

    @Test
    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }

    @Test
    public void testHasId() {
        assertNotNull(VehicleSpeed.ID);
    }
}
