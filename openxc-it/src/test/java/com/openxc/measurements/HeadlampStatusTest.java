package com.openxc.measurements;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class HeadlampStatusTest extends TestCase {
    HeadlampStatus measurement;

    @Override
    public void setUp() {
        measurement = new HeadlampStatus(new Boolean(false));
    }

    public void testGet() {
        assertThat(measurement.getValue().booleanValue(), equalTo(false));
    }

    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }
}
