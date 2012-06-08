package com.openxc;

import java.io.File;
import java.io.IOException;

import java.lang.InterruptedException;

import java.net.URISyntaxException;
import java.net.URI;

import org.apache.commons.io.FileUtils;

import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.HighBeamStatus;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.FineOdometer;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.NoValueException;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.TorqueAtTransmission;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.VehicleButtonEvent;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.WindshieldWiperStatus;

import com.openxc.sources.trace.TraceVehicleDataSource;

import com.openxc.VehicleManager;

import android.content.Intent;

import android.os.RemoteException;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.MediumTest;

import junit.framework.Assert;

public class MeasurementsTest extends ServiceTestCase<VehicleManager> {
    VehicleManager service;
    URI traceUri;

    public MeasurementsTest() {
        super(VehicleManager.class);
    }

    private void copyTraces() {
        try {
            traceUri = new URI("file:///sdcard/com.openxc/trace.json");
        } catch(URISyntaxException e) {
            Assert.fail("Couldn't construct resource URIs: " + e);
        }

        try {
            FileUtils.copyInputStreamToFile(
                    getContext().getResources().openRawResource(
                        R.raw.tracejson), new File(traceUri));
        } catch(IOException e) {}
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        copyTraces();

        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleManager.class);
        service = ((VehicleManager.VehicleBinder)
                bindService(startIntent)).getService();
        service.waitUntilBound();
        service.addSource(new TraceVehicleDataSource(getContext(), traceUri));
        pause(200);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if(service != null)  {
            service.initializeDefaultSources();
        }
    }

    private void checkReceivedMeasurement(Measurement measurement) {
        assertNotNull(measurement);
    }

    @MediumTest
    public void testGetSpeed() throws UnrecognizedMeasurementTypeException,
            NoValueException, RemoteException, InterruptedException {
        VehicleSpeed measurement = (VehicleSpeed)
                service.get(VehicleSpeed.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 42.0);
    }

    @MediumTest
    public void testGetSteeringWheelAngle()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        SteeringWheelAngle measurement = (SteeringWheelAngle)
                service.get(SteeringWheelAngle.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 94.1);
    }

    @MediumTest
    public void testGetTorqueAtTransmission()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        TorqueAtTransmission measurement = (TorqueAtTransmission)
                service.get(TorqueAtTransmission.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 232.1);
    }

    @MediumTest
    public void testGetOdometer()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        Odometer measurement = (Odometer) service.get(Odometer.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 124141.0);
    }

    @MediumTest
    public void testGetFineOdometer()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        FineOdometer measurement = (FineOdometer) service.get(
                FineOdometer.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 142.312423);
    }

    @MediumTest
    public void testGetFuelLevel()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        FuelLevel measurement = (FuelLevel)
                service.get(FuelLevel.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 71.2);
    }

    @MediumTest
    public void testGetFuelConsumed()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        FuelConsumed measurement = (FuelConsumed)
                service.get(FuelConsumed.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 81.2);
    }

    @MediumTest
    public void testGetAcceleratorPedalPosition()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        AcceleratorPedalPosition measurement = (AcceleratorPedalPosition)
                service.get(AcceleratorPedalPosition.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 14.0);
    }

    @MediumTest
    public void testGetLatitude() throws UnrecognizedMeasurementTypeException,
            NoValueException, RemoteException, InterruptedException {
        Latitude measurement = (Latitude) service.get(Latitude.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 45.123);
    }

    @MediumTest
    public void testGetLongitude() throws UnrecognizedMeasurementTypeException,
            NoValueException, RemoteException, InterruptedException {
        Longitude measurement = (Longitude) service.get(Longitude.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 120.442);
    }

    @MediumTest
    public void testGetWindshieldWiperStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        pause(300);
        WindshieldWiperStatus measurement = (WindshieldWiperStatus)
                service.get(WindshieldWiperStatus.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().booleanValue(), true);
    }

    @MediumTest
    public void testGetBrakePedalStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        pause(300);
        BrakePedalStatus measurement = (BrakePedalStatus)
            service.get(BrakePedalStatus.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().booleanValue(), false);
    }

    @MediumTest
    public void testGetHeadlampStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        pause(300);
        HeadlampStatus measurement = (HeadlampStatus)
            service.get(HeadlampStatus.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().booleanValue(), true);
    }

    @MediumTest
    public void testGetHighBeamStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        pause(300);
        HighBeamStatus measurement = (HighBeamStatus)
            service.get(HighBeamStatus.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().booleanValue(), false);
    }

    @MediumTest
    public void testGetTransmissionGearPosition()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        pause(300);
        TransmissionGearPosition measurement = (TransmissionGearPosition)
                service.get(TransmissionGearPosition.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().enumValue(),
                TransmissionGearPosition.GearPosition.FIRST);
    }

    @MediumTest
    public void testGetVehicleButtonEvent()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        pause(300);
        VehicleButtonEvent event = (VehicleButtonEvent)
                service.get(VehicleButtonEvent.class);
        checkReceivedMeasurement(event);
        assertEquals(event.getValue().enumValue(),
                VehicleButtonEvent.ButtonId.OK);
        assertEquals(event.getEvent().enumValue(),
                VehicleButtonEvent.ButtonAction.PRESSED);
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }
}

