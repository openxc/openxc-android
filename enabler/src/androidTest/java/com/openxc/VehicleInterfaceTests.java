package com.openxc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.openxc.interfaces.TestVehicleInterface;
import com.openxc.interfaces.VehicleInterface;
import com.openxc.measurements.TurnSignalStatus;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.remote.VehicleService;
import com.openxc.remote.VehicleServiceException;
import com.openxc.sinks.DataSinkException;

public class VehicleInterfaceTests extends ServiceTestCase<VehicleManager> {
    VehicleManager service;
    VehicleInterface mTestInterface;

    public VehicleInterfaceTests() {
        super(VehicleManager.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mTestInterface = mock(VehicleInterface.class);
        when(mTestInterface.isConnected()).thenReturn(true);

        // if the service is already running (and thus may have old data
        // cached), kill it.
        getContext().stopService(new Intent(getContext(),
                    VehicleService.class));
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
        try {
            service.waitUntilBound();
            service.setVehicleInterface(TestVehicleInterface.class);
        } catch(VehicleServiceException e) { }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @MediumTest
    public void testSendMeasurement() throws
            UnrecognizedMeasurementTypeException, DataSinkException {
        prepareServices();
        assertTrue(service.send(new TurnSignalStatus(
                    TurnSignalStatus.TurnSignalPosition.LEFT)));
        // TODO how can we do these tests without a local interface? it's not
        // worth having that in the API *just* for testing
        // verify(mTestInterface).receive(Mockito.any(VehicleMessage.class));
    }

    @MediumTest
    public void testSendMessage() throws DataSinkException {
        prepareServices();
        assertTrue(service.send(new SimpleVehicleMessage("foo", "bar")));
        // TODO
        // verify(mTestInterface).receive(Mockito.any(VehicleMessage.class));
    }

    @MediumTest
    public void testSentMessageTimestamped() throws DataSinkException {
        prepareServices();
        VehicleMessage message = new SimpleVehicleMessage("foo", "bar");
        assertFalse(message.isTimestamped());
        assertTrue(service.send(message));
        assertTrue(message.isTimestamped());
    }

    @MediumTest
    public void testSendDiagnosticRequest() throws DataSinkException {
        prepareServices();
        DiagnosticRequest request = new DiagnosticRequest(1, 2, 3, 4);
        assertFalse(request.isTimestamped());
        assertTrue(service.send(request));
        // TODO
        // ArgumentCaptor<Command> argument = ArgumentCaptor.forClass(
                // Command.class);
        // verify(mTestInterface).receive(argument.capture());
        // assertTrue(request.isTimestamped());
        // Command command = argument.getValue();
        // assertEquals(command.getCommand(), Command.CommandType.DIAGNOSTIC_REQUEST);
        // assertNotNull(command.getDiagnosticRequest());
        // assertThat(command.getDiagnosticRequest(), equalTo(request));
    }
}

