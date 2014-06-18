package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import junit.framework.Assert;
import junit.framework.TestCase;

import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.units.Meter;
import com.openxc.util.Range;

public class BaseMeasurementTest extends TestCase {
    Range<Meter> range;
    Double value = Double.valueOf(42);
    String name = "vehicle_speed";
    SimpleVehicleMessage message;

    @Override
    public void setUp() throws UnrecognizedMeasurementTypeException {
        message = new SimpleVehicleMessage(name, value);
        BaseMeasurement.getIdForClass(VehicleSpeed.class);
    }

    public void testBuildFromMessage()
            throws UnrecognizedMeasurementTypeException, NoValueException {
        Measurement measurement =
            BaseMeasurement.getMeasurementFromMessage(message);
        assertTrue(measurement instanceof VehicleSpeed);
        VehicleSpeed vehicleSpeed = (VehicleSpeed) measurement;
        assertThat(vehicleSpeed.getValue().doubleValue(), equalTo(value));
    }

    public void testBuildFromUnrecognizedMessage()
            throws NoValueException {
        message = new SimpleVehicleMessage("foo", value);
        try {
            Measurement measurement =
                BaseMeasurement.getMeasurementFromMessage(message);
        } catch(UnrecognizedMeasurementTypeException e) {
            return;
        }
        Assert.fail();
    }
}
