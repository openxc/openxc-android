package com.openxc.messages.formatters;

import java.util.HashMap;

import org.junit.Test;
import org.junit.Assert;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public abstract class AbstractFormatterTest {

    protected void serializeDeserializeAndCheckEqual(
            VehicleMessage originalMessage) {
        String serialized = JsonFormatter.serialize(originalMessage);
        assertFalse(serialized.isEmpty());

        try {
            VehicleMessage deserialized = JsonFormatter.deserialize(serialized);
            assertEquals(originalMessage, deserialized);
        } catch(UnrecognizedMessageTypeException e) {
            Assert.fail(e.toString());
        }
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
    public void serializeDiagnosticRequest() {
        DiagnosticRequest request = new DiagnosticRequest(1, 2, 3, 4);
        serializeDeserializeAndCheckEqual(request);
    }

    @Test
    public void serializeDiagnosticRequestWithOptional() {
        DiagnosticRequest request = new DiagnosticRequest(1, 2, 3, 4);
        request.setPayload(new byte[]{1,2,3,4});
        request.setMultipleResponses(false);
        request.setFrequency(2.0);
        request.setName("foo");
        serializeDeserializeAndCheckEqual(request);
    }

    @Test
    public void serializeCommandResponse() {
        serializeDeserializeAndCheckEqual(new CommandResponse("foo"));
    }

    @Test
    public void serializeCommandResponseWithMessage() {
        serializeDeserializeAndCheckEqual(new CommandResponse("foo", "bar"));
    }

    @Test
    public void serializeCommand() {
        serializeDeserializeAndCheckEqual(new Command("foo"));
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
    public void serializeNamedMessageWithExtras() {
        HashMap<String, Object> extras = new HashMap<>();
        extras.put("foo", "bar");
        extras.put("baz", 42.0);
        VehicleMessage message = new NamedVehicleMessage("foo");
        message.setExtras(extras);
        serializeDeserializeAndCheckEqual(message);
    }

    @Test
    public void serializeNamedMessage() {
        serializeDeserializeAndCheckEqual(new NamedVehicleMessage("foo"));
    }

    @Test
    public void serializeWithExtras() {
        HashMap<String, Object> extras = new HashMap<>();
        extras.put("foo", "bar");
        extras.put("baz", 42.0);
        VehicleMessage message = new VehicleMessage(extras);
        serializeDeserializeAndCheckEqual(message);
    }

    @Test
    public void serializeEmptyVehicleMessage() {
        serializeDeserializeAndCheckEqual(new VehicleMessage());
    }

    // TODO check timestamp gets serialized
}
