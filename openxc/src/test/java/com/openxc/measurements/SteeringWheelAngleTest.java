package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import com.openxc.units.Degree;

public class SteeringWheelAngleTest {
    SteeringWheelAngle measurement;

    @Before
    public void setUp() {
        measurement = new SteeringWheelAngle(new Degree(1.0));
    }

    @Test
    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(1.0));
    }

    @Test
    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }

    @Test
    public void testHasId() {
        assertNotNull(SteeringWheelAngle.ID);
    }
}
