package com.openxc.measurements;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.openxc.units.NewtonMeter;

public class TorqueAtTransmissionTest extends TestCase {
    TorqueAtTransmission measurement;

    @Override
    public void setUp() {
        measurement = new TorqueAtTransmission(new NewtonMeter(1.0));
    }

    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(1.0));
    }

    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }
}
