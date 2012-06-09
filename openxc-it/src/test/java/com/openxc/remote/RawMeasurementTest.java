package com.openxc.remote;

import junit.framework.TestCase;

public class RawMeasurementTest extends TestCase {
    RawMeasurement measurement;

    public void testValue() {
        measurement = new RawMeasurement("measurement_type", new Double(42.0));
    }

    public void testHasAge() {
        measurement = new RawMeasurement("measurement_type", new Double(42));
        assertTrue(measurement.getTimestamp() > 0);
    }

    public void testStopsAging() {
        measurement = new RawMeasurement("measurement_type", new Double(42));
        double timestamp = measurement.getTimestamp();
        pause(10);
        assertEquals(timestamp, measurement.getTimestamp(), 0);
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }
}
