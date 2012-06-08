package com.openxc;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

import static junit.framework.Assert.assertTrue;

import com.openxc.units.Meter;
import com.openxc.util.Range;
import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.NoRangeException;

public class MeasurementTest extends TestCase {
    TestMeasurement measurement;
    Range<Meter> range;

    @Override
    public void setUp() {
        range = new Range<Meter>(new Meter(0.0), new Meter(101.2));
        measurement = new TestMeasurement(new Meter(10.0), range);
    }

    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(10.0));
    }

    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }

    public void testGetRange() throws NoRangeException {
        assertThat(measurement.getRange(), equalTo(range));
    }

    public void testAgeIsPositive() {
        assertThat(measurement.getAge(), greaterThan(0.0));
    }

    public void testEquality() {
        TestMeasurement anotherMeasurement =
            new TestMeasurement(new Meter(10.0), range);
        TestMeasurement inequalMeasurement  =
            new TestMeasurement(new Meter(12.0), range);
        assertTrue(measurement.equals(anotherMeasurement));
        assertFalse(measurement.equals(inequalMeasurement));
    }

    public void testSerialize() {
        assertThat(measurement.serialize(), equalTo(
                    "{\"name\": \"" + measurement.ID + "\", \"value\": "
                    + measurement.getValue() + "}"));
    }

    public void testDeserialize() {
        assertThat(BaseMeasurement.deserialize(measurement.serialize()),
                equalTo(measurement));
    }

    private class TestMeasurement extends BaseMeasurement<Meter> {
        public final static String ID = "test_generic_name";

        public TestMeasurement(Meter value, Range<Meter> range) {
            super(value, range);
        }
    }
}
