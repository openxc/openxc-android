package com.openxc;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;

import com.openxc.NoValueException;

import com.openxc.remote.RawMeasurement;

import com.openxc.TestUtils;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.containsString;

import com.openxc.units.Meter;
import com.openxc.util.Range;
import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.NoRangeException;

public class MeasurementTest extends TestCase {
    TestMeasurement measurement;
    Range<Meter> range;

    @Override
    public void setUp() {
        try {
            BaseMeasurement.getIdForClass(TestMeasurement.class);
        } catch (UnrecognizedMeasurementTypeException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        range = new Range<Meter>(new Meter(0.0), new Meter(101.2));
        measurement = new TestMeasurement(new Meter(10.1), range);
    }

    public void testGet() {
        assertThat(measurement.getValue().doubleValue(), equalTo(10.1));
    }

    public void testHasRange() {
        assertTrue(measurement.hasRange());
    }

    public void testGetRange() throws NoRangeException {
        assertThat(measurement.getRange(), equalTo(range));
    }

    public void testAgeIsPositive() {
        TestUtils.pause(10);
        assertThat(measurement.getAge(), greaterThan(0.0));
    }

    public void testEquality() {
        TestMeasurement anotherMeasurement =
            new TestMeasurement(new Meter(10.1), range);
        TestMeasurement inequalMeasurement  =
            new TestMeasurement(new Meter(12.0), range);
        assertTrue(measurement.equals(anotherMeasurement));
        assertFalse(measurement.equals(inequalMeasurement));
    }

    public void testSerialize() {
        measurement = new TestMeasurement(10.1);
        String data = measurement.serialize();
        assertThat(data, containsString(TestMeasurement.ID));
        assertThat(data, containsString(
                    measurement.getSerializedValue().toString()));
    }

    public void testToRaw() {
        measurement = new TestMeasurement(10.1);
        RawMeasurement raw = measurement.toRaw();
        assertEquals(raw.getName(), TestMeasurement.ID);
        assertEquals(raw.getValue(), measurement.getValue().doubleValue());
    }

    public void testDeserialize() throws NoValueException,
           UnrecognizedMeasurementTypeException {
        measurement = new TestMeasurement(10.1);
        assertEquals(BaseMeasurement.deserialize(measurement.serialize()),
                measurement);
    }

    public static class TestMeasurement extends BaseMeasurement<Meter> {
        public final static String ID = "test_generic_name";

        public TestMeasurement(Meter value, Range<Meter> range) {
            super(value, range);
        }

        public TestMeasurement(Number value) {
            super(new Meter(value));
        }

        @Override
        public String getGenericName() {
            return ID;
        }
    }
}
