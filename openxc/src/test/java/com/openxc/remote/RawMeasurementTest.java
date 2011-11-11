package com.openxc.remote;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class RawMeasurementTest {
    RawNumericalMeasurement numericalMeasurement;
    RawStateMeasurement stateMeasurement;

    @Test
    public void testNumericalValue() {
        numericalMeasurement = new RawNumericalMeasurement(new Double(42.0));
    }

    @Test
    public void testStateValue() {
        stateMeasurement = new RawStateMeasurement("MyState");
    }

    @Test
    public void testValidity() {
        numericalMeasurement = new RawNumericalMeasurement(new Double(42));
        assertTrue(numericalMeasurement.isValid());

        numericalMeasurement = new RawNumericalMeasurement();
        assertFalse(numericalMeasurement.isValid());
   }
}
