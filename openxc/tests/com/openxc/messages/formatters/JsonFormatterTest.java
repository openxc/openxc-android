package com.openxc.messages.formatters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.SerializationException;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class JsonFormatterTest {
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
        assertTrue(serialized.contains("1.332432977835E9"));
    }

    @Test
    public void testSerializeWithoutTimestamp() {
        VehicleMessage message = new SimpleVehicleMessage(messageName, value);
        message.untimestamp();
        String serialized = new String(JsonFormatter.serialize(message));
        assertFalse(serialized.contains("timestamp"));
    }

    @Test
    public void serializeEmptyVehicleMessage() {
        // JsonFormatter allows blank messages
        serializeDeserializeAndCheckEqual(new VehicleMessage());
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
}
