package com.openxc.messages.formatters;

import java.util.HashMap;

import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.messages.VehicleMessage;
import com.openxc.messages.CanMessage;
import com.openxc.messages.Command;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public abstract class AbstractFormatterTest {

    protected abstract void serializeDeserializeAndCheckEqual(
            VehicleMessage message);

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
                    Command.CommandType.DEVICE_ID));
    }

    @Test
    public void serializeCommandResponseWithMessage() {
        serializeDeserializeAndCheckEqual(new CommandResponse(
                    Command.CommandType.DEVICE_ID, "bar"));
    }

    @Test
    public void serializeCommand() {
        serializeDeserializeAndCheckEqual(new Command(
                    Command.CommandType.VERSION));
    }

    @Test
    public void serializeCommandWithDiagnosticRequest() {
        DiagnosticRequest request = new DiagnosticRequest(1, 2, 3, 4);
        serializeDeserializeAndCheckEqual(new Command(request));
    }

    @Test
    public void serializeCanMessage() {
        serializeDeserializeAndCheckEqual(new CanMessage(1, 2, new byte[]{1,2,3,4}));
    }

    @Test
    public void serializeSimpleMessage() {
        serializeDeserializeAndCheckEqual(new SimpleVehicleMessage("foo", "bar"));
    }

    @Test
    public void serializeEventedSimpleMessage() {
        serializeDeserializeAndCheckEqual(new EventedSimpleVehicleMessage(
                    "foo", "bar", "baz"));
    }

    @Test
    public void serializeNamedMessage() {
        serializeDeserializeAndCheckEqual(new NamedVehicleMessage("foo"));
    }

    // TODO check timestamp gets serialized
}
