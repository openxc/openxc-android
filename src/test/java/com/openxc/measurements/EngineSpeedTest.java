package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import com.openxc.units.RotationsPerMinute;
import com.openxc.measurements.NoValueException;

public class EngineSpeedTest {
    EngineSpeed measurement;

    @Before
    public void setUp() {
        measurement = new EngineSpeed(new RotationsPerMinute(1.0));
    }

    @Test
    public void testGet() throws NoValueException {
        assertThat(measurement.getValue().doubleValue(), equalTo(1.0));
    }

    @Test
    public void testHasId() {
        assertThat(measurement.getId(), equalTo("EngineSpeed"));
    }

}
