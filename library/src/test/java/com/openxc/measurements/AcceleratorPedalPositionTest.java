package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.openxc.units.Percentage;

public class AcceleratorPedalPositionTest {
    AcceleratorPedalPosition measurement;

    @Before
    public void setUp() {
        measurement = new AcceleratorPedalPosition(new Percentage(2.0));
    }

    @Test
    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(2.0));
    }

    @Test
    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }
}
