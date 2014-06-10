package com.openxc.messages.formatters;

import java.io.IOException;
import java.io.InputStream;
import com.openxc.BinaryMessages;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.CanMessage;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;

public class BinaryFormatter implements VehicleMessageFormatter {
    public VehicleMessage deserialize(InputStream data) {
        try {
            BinaryMessages.VehicleMessage message =
                BinaryMessages.VehicleMessage.parseFrom(data);

            if(message != null && validateProtobuf(message)) {
                return buildVehicleMessage(message);
            }
        } catch(IOException e) {
            // TODO
        } catch(UnrecognizedMessageTypeException e) {
            // TODO
        }
        return null;
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
                NamedVehicleMessage message = new NamedVehicleMessage(name);
                message.put(SimpleVehicleMessage.VALUE_KEY, value);
                message.put("event", event);
                return message;
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

    private static boolean validateProtobuf(BinaryMessages.VehicleMessage message) {
        return (message.hasTranslatedMessage() &&
                        message.getTranslatedMessage().hasName() &&
                            message.getTranslatedMessage().hasValue())
            // TODO raw messages aren't supported upstream in the library at the
            // moment so we forcefully reject it here
                || (false && message.hasRawMessage() &&
                        message.getRawMessage().hasMessageId() &&
                        message.getRawMessage().hasData());
    }

}
