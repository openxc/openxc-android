package com.openxc.messages.formatters;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

import com.google.protobuf.ByteString;

import com.openxc.BinaryMessages;
import com.openxc.messages.CanMessage;
import com.openxc.messages.Command;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.SerializationException;
import com.openxc.messages.VehicleMessage;

public class BinaryFormatter {
    private final static String TAG = "BinaryFormatter";

    public static VehicleMessage deserialize(InputStream data)
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

    public static byte[] serialize(VehicleMessage message)
            throws SerializationException {
        if(message.hasExtras()) {
            throw new SerializationException("Messages with extras cannot be " +
                    "serialized to the binary format - use JSON instead");
        }

        BinaryMessages.VehicleMessage.Builder builder =
            BinaryMessages.VehicleMessage.newBuilder();
        if(message instanceof CanMessage) {
            buildCanMessage(builder, (CanMessage) message);
        } else if(message instanceof DiagnosticResponse) {
            buildDiagnosticResponse(builder, (DiagnosticResponse) message);
        } else if(message instanceof DiagnosticRequest) {
            buildDiagnosticRequest(builder, (DiagnosticRequest) message);
        } else if(message instanceof Command) {
            buildCommand(builder, (Command) message);
        } else if(message instanceof CommandResponse) {
            buildCommandResponse(builder, (CommandResponse) message);
        } else if(message instanceof EventedSimpleVehicleMessage) {
            buildEventedSimpleVehicleMessage(builder,
                    (EventedSimpleVehicleMessage) message);
        } else if(message instanceof SimpleVehicleMessage) {
            buildSimpleVehicleMessage(builder, (SimpleVehicleMessage) message);
        } else if(message instanceof NamedVehicleMessage) {
            buildNamedVehicleMessage(builder, (NamedVehicleMessage) message);
        } else {
            buildGenericVehicleMessage(builder, message);
        }
        return builder.build().toByteArray();
    }

