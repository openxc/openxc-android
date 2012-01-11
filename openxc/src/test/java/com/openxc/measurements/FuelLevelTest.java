package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import com.openxc.units.Percentage;
import com.openxc.measurements.NoValueException;

public class FuelLevelTest {
    FuelLevel measurement;

    @Before
    public void setUp() {
        measurement = new FuelLevel(new Percentage(1.0));
    }

    @Test
    public void testGet() throws NoValueException {
        assertThat(measurement.getValue().doubleValue(), equalTo(1.0));
    }

    @Test
    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }

    @Test
    public void testHasId() {
        assertNotNull(FuelLevel.ID);
    }
}
