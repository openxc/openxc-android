package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.VehicleDoorStatus;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.units.Meter;
import com.openxc.util.Range;

@RunWith(RobolectricTestRunner.class)
public class BaseMeasurementTest {
    Range<Meter> range;
    Double value = Double.valueOf(42);
    SimpleVehicleMessage message;

    @Before
    public void setUp() throws UnrecognizedMeasurementTypeException {
        message = new SimpleVehicleMessage(VehicleSpeed.ID, value);
    }

    @Test
    public void buildFromMessage()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        VehicleSpeed measurement = new VehicleSpeed(value);
        Measurement deserializedMeasurement =
            BaseMeasurement.getMeasurementFromMessage(message);
        assertThat(deserializedMeasurement, instanceOf(VehicleSpeed.class));
        VehicleSpeed vehicleSpeed = (VehicleSpeed) deserializedMeasurement;
        assertThat(vehicleSpeed, equalTo(measurement));
    }

    public class NewMeasurement extends BaseMeasurement<Meter> {
        public final static String ID = "new_measurement";

        public NewMeasurement() {
            super(new Meter(42.0));
        }

        public String getGenericName() {
            return ID;
        }
    }

    public class PrivateIdFieldMeasurement extends BaseMeasurement<Meter> {
        @SuppressWarnings("unused")
        private final static String ID = "new_measurement";

        public PrivateIdFieldMeasurement() {
            super(new Meter(42.0));
        }

        public String getGenericName() {
            return "foo";
        }
    }

    public class InvalidMeasurementType extends BaseMeasurement<Meter> {
        public InvalidMeasurementType() {
            super(new Meter(42.0));
        }

        public String getGenericName() {
            return "foo";
        }
    }

    @Test
    public void getKeyForUnrecognizedMeasurementWithProperId()
            throws UnrecognizedMeasurementTypeException {
        assertThat(BaseMeasurement.getKeyForMeasurement(NewMeasurement.class),
                equalTo(new NamedVehicleMessage(NewMeasurement.ID).getKey()));
    }

    @Test(expected=UnrecognizedMeasurementTypeException.class)
    public void getKeyForMeasurementMissingIdField()
            throws UnrecognizedMeasurementTypeException {
        BaseMeasurement.getKeyForMeasurement(InvalidMeasurementType.class);
    }

    @Test(expected=UnrecognizedMeasurementTypeException.class)
    public void getKeyForMeasurementPrivateID()
            throws UnrecognizedMeasurementTypeException {
        BaseMeasurement.getKeyForMeasurement(PrivateIdFieldMeasurement.class);
    }

    @Test
    public void buildFromMessageWithInteger()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        message = new SimpleVehicleMessage(VehicleSpeed.ID, Integer.valueOf(42));
        VehicleSpeed measurement = new VehicleSpeed(value);
        Measurement deserializedMeasurement =
            BaseMeasurement.getMeasurementFromMessage(message);
        assertThat(deserializedMeasurement, instanceOf(VehicleSpeed.class));
        VehicleSpeed vehicleSpeed = (VehicleSpeed) deserializedMeasurement;
        assertThat(vehicleSpeed, equalTo(measurement));
    }

    @Test(expected=NoValueException.class)
    public void buildFromNull() throws NoValueException,
           UnrecognizedMeasurementTypeException {
        BaseMeasurement.getMeasurementFromMessage(VehicleSpeed.class, null);
    }

    @Test
    public void buildEventedFromMessage()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        VehicleDoorStatus measurement = new VehicleDoorStatus("driver", false);
        VehicleMessage eventedMessage = measurement.toVehicleMessage();
        Measurement deserializedMeasurement =
            BaseMeasurement.getMeasurementFromMessage(
                    eventedMessage.asSimpleMessage());
        assertThat(deserializedMeasurement, instanceOf(VehicleDoorStatus.class));
        VehicleDoorStatus doorStatus = (VehicleDoorStatus) deserializedMeasurement;
        assertThat(doorStatus, equalTo(measurement));
    }

    @Test(expected=UnrecognizedMeasurementTypeException.class)
    public void buildFromUnrecognizedMessage()
            throws NoValueException, UnrecognizedMeasurementTypeException {
        message = new SimpleVehicleMessage("foo", value);
        BaseMeasurement.getMeasurementFromMessage(message);
    }

    @Test
    public void getBirthtime() {
        VehicleSpeed measurement = new VehicleSpeed(value);
        assertThat(measurement.getBirthtime(), notNullValue());
    }

    @Test
    public void setAndGetBirthtime() {
        VehicleSpeed measurement = new VehicleSpeed(value);
        measurement.setTimestamp(1000);
        assertEquals(measurement.getBirthtime(), 1000);
    }

    @Test
    public void sameEquals() {
        VehicleSpeed measurement = new VehicleSpeed(value);
        assertEquals(measurement, measurement);
    }

    @Test
    public void nullNotEqual() {
        VehicleSpeed measurement = new VehicleSpeed(value);
        assertThat(measurement, not(equalTo(null)));
    }

    @Test
    public void differentClassSameValueNotEqual() {
        VehicleSpeed measurement = new VehicleSpeed(value);
        EngineSpeed otherMeasurement = new EngineSpeed(value);
        assertFalse(measurement.equals(otherMeasurement));
    }

    @Test
    public void toStringNotNull() {
        VehicleSpeed measurement = new VehicleSpeed(value);
        assertThat(measurement.toString(), notNullValue());
    }
}
