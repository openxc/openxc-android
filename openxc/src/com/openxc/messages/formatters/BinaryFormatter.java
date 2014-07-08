package com.openxc.messages.formatters;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.openxc.BinaryMessages;
import com.openxc.messages.CanMessage;
import com.openxc.messages.Command;
import com.openxc.messages.Command.CommandType;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SerializationException;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;

public class BinaryFormatter {
    private final static String TAG = "BinaryFormatter";
    // TODO split up into a serializer and deserializer class that are only used
    // internally in this class as this file is too large

    public static VehicleMessage deserialize(InputStream data)
            throws UnrecognizedMessageTypeException {
        VehicleMessage result = null;
        try {
            BinaryMessages.VehicleMessage message =
                BinaryMessages.VehicleMessage.parseFrom(data);

            if(message != null) {
                result = deserialize(message);
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

    private static NamedVehicleMessage deserializeNamedMessage(
            BinaryMessages.VehicleMessage binaryMessage) throws UnrecognizedMessageTypeException {
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
    }

    private static CanMessage deserializeCanMessage(
            BinaryMessages.VehicleMessage binaryMessage) {
        BinaryMessages.RawMessage canMessage = binaryMessage.getRawMessage();
        return new CanMessage(canMessage.getBus(),
                canMessage.getMessageId(),
                canMessage.getData().toByteArray());
    }

    private static Command deserializeCommand(
            BinaryMessages.VehicleMessage binaryMessage)
            throws UnrecognizedMessageTypeException {
        BinaryMessages.ControlCommand command =
                binaryMessage.getControlCommand();
        CommandType commandType = null;
        if(command.hasType()) {
            BinaryMessages.ControlCommand.Type serializedType = command.getType();
            if(serializedType.equals(BinaryMessages.ControlCommand.Type.VERSION)) {
                commandType = CommandType.VERSION;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.DEVICE_ID)) {
                commandType = CommandType.DEVICE_ID;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.DIAGNOSTIC)) {
                commandType = CommandType.DIAGNOSTIC_REQUEST;
            } else {
                throw new UnrecognizedMessageTypeException(
                        "Unrecognized command type in command: " +
                        command.getType());
            }
        } else {
            throw new UnrecognizedMessageTypeException(
                    "Command missing type");
        }

        // TODO diagnostic request as a part of diag request

        return new Command(commandType);
    }

    private static DiagnosticResponse deserializeDiagnosticResponse(
            BinaryMessages.VehicleMessage binaryMessage) {
        BinaryMessages.DiagnosticResponse serializedResponse =
                binaryMessage.getDiagnosticResponse();
        // TODO check if all required values are present
        byte[] payload = serializedResponse.getPayload().toByteArray();
        DiagnosticResponse response = new DiagnosticResponse(
                serializedResponse.getBus(),
                serializedResponse.getMessageId(),
                serializedResponse.getMode(),
                serializedResponse.getPid(),
                payload);

        if(serializedResponse.hasNegativeResponseCode()) {
            response.setNegativeResponseCode(
                    DiagnosticResponse.NegativeResponseCode.get(
                        serializedResponse.getNegativeResponseCode()));
        }

        if(serializedResponse.hasValue()) {
            response.setValue(serializedResponse.getValue());
        }
        return response;
    }

    private static CommandResponse deserializeCommandResponse(
            BinaryMessages.VehicleMessage binaryMessage)
            throws UnrecognizedMessageTypeException {
        BinaryMessages.CommandResponse response =
                binaryMessage.getCommandResponse();
        CommandType commandType = null;
        if(response.hasType()) {
            BinaryMessages.ControlCommand.Type serializedType = response.getType();
            if(serializedType.equals(BinaryMessages.ControlCommand.Type.VERSION)) {
                commandType = CommandType.VERSION;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.DEVICE_ID)) {
                commandType = CommandType.DEVICE_ID;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.DIAGNOSTIC)) {
                commandType = CommandType.DIAGNOSTIC_REQUEST;
            } else {
                throw new UnrecognizedMessageTypeException(
                        "Unrecognized command type in response: " +
                        response.getType());
            }
        } else {
            throw new UnrecognizedMessageTypeException(
                    "Command response missing type");
        }

