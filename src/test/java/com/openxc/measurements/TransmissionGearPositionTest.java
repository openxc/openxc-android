package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;

import com.openxc.units.GearPosition;
import com.openxc.measurements.NoValueException;

public class TransmissionGearPositionTest {
    TransmissionGearPosition measurement;

    @Before
    public void setUp() {
        measurement = new TransmissionGearPosition(
                new GearPosition(GearPosition.GearPositions.FIRST));
    }

    @Test
    public void testGet() throws NoValueException {
        assertThat(measurement.getValue().toString(), equalTo(
                    GearPosition.GearPositions.FIRST.toString()));
    }

    @Test
    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }

    @Test
    public void testHasId() {
        assertThat(measurement.getId(), equalTo("transmission_gear"));
    }

}
