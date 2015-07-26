package com.openxc.messages.formatters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.BinaryMessages;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.SerializationException;

@RunWith(RobolectricTestRunner.class)
public class BinaryFormatterTest extends AbstractFormatterTestBase {
    BinaryFormatter formatter = new BinaryFormatter();
    SimpleVehicleMessage message;
    String messageName = "foo";
    Double value = Double.valueOf(42);

    protected void serializeDeserializeAndCheckEqual(
            VehicleMessage originalMessage) {
        try {
            byte[] serialized = BinaryFormatter.serialize(originalMessage);
            assertThat(serialized.length, greaterThan(0));

            VehicleMessage deserialized = BinaryFormatter.deserialize(
                    new ByteArrayInputStream(serialized));
            assertEquals(originalMessage, deserialized);
        } catch(UnrecognizedMessageTypeException | SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deserializeNoErrors() throws IOException,
            UnrecognizedMessageTypeException {
        BinaryMessages.VehicleMessage.Builder builder =
            BinaryMessages.VehicleMessage.newBuilder();
        builder.setType(BinaryMessages.VehicleMessage.Type.SIMPLE);

        BinaryMessages.SimpleMessage.Builder messageBuilder =
                BinaryMessages.SimpleMessage.newBuilder();
        messageBuilder.setName(messageName);
        BinaryMessages.DynamicField.Builder fieldBuilder =
                BinaryMessages.DynamicField.newBuilder();
        fieldBuilder.setType(BinaryMessages.DynamicField.Type.NUM);
        fieldBuilder.setNumericValue(42);

        messageBuilder.setValue(fieldBuilder);
        builder.setSimpleMessage(messageBuilder);

        BinaryMessages.VehicleMessage serialized = builder.build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        serialized.writeTo(output);
        InputStream input = new ByteArrayInputStream(output.toByteArray());

        try {
            message = (SimpleVehicleMessage) BinaryFormatter.deserialize(input);
        } catch(UnrecognizedMessageTypeException e) {}

        assertEquals(message.getName(), messageName);
        assertEquals(message.getValue(), value);
    }

    @Test
    public void deserializeInvalidReturnsNull() throws IOException,
           UnrecognizedMessageTypeException {
        InputStream input = new ByteArrayInputStream(new byte[]{0,1,2,3,4});
        assertThat(BinaryFormatter.deserialize(input), nullValue());
    }

    @Test(expected=UnrecognizedMessageTypeException.class)
    public void deserializeWellFormedButConfusedMessage() throws IOException,
           UnrecognizedMessageTypeException {
        // Build a simple message that's missing a name
        BinaryMessages.VehicleMessage.Builder builder =
            BinaryMessages.VehicleMessage.newBuilder();
        builder.setType(BinaryMessages.VehicleMessage.Type.SIMPLE);

        BinaryMessages.SimpleMessage.Builder messageBuilder =
                BinaryMessages.SimpleMessage.newBuilder();
        BinaryMessages.DynamicField.Builder fieldBuilder =
                BinaryMessages.DynamicField.newBuilder();
        fieldBuilder.setType(BinaryMessages.DynamicField.Type.NUM);
        fieldBuilder.setNumericValue(42);

        messageBuilder.setValue(fieldBuilder);
        builder.setSimpleMessage(messageBuilder);

        BinaryMessages.VehicleMessage serialized = builder.build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        serialized.writeTo(output);
        InputStream input = new ByteArrayInputStream(output.toByteArray());

        BinaryFormatter.deserialize(input);
    }

    @Test(expected=SerializationException.class)
    public void serializeNamedMessageWithExtras() throws SerializationException {
        HashMap<String, Object> extras = new HashMap<>();
        extras.put("foo", "bar");
        extras.put("baz", 42.0);
        VehicleMessage message = new NamedVehicleMessage("foo");
        message.setExtras(extras);
        BinaryFormatter.serialize(message);
    }

    @Test(expected=SerializationException.class)
    public void serializeWithExtras() throws SerializationException {
        HashMap<String, Object> extras = new HashMap<>();
        extras.put("foo", "bar");
        extras.put("baz", 42.0);
        BinaryFormatter.serialize(new VehicleMessage(extras));
    }
}
