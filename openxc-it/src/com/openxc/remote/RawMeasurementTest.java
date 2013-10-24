package com.openxc.remote;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.openxc.TestUtils;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;

public class RawMeasurementTest extends TestCase {
    RawMeasurement measurement;
    final static String measurementName = "measurement_type";
    final static Double measurementValue = Double.valueOf(42.0);

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
        TestUtils.pause(10);
        assertEquals(timestamp, measurement.getTimestamp(), 0);
    }

    public void testInvalidTimestampGetsTimestampped() {
        measurement = new RawMeasurement(measurementName, measurementValue,
                null, 0);
        assertTrue(measurement.isTimestamped());
        assertFalse(0 == measurement.getTimestamp());
    }

    public void testUntimestamp() {
        measurement = new RawMeasurement(measurementName, measurementValue);
        assertTrue(measurement.isTimestamped());
        measurement.untimestamp();
        assertFalse(measurement.isTimestamped());
    }

    public void testSerializeWithoutTimestamp() {
        measurement = new RawMeasurement(measurementName, measurementValue);
        measurement.untimestamp();
        String serialized = measurement.serialize();
        assertFalse(serialized.contains("timestamp"));
    }

    public void testDeserialize() {
        try {
            measurement = new RawMeasurement(
                    "{\"name\": \"" + measurementName + "\", \"value\": " +
                    measurementValue.toString() + "}");
        } catch(UnrecognizedMeasurementTypeException e) {}
        assertEquals(measurement.getName(), measurementName);
        assertEquals(measurement.getValue(), measurementValue);
    }

    public void testDeserializeInvalidJson() {
        try {
            new RawMeasurement("{\"name\":");
        } catch(UnrecognizedMeasurementTypeException e) {
            return;
        }
        Assert.fail();
    }

    public void testDeserializeMissingAttribute() {
        try {
            new RawMeasurement("{\"name\": \"" + measurementName + "\"}");
        } catch(UnrecognizedMeasurementTypeException e) {
            return;
        }
        Assert.fail();
    }
}
