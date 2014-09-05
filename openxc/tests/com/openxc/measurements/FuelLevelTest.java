package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import junit.framework.TestCase;

import com.openxc.units.Percentage;

public class FuelLevelTest extends TestCase {
    FuelLevel measurement;

    @Override
    public void setUp() {
        measurement = new FuelLevel(new Percentage(1.0));
    }

    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(1.0));
    }

    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }
}
