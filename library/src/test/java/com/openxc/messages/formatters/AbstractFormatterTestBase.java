package com.openxc.messages.formatters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.messages.CanMessage;
import com.openxc.messages.Command;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SerializationException;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;

@RunWith(RobolectricTestRunner.class)
public abstract class AbstractFormatterTestBase {

    protected abstract void serializeDeserializeAndCheckEqual(
            VehicleMessage message);

    @Test
    public void serializeFailedDiagnosticResponse() {
        int id = 42;
        int bus = 1;
        int mode = 2;
        int pid = 4;
        byte[] payload = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        double value = 42.0;
        serializeDeserializeAndCheckEqual(new DiagnosticResponse(bus, id, mode,
                pid, payload, null, value));
    }

    @Test
    public void serializeDiagnosticResponse() {
        serializeDeserializeAndCheckEqual(new DiagnosticResponse(
                    1, 2, 3, 4,
                    new byte[]{1,2,3,4}));
    }

    @Test
    public void serializeDiagnosticResponseWithValue() {
        serializeDeserializeAndCheckEqual(new DiagnosticResponse(
                    1, 2, 3, 4,
                    new byte[]{1,2,3,4}, null, 42.0));
    }

    @Test
    public void serializeCommandResponse() {
        serializeDeserializeAndCheckEqual(new CommandResponse(
                    Command.CommandType.DEVICE_ID, true));
    }

    @Test
    public void serializeCommandResponseWithMessage() {
        serializeDeserializeAndCheckEqual(new CommandResponse(
                    Command.CommandType.DEVICE_ID, true, "bar"));
        serializeDeserializeAndCheckEqual(new CommandResponse(
                    Command.CommandType.VERSION, true, "bar"));
    }

    @Test
    public void serializeCommand() {
        serializeDeserializeAndCheckEqual(new Command(
                    Command.CommandType.VERSION));
    }

    @Test
    public void serializeCommandWithDiagnosticRequest() {
        DiagnosticRequest request = new DiagnosticRequest(1, 2, 3, 4);
        serializeDeserializeAndCheckEqual(new Command(request, "add"));
    }

    @Test
    public void serializeCommandWithDiagnosticRequestNoPid() {
        DiagnosticRequest request = new DiagnosticRequest(1, 2, 3);
        serializeDeserializeAndCheckEqual(new Command(request, "add"));
    }

    @Test
    public void serializeCanMessage() {
        serializeDeserializeAndCheckEqual(new CanMessage(1, 2, new byte[]{1,2,3,4}));
    }

    @Test
    public void serializeSimpleMessage() {
        serializeDeserializeAndCheckEqual(new SimpleVehicleMessage("foo", "bar"));
        serializeDeserializeAndCheckEqual(new SimpleVehicleMessage("foo", false));
        serializeDeserializeAndCheckEqual(new SimpleVehicleMessage("foo", 42.0));
    }

    @Test
    public void serializeEventedSimpleMessage() {
        serializeDeserializeAndCheckEqual(new EventedSimpleVehicleMessage(
                    "foo", "bar", "baz"));
        serializeDeserializeAndCheckEqual(new EventedSimpleVehicleMessage(
                    "foo", "bar", false));
        serializeDeserializeAndCheckEqual(new EventedSimpleVehicleMessage(
                    "foo", "bar", 42.0));
    }

    @Test
    public void serializeNamedMessage() {
        serializeDeserializeAndCheckEqual(new NamedVehicleMessage("foo"));
    }

    @Test
    public void serializeDiagnosticRequestWithOptional() {
        DiagnosticRequest request = new DiagnosticRequest(1, 2, 3, 4);
        request.setPayload(new byte[]{1,2,3,4});
        request.setMultipleResponses(false);
        request.setFrequency(2.0);
        request.setName("foo");
        serializeDeserializeAndCheckEqual(new Command(request, "add"));
    }

    @Test(expected=SerializationException.class)
    public void serializeEmptyVehicleMessage()
            throws SerializationException {
        BinaryFormatter.serialize(new VehicleMessage());
    }
}
