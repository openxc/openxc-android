package com.openxc.remote;

import junit.framework.TestCase;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertEquals;

public class RawMeasurementTest extends TestCase {
    RawMeasurement measurement;

    public void testValue() {
        measurement = new RawMeasurement(new Double(42.0));
    }

    public void testValidity() {
        measurement = new RawMeasurement(new Double(42));
        assertTrue(measurement.isValid());

        measurement = new RawMeasurement();
        assertFalse(measurement.isValid());
    }

    public void testHasAge() {
        measurement = new RawMeasurement(new Double(42));
        assertTrue(measurement.getTimestamp() > 0);
    }

    public void testStopsAging() {
        measurement = new RawMeasurement(new Double(42));
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