        String message = null;
        if(response.hasMessage()) {
            message = response.getMessage();
        }
        return new CommandResponse(commandType, message);
    }

    private static VehicleMessage deserialize(
            BinaryMessages.VehicleMessage binaryMessage)
                throws UnrecognizedMessageTypeException {
        if(binaryMessage.hasTranslatedMessage()) {
            return deserializeNamedMessage(binaryMessage);
        } else if(binaryMessage.hasRawMessage()) {
            return deserializeCanMessage(binaryMessage);
        } else if(binaryMessage.hasCommandResponse()) {
            return deserializeCommandResponse(binaryMessage);
        } else if(binaryMessage.hasControlCommand()) {
            return deserializeCommand(binaryMessage);
        } else if(binaryMessage.hasDiagnosticResponse()) {
            return deserializeDiagnosticResponse(binaryMessage);
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

        if(message.hasValue()) {
            messageBuilder.setValue(message.getValue());
        }

        if(message.hasPayload()) {
            messageBuilder.setPayload(ByteString.copyFrom(message.getPayload()));
        }

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
        messageBuilder.setMultipleResponses(message.getMultipleResponses());
        messageBuilder.setFrequency(message.getFrequency());
        messageBuilder.setName(message.getName());

        if(message.hasPayload()) {
            messageBuilder.setPayload(ByteString.copyFrom(message.getPayload()));
        }
        // TODO hmm, not sure this exists
        // messageBuilder.setDecodedType(message.getDecodedType());
        // builder.setDiagnosticRequest(messageBuilder);
    }

    private static void buildCommand(BinaryMessages.VehicleMessage.Builder builder,
            Command message) throws SerializationException {
        builder.setType(BinaryMessages.VehicleMessage.Type.CONTROL_COMMAND);

        BinaryMessages.ControlCommand.Builder messageBuilder =
                BinaryMessages.ControlCommand.newBuilder();
        if(message.getCommand().equals(CommandType.VERSION)) {
            messageBuilder.setType(BinaryMessages.ControlCommand.Type.VERSION);
        } else if(message.getCommand().equals(CommandType.DEVICE_ID)) {
            messageBuilder.setType(BinaryMessages.ControlCommand.Type.DEVICE_ID);
        } else if(message.getCommand().equals(CommandType.DIAGNOSTIC_REQUEST)) {
            messageBuilder.setType(BinaryMessages.ControlCommand.Type.DIAGNOSTIC);
        } else {
            throw new SerializationException(
                    "Unrecognized command type in response: " +
                    message.getCommand());
        }

        builder.setControlCommand(messageBuilder);
    }

    private static void buildCommandResponse(BinaryMessages.VehicleMessage.Builder builder,
            CommandResponse message) throws SerializationException {
        builder.setType(BinaryMessages.VehicleMessage.Type.COMMAND_RESPONSE);

        BinaryMessages.CommandResponse.Builder messageBuilder =
                BinaryMessages.CommandResponse.newBuilder();
        if(message.getCommand().equals(CommandType.VERSION)) {
            messageBuilder.setType(BinaryMessages.ControlCommand.Type.VERSION);
        } else if(message.getCommand().equals(CommandType.DEVICE_ID)) {
            messageBuilder.setType(BinaryMessages.ControlCommand.Type.DEVICE_ID);
        } else if(message.getCommand().equals(CommandType.DIAGNOSTIC_REQUEST)) {
            messageBuilder.setType(BinaryMessages.ControlCommand.Type.DIAGNOSTIC);
        } else {
            throw new SerializationException(
                    "Unrecognized command type in response: " +
                    message.getCommand());
        }

        if(message.hasMessage()) {
            messageBuilder.setMessage(message.getMessage());
        }

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
