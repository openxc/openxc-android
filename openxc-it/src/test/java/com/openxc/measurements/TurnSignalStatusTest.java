package com.openxc.measurements;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;


public class TurnSignalStatusTest extends TestCase {
    TurnSignalStatus measurement;

    @Override
    public void setUp() {
        measurement = new TurnSignalStatus(
                TurnSignalStatus.TurnSignalPosition.LEFT);
    }

    public void testGet() {
        assertThat(measurement.getValue().enumValue(),
                equalTo(TurnSignalStatus.TurnSignalPosition.LEFT));
    }

    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }

    public void testHasId() {
        assertNotNull(TurnSignalStatus.ID);
    }
}
