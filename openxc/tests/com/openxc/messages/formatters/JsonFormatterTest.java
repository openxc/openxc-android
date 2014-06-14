package com.openxc.messages.formatters;

import java.util.HashMap;

import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.messages.InvalidMessageFieldsException;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.CanMessage;
import com.openxc.messages.Command;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class JsonFormatterTest {
    JsonFormatter formatter = new JsonFormatter();
    String messageName = "foo";
    Double value = Double.valueOf(42);

    private void serializeDeserializeAndCheckEqual(
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
                    new DiagnosticRequest(1, 2, 3, 4),
                    new byte[]{1,2,3,4}, true));
    }

    @Test
    public void serializeDiagnosticRequest() {
        serializeDeserializeAndCheckEqual(new DiagnosticRequest(1, 2, 3, 4,
                    new byte[]{1,2,3,4}, false, 1.2, 42, 2, "foo"));
    }

    @Test
    public void serializeCommandResponse() {
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
    public void serializeNamedMessageWithValues()
            throws InvalidMessageFieldsException {
        HashMap<String, Object> data = new HashMap<>();
        data.put("foo", "bar");
        data.put("baz", 42);
        serializeDeserializeAndCheckEqual(new NamedVehicleMessage("foo", data));
    }

    @Test
    public void serializeNamedMessage() {
        serializeDeserializeAndCheckEqual(new NamedVehicleMessage("foo"));
    }

    @Test
    public void serializeVehicleMessageArbitraryFields() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("foo", "bar");
        data.put("baz", 42);
        VehicleMessage message = new VehicleMessage(data);
        serializeDeserializeAndCheckEqual(message);
    }

    @Test
    public void serializeEmptyVehicleMessage() {
        serializeDeserializeAndCheckEqual(new VehicleMessage());
    }

    @Test
    public void testSerializeWithoutTimestamp() {
        VehicleMessage message = new SimpleVehicleMessage(messageName, value);
        message.untimestamp();
        String serialized = new String(formatter.serialize(message));
        assertFalse(serialized.contains("timestamp"));
    }

    @Test
    public void testDeserialize() throws UnrecognizedMessageTypeException {
        SimpleVehicleMessage message = (SimpleVehicleMessage) formatter.deserialize(
                "{\"name\": \"" + messageName + "\", \"value\": " +
                value.toString() + "}");
        assertEquals(message.getName(), messageName);
        assertEquals(message.getValue(), value);
    }

    @Test
    public void testDeserializeInvalidJson() {
        try {
            formatter.deserialize("{\"name\":");
        } catch(UnrecognizedMessageTypeException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testSerializedTimestamp() throws InvalidMessageFieldsException {
        String serialized = new String(formatter.serialize(
                    new SimpleVehicleMessage(
                        Long.valueOf(1332432977835L), messageName, value)));
        assertTrue(serialized.contains("1332432977.835"));
    }
}
