package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.measurements.*;
import com.openxc.messages.SimpleVehicleMessage;

@RunWith(RobolectricTestRunner.class)
public class MeasurementsTest {
    Double value = Double.valueOf(42);

    private void checkDeserializeSerialize(Measurement measurement) throws
            UnrecognizedMeasurementTypeException, NoValueException {
        SimpleVehicleMessage message = measurement.toVehicleMessage();
        Measurement deserializedMeasurement =
                BaseMeasurement.getMeasurementFromMessage(message);
        assertThat(deserializedMeasurement, instanceOf(measurement.getClass()));
        assertThat(deserializedMeasurement, equalTo(measurement));
    }

    @Test
    public void buildVehicleSpeed()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new VehicleSpeed(value));
    }

    @Test
    public void buildEngineSpeed()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new EngineSpeed(value));
    }

    @Test
    public void buildSteeringWheelAngle()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new SteeringWheelAngle(value));
    }

    @Test
    public void buildTorqueAtTransmission()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new TorqueAtTransmission(value));
    }

    @Test
    public void buildOdometer()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new Odometer(value));
    }

    @Test
    public void buildFuelLevel()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new FuelLevel(value));
    }

    @Test
    public void buildFuelConsumed()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new FuelConsumed(value));
    }

    @Test
    public void buildAcceleratorPedalPosition()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new AcceleratorPedalPosition(value));
    }

    @Test
    public void buildLatitude()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new Latitude(value));
    }

    @Test
    public void buildLongitude()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new Longitude(value));
    }

    @Test
    public void buildWindshieldWiperStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new WindshieldWiperStatus(true));
    }

    @Test
    public void buildParkingBrakeStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new ParkingBrakeStatus(true));
    }

    @Test
    public void buildBrakePedalStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new BrakePedalStatus(true));
    }

    @Test
    public void buildHeadlampStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new HeadlampStatus(false));
    }

    @Test
    public void buildHighBeamStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new HighBeamStatus(true));
    }

    @Test
    public void buildIgnitionStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new IgnitionStatus(
                    IgnitionStatus.IgnitionPosition.RUN));
    }

    @Test
    public void buildTurnSignalStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new TurnSignalStatus(
                    TurnSignalStatus.TurnSignalPosition.LEFT));
    }

    @Test
    public void buildTransmissionGearPosition()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new TransmissionGearPosition(
                    TransmissionGearPosition.GearPosition.FIRST));
    }

    @Test
    public void buildVehicleButtonEvent()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new VehicleButtonEvent("left", "idle"));
    }

    @Test
    public void buildVehicleDoorStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        checkDeserializeSerialize(new VehicleDoorStatus("driver", true));
    }
}
