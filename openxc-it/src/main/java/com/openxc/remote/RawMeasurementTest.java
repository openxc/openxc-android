package com.openxc.remote;

import com.openxc.remote.RawMeasurement;

import junit.framework.TestCase;

public class RawMeasurementTest extends TestCase {
    RawMeasurement measurement;
    final static String measurementName = "measurement_type";
    final static Double measurementValue = new Double(42.0);

    public void testValue() {
        measurement = new RawMeasurement(measurementName, measurementValue);
    }

    public void testHasAge() {
        measurement = new RawMeasurement(measurementName, measurementValue);
        assertTrue(measurement.getTimestamp() > 0);
    }

    public void testStopsAging() {
        measurement = new RawMeasurement(measurementName, measurementValue);
        double timestamp = measurement.getTimestamp();
        pause(10);
        assertEquals(timestamp, measurement.getTimestamp(), 0);
    }

    public void testUntimestamp() {
        measurement = new RawMeasurement(measurementName, measurementValue);
        assertTrue(measurement.isTimestamped());
        measurement.untimestamp();
        assertFalse(measurement.isTimestamped());
    }

    public void testDeserialize() {
        measurement = new RawMeasurement(
                "{\"name\": \"" + measurementName + "\", \"value\": " +
                measurementValue.toString() + "}");
        assertEquals(measurement.getName(), measurementName);
        assertEquals(measurement.getValue(), measurementValue);
    }

    public void testDeserializeInvalidJson() {
        measurement = new RawMeasurement("{\"name\":");
        assertNull(measurement);
    }

    public void testDeserializeMissingAttribute() {
        measurement = new RawMeasurement("{\"name\": \"" +
                measurementName + "\"}");
        assertNull(measurement);
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }
}
