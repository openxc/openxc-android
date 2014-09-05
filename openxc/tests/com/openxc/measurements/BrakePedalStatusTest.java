package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import junit.framework.TestCase;


public class BrakePedalStatusTest extends TestCase {
    BrakePedalStatus measurement;

    @Override
    public void setUp() {
        measurement = new BrakePedalStatus(new Boolean(false));
    }

    public void testGet() {
        assertThat(measurement.getValue().booleanValue(), equalTo(false));
    }

    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }

    public void testGenericName() {
        assertEquals(measurement.getGenericName(), BrakePedalStatus.ID);
    }
}
