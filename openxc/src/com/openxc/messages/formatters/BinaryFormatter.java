package com.openxc.messages.formatters;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

import com.openxc.BinaryMessages;
import com.openxc.messages.CanMessage;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;

public class BinaryFormatter {
    private final static String TAG = "BinaryFormatter";

    public VehicleMessage deserialize(InputStream data)
            throws UnrecognizedMessageTypeException {
        VehicleMessage result = null;
        try {
            BinaryMessages.VehicleMessage message =
                BinaryMessages.VehicleMessage.parseFrom(data);

            if(message != null) {
                result = buildVehicleMessage(message);
            }
        } catch(IOException e) {
            Log.w(TAG, "Unable to deserialize from binary stream", e);
        }
        return result;
    }

    private VehicleMessage buildVehicleMessage(
            BinaryMessages.VehicleMessage binaryMessage)
                throws UnrecognizedMessageTypeException{
        if(binaryMessage.hasTranslatedMessage()) {
            BinaryMessages.TranslatedMessage translatedMessage =
                    binaryMessage.getTranslatedMessage();
            String name;
            if(translatedMessage.hasName()) {
                name = translatedMessage.getName();
            } else {
                throw new UnrecognizedMessageTypeException(
                        "Binary message is missing name");
            }

            Object value = null;
            if(translatedMessage.hasValue()) {
                BinaryMessages.DynamicField field =
                        translatedMessage.getValue();
                if(field.hasNumericValue()) {
                    value = field.getNumericValue();
                } else if(field.hasBooleanValue()) {
                    value = field.getBooleanValue();
                } else if(field.hasStringValue()) {
                    value = field.getStringValue();
                } else {
                    throw new UnrecognizedMessageTypeException(
                        "Binary message value had no...value");
                }
            } else {
                throw new UnrecognizedMessageTypeException(
                        "Binary message had no value");
            }

            Object event = null;
            if(translatedMessage.hasEvent()) {
                BinaryMessages.DynamicField field =
                        translatedMessage.getEvent();
                if(field.hasNumericValue()) {
                    event = field.getNumericValue();
                } else if(field.hasBooleanValue()) {
                    event = field.getBooleanValue();
                } else if(field.hasStringValue()) {
                    event = field.getStringValue();
                }
            }

            if(event == null) {
                return new SimpleVehicleMessage(name, value);
            } else {
               return new EventedSimpleVehicleMessage(name, value, event);
            }
        } else if(binaryMessage.hasRawMessage()) {
            BinaryMessages.RawMessage canMessage = binaryMessage.getRawMessage();
            return new CanMessage(canMessage.getBus(),
                    canMessage.getMessageId(),
                    canMessage.getData().toByteArray());

        } else {
            throw new UnrecognizedMessageTypeException(
                    "Binary message type not recognized");
        }
    }

    public byte[] serialize(VehicleMessage message) {
        // TODO
        return null;
    }
}
