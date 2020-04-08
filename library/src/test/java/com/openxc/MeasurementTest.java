package com.openxc;

import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.VehicleDoorStatus;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.units.Meter;
import com.openxc.util.Range;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MeasurementTest {
    TestMeasurement measurement;
    Range<Meter> range;

    @Before
    public void setUp() {
        range = new Range<Meter>(new Meter(0.0), new Meter(101.2));
        measurement = new TestMeasurement(new Meter(10.1), range);
    }

    @Test
    public void getDouble() {
        assertThat(measurement.getValue().doubleValue(), equalTo(10.1));
    }

    @Test
    public void hasRange() {
        assertTrue(measurement.hasRange());
    }

    @Test
    public void getRange() {
        assertThat(measurement.getRange(), equalTo(range));
    }

    @Test
    public void ageIsPositive() {
        TestUtils.pause(10);
        assertThat(measurement.getAge(), greaterThan(Long.valueOf(0)));
    }

    @Test
    public void equality() {
        TestMeasurement anotherMeasurement =
            new TestMeasurement(new Meter(10.1), range);
        TestMeasurement inequalMeasurement  =
            new TestMeasurement(new Meter(12.0), range);
        assertTrue(measurement.equals(anotherMeasurement));
        assertFalse(measurement.equals(inequalMeasurement));
    }

    @Test
    public void toVehicleMessage() {
        measurement = new TestMeasurement(10.1);
        VehicleMessage message = measurement.toVehicleMessage();
        assertTrue(message instanceof SimpleVehicleMessage);
        SimpleVehicleMessage simpleMessage = message.asSimpleMessage();
        assertEquals(TestMeasurement.ID, simpleMessage.getName());
        assertEquals(simpleMessage.getValue(), measurement.getValue().doubleValue());
    }

    @Test
    public void eventedToVehicleMessage() {
        VehicleDoorStatus doorMeasurement = new VehicleDoorStatus(
                VehicleDoorStatus.DoorId.DRIVER, true);
        VehicleMessage message = doorMeasurement.toVehicleMessage();
        assertTrue(message instanceof EventedSimpleVehicleMessage);
        EventedSimpleVehicleMessage eventedMessage = message.asEventedMessage();
        assertEquals(VehicleDoorStatus.ID,eventedMessage.getName());
        assertEquals(eventedMessage.getValue(), doorMeasurement.getValue().toString());
        assertEquals(eventedMessage.getEvent(), doorMeasurement.getEvent().booleanValue());
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
