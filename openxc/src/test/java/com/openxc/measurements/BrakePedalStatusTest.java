package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.openxc.measurements.NoValueException;

public class BrakePedalStatusTest {
    BrakePedalStatus measurement;

    @Before
    public void setUp() {
        measurement = new BrakePedalStatus(new Boolean(false));
    }

    @Test
    public void testDefaultConstructor() {
        assertThat(new BrakePedalStatus().isNone(), equalTo(true));
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
        assertNotNull(BrakePedalStatus.ID);
    }
}
