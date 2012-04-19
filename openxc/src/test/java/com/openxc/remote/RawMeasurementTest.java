package com.openxc.remote;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class RawMeasurementTest {
    RawMeasurement measurement;

    @Test
    public void testValue() {
        measurement = new RawMeasurement(new Double(42.0));
    }

    @Test
    public void testValidity() {
        measurement = new RawMeasurement(new Double(42));
        assertTrue(measurement.isValid());

        measurement = new RawMeasurement();
        assertFalse(measurement.isValid());
   }

   @Test
   public void testHasAge() {
        measurement = new RawMeasurement(new Double(42));
        assertTrue(measurement.getTimestamp() > 0);
   }
}
