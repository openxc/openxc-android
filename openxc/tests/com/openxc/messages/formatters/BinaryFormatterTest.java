package com.openxc.messages.formatters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.BinaryMessages;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.UnrecognizedMessageTypeException;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class BinaryFormatterTest {
    BinaryFormatter formatter = new BinaryFormatter();
    SimpleVehicleMessage message;
    String messageName = "foo";
    Double value = Double.valueOf(42);

    @Test
    public void deserializeNoErrors() throws IOException,
            UnrecognizedMessageTypeException {
        BinaryMessages.VehicleMessage.Builder builder =
            BinaryMessages.VehicleMessage.newBuilder();
        builder.setType(BinaryMessages.VehicleMessage.Type.TRANSLATED);

        BinaryMessages.TranslatedMessage.Builder messageBuilder =
                BinaryMessages.TranslatedMessage.newBuilder();
        messageBuilder.setName(messageName);
        BinaryMessages.DynamicField.Builder fieldBuilder =
                BinaryMessages.DynamicField.newBuilder();
        fieldBuilder.setType(BinaryMessages.DynamicField.Type.NUM);
        fieldBuilder.setNumericValue(42);

        messageBuilder.setValue(fieldBuilder);
        builder.setTranslatedMessage(messageBuilder);

        BinaryMessages.VehicleMessage serialized = builder.build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        serialized.writeTo(output);
        InputStream input = new ByteArrayInputStream(output.toByteArray());

        try {
            message = (SimpleVehicleMessage) formatter.deserialize(input);
        } catch(UnrecognizedMessageTypeException e) {}

        assertEquals(message.getName(), messageName);
        assertEquals(message.getValue(), value);
    }

    @Test
    public void deserializeInvalidReturnsNull() throws IOException,
           UnrecognizedMessageTypeException {
        InputStream input = new ByteArrayInputStream(new byte[]{0,1,2,3,4});
        assertThat(formatter.deserialize(input), nullValue());
    }

    @Test(expected=UnrecognizedMessageTypeException.class)
    public void deserializeWellFormedButConfusedMessage() throws IOException,
           UnrecognizedMessageTypeException {
        // Build a translated message that's missing a name
        BinaryMessages.VehicleMessage.Builder builder =
            BinaryMessages.VehicleMessage.newBuilder();
        builder.setType(BinaryMessages.VehicleMessage.Type.TRANSLATED);

        BinaryMessages.TranslatedMessage.Builder messageBuilder =
                BinaryMessages.TranslatedMessage.newBuilder();
        BinaryMessages.DynamicField.Builder fieldBuilder =
                BinaryMessages.DynamicField.newBuilder();
        fieldBuilder.setType(BinaryMessages.DynamicField.Type.NUM);
        fieldBuilder.setNumericValue(42);

        messageBuilder.setValue(fieldBuilder);
        builder.setTranslatedMessage(messageBuilder);

        BinaryMessages.VehicleMessage serialized = builder.build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        serialized.writeTo(output);
        InputStream input = new ByteArrayInputStream(output.toByteArray());

        formatter.deserialize(input);
    }
}
