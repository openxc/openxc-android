package com.openxc;

import java.net.URI;

import junit.framework.Assert;

import android.content.Intent;
import android.os.RemoteException;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.HighBeamStatus;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.TorqueAtTransmission;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleDoorStatus;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.WindshieldWiperStatus;
import com.openxc.sources.trace.TraceVehicleDataSource;
import com.openxc.sources.DataSourceException;

public class MeasurementsTest extends ServiceTestCase<VehicleManager> {
    VehicleManager service;
    URI traceUri;
    TraceVehicleDataSource source;

    public MeasurementsTest() {
        super(VehicleManager.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        traceUri = TestUtils.copyToStorage(getContext(), R.raw.tracejson,
                "trace.json");
    }

    // Due to bugs and or general crappiness in the ServiceTestCase, you will
    // run into many unexpected problems if you start the service in setUp - see
    // this blog post for more details:
    // http://convales.blogspot.de/2012/07/never-start-or-shutdown-service-in.html
    private void prepareServices() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleManager.class);
        service = ((VehicleManager.VehicleBinder)
                bindService(startIntent)).getService();
        service.waitUntilBound();
        try {
            source = new TraceVehicleDataSource(getContext(), traceUri);
            service.addSource(source);
        } catch(DataSourceException e) {
            Assert.fail();
        }
        TestUtils.pause(100);
    }

    @Override
    protected void tearDown() throws Exception {
        if(service != null) {
            service.removeSource(source);
        }
        super.tearDown();
    }

    private void checkReceivedMeasurement(Measurement measurement) {
        assertNotNull(measurement);
    }

    @MediumTest
    public void testGetSpeed() throws UnrecognizedMeasurementTypeException,
            NoValueException, RemoteException, InterruptedException {
        prepareServices();
        VehicleSpeed measurement = (VehicleSpeed)
                service.get(VehicleSpeed.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 42.0);
    }

    @MediumTest
    public void testGetSteeringWheelAngle()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        prepareServices();
        SteeringWheelAngle measurement = (SteeringWheelAngle)
                service.get(SteeringWheelAngle.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 94.1);
    }

    @MediumTest
    public void testGetTorqueAtTransmission()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        prepareServices();
        TorqueAtTransmission measurement = (TorqueAtTransmission)
                service.get(TorqueAtTransmission.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 232.1);
    }

    @MediumTest
    public void testGetOdometer()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        prepareServices();
        Odometer measurement = (Odometer) service.get(Odometer.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 124141.0);
    }

    @MediumTest
    public void testGetFuelLevel()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        prepareServices();
        FuelLevel measurement = (FuelLevel)
                service.get(FuelLevel.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 71.2);
    }

    @MediumTest
    public void testGetFuelConsumed()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        prepareServices();
        FuelConsumed measurement = (FuelConsumed)
                service.get(FuelConsumed.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 81.2);
    }

    @MediumTest
    public void testGetAcceleratorPedalPosition()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        prepareServices();
        AcceleratorPedalPosition measurement = (AcceleratorPedalPosition)
                service.get(AcceleratorPedalPosition.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 14.0);
    }

    @MediumTest
    public void testGetLatitude() throws UnrecognizedMeasurementTypeException,
            NoValueException, RemoteException, InterruptedException {
        prepareServices();
        Latitude measurement = (Latitude) service.get(Latitude.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 45.123);
    }

    @MediumTest
    public void testGetLongitude() throws UnrecognizedMeasurementTypeException,
            NoValueException, RemoteException, InterruptedException {
        prepareServices();
        Longitude measurement = (Longitude) service.get(Longitude.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 120.442);
    }

    @MediumTest
    public void testGetWindshieldWiperStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        prepareServices();
        WindshieldWiperStatus measurement = (WindshieldWiperStatus)
                service.get(WindshieldWiperStatus.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().booleanValue(), true);
    }

    @MediumTest
    public void testGetBrakePedalStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        prepareServices();
        BrakePedalStatus measurement = (BrakePedalStatus)
            service.get(BrakePedalStatus.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().booleanValue(), false);
    }

    @MediumTest
    public void testGetHeadlampStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        prepareServices();
        HeadlampStatus measurement = (HeadlampStatus)
            service.get(HeadlampStatus.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().booleanValue(), true);
    }

    @MediumTest
    public void testGetHighBeamStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        prepareServices();
        HighBeamStatus measurement = (HighBeamStatus)
            service.get(HighBeamStatus.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().booleanValue(), false);
    }

    @MediumTest
    public void testGetTransmissionGearPosition()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        prepareServices();
        TransmissionGearPosition measurement = (TransmissionGearPosition)
                service.get(TransmissionGearPosition.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().enumValue(),
                TransmissionGearPosition.GearPosition.FIRST);
    }

    @MediumTest
    public void testGetVehicleDoorStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        prepareServices();
        VehicleDoorStatus event = (VehicleDoorStatus)
                service.get(VehicleDoorStatus.class);
        checkReceivedMeasurement(event);
        assertEquals(event.getValue().enumValue(),
                VehicleDoorStatus.DoorId.DRIVER);
        // TODO
        // assertEquals(event.getEvent().booleanValue(), true);
    }
}

