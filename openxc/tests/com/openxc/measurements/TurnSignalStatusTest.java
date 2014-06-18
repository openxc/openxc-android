package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import junit.framework.TestCase;


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
