package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import junit.framework.TestCase;

public class HighBeamStatusTest extends TestCase {
    HighBeamStatus measurement;

    @Override
    public void setUp() {
        measurement = new HighBeamStatus(new Boolean(false));
    }

    public void testGet() {
        assertThat(measurement.getValue().booleanValue(), equalTo(false));
    }

    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }
}
