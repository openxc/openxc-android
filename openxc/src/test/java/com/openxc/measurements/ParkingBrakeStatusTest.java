package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.openxc.remote.NoValueException;

public class ParkingBrakeStatusTest {
    ParkingBrakeStatus measurement;

    @Before
    public void setUp() {
        measurement = new ParkingBrakeStatus(new Boolean(false));
    }

    @Test
    public void testGet() throws NoValueException {
        assertThat(measurement.getValue().booleanValue(), equalTo(false));
    }

    @Test
    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }

    @Test
    public void testHasId() {
        assertNotNull(ParkingBrakeStatus.ID);
    }
}
