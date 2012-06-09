package com.openxc.measurements;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.openxc.units.State;

public class TransmissionGearPositionTest extends TestCase {
    TransmissionGearPosition measurement;

    @Override
    public void setUp() {
        measurement = new TransmissionGearPosition(
                new State<TransmissionGearPosition.GearPosition>(
                    TransmissionGearPosition.GearPosition.FIRST));
    }

    public void testGet() {
        assertThat(measurement.getValue().enumValue(), equalTo(
                    TransmissionGearPosition.GearPosition.FIRST));
    }

    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }
}
