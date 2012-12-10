package com.openxc.measurements;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.openxc.units.RotationsPerMinute;

public class EngineSpeedTest extends TestCase {
    EngineSpeed measurement;

    @Override
    public void setUp() {
        measurement = new EngineSpeed(new RotationsPerMinute(1.0));
    }

    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(1.0));
    }
}
