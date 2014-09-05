package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import junit.framework.TestCase;

import com.openxc.units.Percentage;

public class AcceleratorPedalPositionTest extends TestCase {
    AcceleratorPedalPosition measurement;

    @Override
    public void setUp() {
        measurement = new AcceleratorPedalPosition(new Percentage(2.0));
    }

    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(2.0));
    }

    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }
}
