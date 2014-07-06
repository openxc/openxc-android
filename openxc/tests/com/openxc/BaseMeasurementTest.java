package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.VehicleDoorStatus;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.units.Meter;
import com.openxc.util.Range;

@Config(emulateSdk = 18, manifest = Config.NONE)
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

    @Test
    public void buildFromUnrecognizedMessage()
            throws NoValueException {
        message = new SimpleVehicleMessage("foo", value);
        try {
            BaseMeasurement.getMeasurementFromMessage(message);
        } catch(UnrecognizedMeasurementTypeException e) {
            return;
        }
        Assert.fail();
    }
}
