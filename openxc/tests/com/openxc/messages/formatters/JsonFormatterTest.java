package com.openxc.messages.formatters;

import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.SimpleVehicleMessage;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class JsonFormatterTest {
    JsonFormatter formatter = new JsonFormatter();
    SimpleVehicleMessage message;
    String messageName = "foo";
    Double value = Double.valueOf(42);

    @Test
    public void testSerializeWithoutTimestamp() {
        message = new SimpleVehicleMessage(messageName, value);
        message.untimestamp();
        String serialized = new String(formatter.serialize(message));
        assertFalse(serialized.contains("timestamp"));
    }

    @Test
    public void testDeserialize() {
        try {
            message = (SimpleVehicleMessage) formatter.deserialize(
                    "{\"name\": \"" + messageName + "\", \"value\": " +
                    value.toString() + "}");
        } catch(UnrecognizedMessageTypeException e) {}
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
    public void testSerializedTimestamp() {
        String serialized = new String(formatter.serialize(
                    new SimpleVehicleMessage(
                        Long.valueOf(1332432977835L), messageName, value)));
        assertTrue(serialized.contains("1332432977.835"));
    }
}
