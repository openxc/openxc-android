package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.openxc.remote.NoValueException;

public class WindshieldWiperStatusTest {
    WindshieldWiperStatus measurement;

    @Before
    public void setUp() {
        measurement = new WindshieldWiperStatus(new Boolean(true));
    }

    @Test
    public void testGet() throws NoValueException {
        assertThat(measurement.getValue().booleanValue(), equalTo(true));
    }

    @Test
    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }

    @Test
    public void testHasId() {
        assertNotNull(WindshieldWiperStatus.ID);
    }
}
