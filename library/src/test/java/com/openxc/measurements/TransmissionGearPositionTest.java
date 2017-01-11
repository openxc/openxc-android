package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.openxc.units.State;

public class TransmissionGearPositionTest {
    TransmissionGearPosition measurement;

    @Before
    public void setUp() {
        measurement = new TransmissionGearPosition(
                new State<TransmissionGearPosition.GearPosition>(
                    TransmissionGearPosition.GearPosition.FIRST));
    }

    @Test
    public void testGet() {
        assertThat(measurement.getValue().enumValue(), equalTo(
                    TransmissionGearPosition.GearPosition.FIRST));
    }

    @Test
    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }
}
