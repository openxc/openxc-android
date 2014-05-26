package com.openxc.messages.formatters;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.messages.SimpleVehicleMessage;

public class JsonFormatterTest extends TestCase {
    JsonFormatter formatter = new JsonFormatter();
    SimpleVehicleMessage message;
    String messageName = "foo";
    Integer value = Integer.valueOf(42);

    public void testSerializeWithoutTimestamp() {
        message = new SimpleVehicleMessage(messageName, value);
        message.untimestamp();
        String serialized = new String(formatter.serialize(message));
        assertFalse(serialized.contains("timestamp"));
    }

    public void testDeserialize() {
        try {
            message = (SimpleVehicleMessage) formatter.deserialize(
                    "{\"name\": \"" + messageName + "\", \"value\": " +
                    value.toString() + "}");
        } catch(UnrecognizedMeasurementTypeException e) {}
        assertEquals(message.getName(), messageName);
        assertEquals(message.getValue(), value);
    }

    public void testDeserializeInvalidJson() {
        try {
            formatter.deserialize("{\"name\":");
        } catch(UnrecognizedMeasurementTypeException e) {
            return;
        }
        Assert.fail();
    }

    public void testDeserializeMissingAttribute() {
        try {
            formatter.deserialize("{\"name\": \"" + messageName + "\"}");
        } catch(UnrecognizedMeasurementTypeException e) {
            return;
        }
        Assert.fail();
    }

    public void testSerializedTimestamp() {
        String serialized = new String(formatter.serialize(
                    new SimpleVehicleMessage(
                        Long.valueOf(1332432977835L), messageName, value)));
        assertTrue(serialized.contains("1332432977.835"));
    }
}
