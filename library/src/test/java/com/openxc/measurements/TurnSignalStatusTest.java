package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TurnSignalStatusTest {
    TurnSignalStatus measurement;

    @Before
    public void setUp() {
        measurement = new TurnSignalStatus(
                TurnSignalStatus.TurnSignalPosition.LEFT);
    }

    @Test
    public void testGet() {
        assertThat(measurement.getValue().enumValue(),
                equalTo(TurnSignalStatus.TurnSignalPosition.LEFT));
    }

    @Test
    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }

    @Test
    public void testHasId() {
        assertNotNull(TurnSignalStatus.ID);
    }
}
