package com.openxc.measurements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.openxc.units.State;

public class VehicleButtonEventTest {
    VehicleButtonEvent measurement;

    @Before
    public void setUp() {
        measurement = new VehicleButtonEvent(
                new State<VehicleButtonEvent.ButtonId>(
                    VehicleButtonEvent.ButtonId.OK),
                new State<VehicleButtonEvent.ButtonAction>(
                    VehicleButtonEvent.ButtonAction.PRESSED));
    }

    @Test
    public void testGet() {
        assertThat(measurement.getValue().enumValue(), equalTo(
                    VehicleButtonEvent.ButtonId.OK));
        assertThat(measurement.getAction().enumValue(), equalTo(
                    VehicleButtonEvent.ButtonAction.PRESSED));
    }

    @Test
    public void testHasNoRange() {
        assertFalse(measurement.hasRange());
    }

    @Test
    public void testHasId() {
        assertNotNull(VehicleButtonEvent.ID);
    }
}
