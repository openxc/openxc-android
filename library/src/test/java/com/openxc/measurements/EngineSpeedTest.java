package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Before;
import org.junit.Test;

import com.openxc.units.RotationsPerMinute;

public class EngineSpeedTest {
    EngineSpeed measurement;

    @Before
    public void setUp() {
        measurement = new EngineSpeed(new RotationsPerMinute(1.0));
    }

    @Test
    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(1.0));
    }

    @Test
    public void testConstructFromNumber() {
        EngineSpeed other = new EngineSpeed(1.0);
        assertThat(other, equalTo(measurement));
    }
}
