package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class WindshieldWiperStatusTest {
    WindshieldWiperStatus measurement;

    @Before
    public void setUp() {
        measurement = new WindshieldWiperStatus(new Boolean(true));
    }

    @Test
    public void testGet() {
        assertThat(measurement.getValue().booleanValue(), equalTo(true));
    }

    @Test
    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }
}
