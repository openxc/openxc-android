package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.openxc.units.State;
import com.openxc.measurements.NoValueException;

public class TransmissionGearPositionTest {
    TransmissionGearPosition measurement;

    @Before
    public void setUp() {
        measurement = new TransmissionGearPosition(
                new State<TransmissionGearPosition.GearPosition>(
                    TransmissionGearPosition.GearPosition.FIRST));
    }

    @Test
    public void testGet() throws NoValueException {
        assertThat(measurement.getValue().toString(), equalTo(
                    TransmissionGearPosition.GearPosition.FIRST.toString()));
    }

    @Test
    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }

    @Test
    public void testHasId() {
        assertNotNull(TransmissionGearPosition.ID);
    }
}