    private static VehicleMessage buildVehicleMessage(
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
            BinaryMessages.DynamicField field =
                    translatedMessage.getValue();
            if(field.hasNumericValue()) {
                value = field.getNumericValue();
            } else if(field.hasBooleanValue()) {
                value = field.getBooleanValue();
            } else if(field.hasStringValue()) {
                value = field.getStringValue();
            }

            Object event = null;
            if(translatedMessage.hasEvent()) {
                field = translatedMessage.getEvent();
                if(field.hasNumericValue()) {
                    event = field.getNumericValue();
                } else if(field.hasBooleanValue()) {
                    event = field.getBooleanValue();
                } else if(field.hasStringValue()) {
                    event = field.getStringValue();
                }
            }

            if(event == null) {
                if(value == null) {
                    return new NamedVehicleMessage(name);
                } else {
                    return new SimpleVehicleMessage(name, value);
                }
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

    private static void buildCanMessage(BinaryMessages.VehicleMessage.Builder builder,
            CanMessage message) {
        // TODO I'd like to change the "raw" language to explicitly be "can
        // message"
        builder.setType(BinaryMessages.VehicleMessage.Type.RAW);

        BinaryMessages.RawMessage.Builder messageBuilder =
                BinaryMessages.RawMessage.newBuilder();
        messageBuilder.setBus(message.getBus());
        messageBuilder.setMessageId(message.getId());
        messageBuilder.setData(ByteString.copyFrom(message.getData()));
        builder.setRawMessage(messageBuilder);
    }

    private static void buildDiagnosticResponse(BinaryMessages.VehicleMessage.Builder builder,
            DiagnosticResponse message) {
        builder.setType(BinaryMessages.VehicleMessage.Type.DIAGNOSTIC);

        BinaryMessages.DiagnosticResponse.Builder messageBuilder =
                BinaryMessages.DiagnosticResponse.newBuilder();
        // TODO need to standardize - getBus or getBusId
        messageBuilder.setBus(message.getBusId());
        messageBuilder.setMessageId(message.getId());
        messageBuilder.setMode(message.getMode());
        messageBuilder.setPid(message.getPid());
        messageBuilder.setNegativeResponseCode(message.getNegativeResponseCode().code());
        messageBuilder.setSuccess(message.isSuccessful());
        messageBuilder.setValue(message.getValue());
        messageBuilder.setPayload(ByteString.copyFrom(message.getPayload()));
        builder.setDiagnosticResponse(messageBuilder);
    }

    private static void buildDiagnosticRequest(BinaryMessages.VehicleMessage.Builder builder,
            DiagnosticRequest message) {
        builder.setType(BinaryMessages.VehicleMessage.Type.DIAGNOSTIC);

        BinaryMessages.DiagnosticRequest.Builder messageBuilder =
                BinaryMessages.DiagnosticRequest.newBuilder();
        messageBuilder.setBus(message.getBusId());
        messageBuilder.setMessageId(message.getId());
        messageBuilder.setMode(message.getMode());
        messageBuilder.setPid(message.getPid());
        messageBuilder.setPayload(ByteString.copyFrom(message.getPayload()));
        messageBuilder.setMultipleResponses(message.getMultipleResponses());
        messageBuilder.setFrequency(message.getFrequency());
        messageBuilder.setName(message.getName());
        // TODO hmm, not sure this exists
        // messageBuilder.setDecodedType(message.getDecodedType());
        // TODO need to figure out how this is serialiezd, to a control command
        // or not
        // builder.setDiagnosticRequest(messageBuilder);
    }

    private static void buildCommand(BinaryMessages.VehicleMessage.Builder builder,
            Command command) {
        builder.setType(BinaryMessages.VehicleMessage.Type.CONTROL_COMMAND);

        BinaryMessages.ControlCommand.Builder messageBuilder =
                BinaryMessages.ControlCommand.newBuilder();
        // TODO
        builder.setControlCommand(messageBuilder);
    }

    private static void buildCommandResponse(BinaryMessages.VehicleMessage.Builder builder,
            CommandResponse message) {
        builder.setType(BinaryMessages.VehicleMessage.Type.COMMAND_RESPONSE);

        BinaryMessages.CommandResponse.Builder messageBuilder =
                BinaryMessages.CommandResponse.newBuilder();
        // TODO
        builder.setCommandResponse(messageBuilder);
    }

    private static BinaryMessages.DynamicField.Builder buildDynamicField(
            Object value) {
        BinaryMessages.DynamicField.Builder fieldBuilder =
                BinaryMessages.DynamicField.newBuilder();
        if(value instanceof String) {
            fieldBuilder.setType(BinaryMessages.DynamicField.Type.STRING);
            fieldBuilder.setStringValue((String)value);
        } else if(value instanceof Number) {
            fieldBuilder.setType(BinaryMessages.DynamicField.Type.NUM);
            fieldBuilder.setNumericValue(((Number)value).doubleValue());
        } else if(value instanceof Boolean) {
            fieldBuilder.setType(BinaryMessages.DynamicField.Type.BOOL);
            fieldBuilder.setBooleanValue((Boolean) value);
        } else {
            // TODO
        }
        return fieldBuilder;
    }

    private static BinaryMessages.TranslatedMessage.Builder startBuildingTranslated(
            BinaryMessages.VehicleMessage.Builder builder,
            NamedVehicleMessage message) {
        // TODO I'd like to get rid of the 'translated' language and switch to
        // Simple and EventedSimple
        builder.setType(BinaryMessages.VehicleMessage.Type.TRANSLATED);

        BinaryMessages.TranslatedMessage.Builder messageBuilder =
                BinaryMessages.TranslatedMessage.newBuilder();
        messageBuilder.setName(message.getName());
        return messageBuilder;
    }

    private static void buildEventedSimpleVehicleMessage(BinaryMessages.VehicleMessage.Builder builder,
            EventedSimpleVehicleMessage message) {
        BinaryMessages.TranslatedMessage.Builder messageBuilder =
                startBuildingTranslated(builder, message);
        messageBuilder.setValue(buildDynamicField(message.getValue()));
        messageBuilder.setEvent(buildDynamicField(message.getEvent()));
        builder.setTranslatedMessage(messageBuilder);
    }

    private static void buildSimpleVehicleMessage(BinaryMessages.VehicleMessage.Builder builder,
            SimpleVehicleMessage message) {
        BinaryMessages.TranslatedMessage.Builder messageBuilder =
                startBuildingTranslated(builder, message);
        messageBuilder.setValue(buildDynamicField(message.getValue()));
        builder.setTranslatedMessage(messageBuilder);
    }

    private static void buildNamedVehicleMessage(BinaryMessages.VehicleMessage.Builder builder,
            NamedVehicleMessage message) {
        BinaryMessages.TranslatedMessage.Builder messageBuilder =
                startBuildingTranslated(builder, message);
        builder.setTranslatedMessage(messageBuilder);
    }

    private static void buildGenericVehicleMessage(BinaryMessages.VehicleMessage.Builder builder,
            VehicleMessage message) throws SerializationException {
        // TODO actually the binary format doens't support arbitrary extra
        // fields right now - could support with protobuf extensions but that is
        // not something I want to do right now
        if(!message.hasExtras()) {
            throw new SerializationException(
                    "Can't serialize empty VehicleMessage: " + message);
        }
    }
}
