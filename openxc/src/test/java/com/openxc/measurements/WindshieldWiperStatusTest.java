package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.openxc.measurements.NoValueException;

public class WindshieldWiperStatusTest {
    WindshieldWiperStatus measurement;

    @Before
    public void setUp() {
        measurement = new WindshieldWiperStatus(new Boolean(false));
    }

    @Test
    public void testDefaultConstructor() {
        assertThat(new WindshieldWiperStatus().isNone(), equalTo(true));
    }

    @Test
    public void testGet() throws NoValueException {
        assertThat(measurement.getValue().booleanValue(), equalTo(false));
    }

    @Test
    public void testHasRange() {
        assertFalse(measurement.hasRange());
    }

    @Test
    public void testHasId() {
        assertNotNull(WindshieldWiperStatus.ID);
    }
}
