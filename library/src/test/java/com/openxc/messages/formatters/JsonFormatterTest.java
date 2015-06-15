package com.openxc.messages.formatters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.messages.Command;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.DiagnosticResponse.NegativeResponseCode;
import com.openxc.messages.formatters.ByteAdapter;
import com.openxc.messages.formatters.JsonFormatter;

@RunWith(RobolectricTestRunner.class)
public class JsonFormatterTest extends AbstractFormatterTestBase {
    JsonFormatter formatter = new JsonFormatter();
    String messageName = "foo";
    Double value = Double.valueOf(42);

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
    public void testDeserializeDiagnosticResponseFromJsonString() throws UnrecognizedMessageTypeException {
        String data = "{\"bus\":1,\"id\":2028,\"mode\":1,\"success\":true,\"pid\":64,\"payload\":\"0x40800020\"}";
        DiagnosticResponse response = (DiagnosticResponse) JsonFormatter.deserialize(data);
        assertEquals(response.getBusId(), 1);
        assertEquals(response.getId(), 2028);
        assertEquals(response.getMode(), 1);
        assertEquals(response.isSuccessful(), true);
        assertEquals(response.getPid().intValue(), 64);
        assertEquals(ByteAdapter.byteArrayToHexString(response.getPayload()), "40800020");
    }

    @Test
    public void testSerializeAndDeserializeDiagnosticResponse() throws UnrecognizedMessageTypeException {
        serializeDeserializeAndCheckEqual(new DiagnosticResponse(
                1, 2028, 1, 64, ByteAdapter.hexStringToByteArray("40800020"), null, 12 ));
    }

    @Test
    public void testSerializeAndDeserializeDiagnosticResponseNoPayload() throws UnrecognizedMessageTypeException {
        serializeDeserializeAndCheckEqual(new DiagnosticResponse(
                1, 2028, 1, 64, null, null, 12 ));
    }

    @Test
    public void testDeserialize() throws UnrecognizedMessageTypeException {
        VehicleMessage message = JsonFormatter.deserialize(
                "{\"name\": \"" + messageName + "\", \"value\": " +
                value.toString() + "}");
        assertThat(message, instanceOf(SimpleVehicleMessage.class));
        SimpleVehicleMessage simpleMessage = (SimpleVehicleMessage) message;
        assertEquals(simpleMessage.getName(), messageName);
        assertEquals(simpleMessage.getValue(), value);
    }

    @Test
    public void testDeserializeInvalidJson() {
        try {
            JsonFormatter.deserialize("{\"name\":");
        } catch(UnrecognizedMessageTypeException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testSerializedTimestamp() {
        String serialized = new String(JsonFormatter.serialize(
                    new SimpleVehicleMessage(
                        Long.valueOf(1332432977835L), messageName, value)));
        // The timestamp is represented intenally as milliseconds, in a long,
        // but when serialized it is a floating point in seconds.
        assertTrue(serialized.contains("1332432977.835"));
    }

    @Test
    public void testSerializeWithoutTimestamp() {
        VehicleMessage message = new SimpleVehicleMessage(messageName, value);
        message.untimestamp();
        String serialized = new String(JsonFormatter.serialize(message));
        assertFalse(serialized.contains("timestamp"));
    }

    @Test
    public void blankExtrasNotInOutput() {
        VehicleMessage message = new SimpleVehicleMessage(messageName, value);
        String serialized = new String(JsonFormatter.serialize(message));
        assertFalse(serialized.contains("extras"));
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
    public void serializeWithExtras() {
        HashMap<String, Object> extras = new HashMap<>();
        extras.put("foo", "bar");
        extras.put("baz", 42.0);
        JsonFormatter.serialize(new VehicleMessage(extras));
    }

    @Test
    public void serializeCommandUsesStringCommand() {
        String serialized = JsonFormatter.serialize(new Command(
                    Command.CommandType.VERSION));
        assertThat(serialized, containsString("version"));
    }

    @Test
    public void serializeCommandResponseUsesStringCommand() {
        boolean status = true;
        String serialized = JsonFormatter.serialize(new CommandResponse(
                    Command.CommandType.VERSION, true));
        assertThat(serialized, containsString("version"));
    }

    @Test
    public void serializeCommandResponseContainsStatus() {
        boolean status = true;
        String serialized = JsonFormatter.serialize(new CommandResponse(
                    Command.CommandType.VERSION, true));
        assertThat(serialized, containsString("status"));
        assertThat(serialized, containsString("true"));
    }

    @Test
    public void defaultsNotIncluded() {
        DiagnosticRequest request = new DiagnosticRequest(1, 2, 3, 4);
        String serialized = JsonFormatter.serialize(request);
        assertThat(serialized, not(containsString("frequency")));
        assertThat(serialized, not(containsString("name")));
        assertThat(serialized, not(containsString("payload")));
        assertThat(serialized, not(containsString("multiple_responses")));

        request.setMultipleResponses(false);
        serialized = JsonFormatter.serialize(request);
        assertThat(serialized, containsString("multiple_responses"));
    }
}
