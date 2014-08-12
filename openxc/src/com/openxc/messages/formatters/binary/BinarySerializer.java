package com.openxc.messages.formatters.binary;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
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
import com.openxc.messages.VehicleMessage;

public class BinarySerializer {
    public static MessageLite preSerialize(VehicleMessage message)
            throws SerializationException {
        if(message.hasExtras()) {
            throw new SerializationException("Messages with extras cannot be " +
                    "serialized to the binary format - use JSON instead");
        }

        BinaryMessages.VehicleMessage.Builder builder =
            BinaryMessages.VehicleMessage.newBuilder();
        if(message instanceof CanMessage) {
            serializeCanMessage(builder, (CanMessage) message);
        } else if(message instanceof DiagnosticResponse) {
            serializeDiagnosticResponse(builder, (DiagnosticResponse) message);
        } else if(message instanceof Command) {
            serializeCommand(builder, (Command) message);
        } else if(message instanceof CommandResponse) {
            serializeCommandResponse(builder, (CommandResponse) message);
        } else if(message instanceof EventedSimpleVehicleMessage) {
            serializeEventedSimpleVehicleMessage(builder,
                    (EventedSimpleVehicleMessage) message);
        } else if(message instanceof SimpleVehicleMessage) {
            serializeSimpleVehicleMessage(builder, (SimpleVehicleMessage) message);
        } else if(message instanceof NamedVehicleMessage) {
            serializeNamedVehicleMessage(builder, (NamedVehicleMessage) message);
        } else {
            serializeGenericVehicleMessage(builder, message);
        }
        return builder.build();
    }

    private static void serializeCanMessage(BinaryMessages.VehicleMessage.Builder builder,
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

    private static void serializeDiagnosticResponse(BinaryMessages.VehicleMessage.Builder builder,
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

    private static BinaryMessages.DiagnosticRequest.Builder
            startSerializingDiagnosticRequest(DiagnosticRequest message) {
        BinaryMessages.DiagnosticRequest.Builder messageBuilder =
                BinaryMessages.DiagnosticRequest.newBuilder();
        messageBuilder.setBus(message.getBusId());
        messageBuilder.setMessageId(message.getId());
        messageBuilder.setMode(message.getMode());
        messageBuilder.setMultipleResponses(message.getMultipleResponses());

        if(message.hasPid()) {
            messageBuilder.setPid(message.getPid());
        }

        if(message.hasFrequency()) {
            messageBuilder.setFrequency(message.getFrequency());
        }

        if(message.hasName()) {
            messageBuilder.setName(message.getName());
        }

        if(message.hasPayload()) {
            messageBuilder.setPayload(ByteString.copyFrom(message.getPayload()));
        }
        // TODO hmm, not sure this exists
        // messageBuilder.setDecodedType(message.getDecodedType());
        return messageBuilder;
    }

    private static void serializeCommand(BinaryMessages.VehicleMessage.Builder builder,
            Command message) throws SerializationException {
        builder.setType(BinaryMessages.VehicleMessage.Type.CONTROL_COMMAND);

        BinaryMessages.ControlCommand.Builder messageBuilder =
                BinaryMessages.ControlCommand.newBuilder();
        CommandType commandType = message.getCommand();
        if(commandType.equals(CommandType.VERSION)) {
            messageBuilder.setType(BinaryMessages.ControlCommand.Type.VERSION);
        } else if(commandType.equals(CommandType.DEVICE_ID)) {
            messageBuilder.setType(BinaryMessages.ControlCommand.Type.DEVICE_ID);
        } else if(commandType.equals(CommandType.DIAGNOSTIC_REQUEST)) {
            messageBuilder.setType(BinaryMessages.ControlCommand.Type.DIAGNOSTIC);
        } else {
            throw new SerializationException(
                    "Unrecognized command type in response: " + commandType);
        }

        if(commandType.equals(CommandType.DIAGNOSTIC_REQUEST)) {
            messageBuilder.setDiagnosticRequest(
                    startSerializingDiagnosticRequest(
                        message.getDiagnosticRequest()));
        }

        if(message.hasAction()) {
            if(message.getAction().equals(DiagnosticRequest.ADD_ACTION_KEY)) {
                messageBuilder.setAction(
                        BinaryMessages.ControlCommand.Action.ADD);
            } else if(message.getAction().equals(DiagnosticRequest.CANCEL_ACTION_KEY)) {
                messageBuilder.setAction(
                        BinaryMessages.ControlCommand.Action.CANCEL);
            }
        }

        builder.setControlCommand(messageBuilder);
    }

    private static void serializeCommandResponse(BinaryMessages.VehicleMessage.Builder builder,
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

        messageBuilder.setStatus(message.getStatus());

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

    private static void serializeEventedSimpleVehicleMessage(BinaryMessages.VehicleMessage.Builder builder,
            EventedSimpleVehicleMessage message) {
        BinaryMessages.TranslatedMessage.Builder messageBuilder =
                startBuildingTranslated(builder, message);
        messageBuilder.setValue(buildDynamicField(message.getValue()));
        messageBuilder.setEvent(buildDynamicField(message.getEvent()));
        builder.setTranslatedMessage(messageBuilder);
    }

    private static void serializeSimpleVehicleMessage(BinaryMessages.VehicleMessage.Builder builder,
            SimpleVehicleMessage message) {
        BinaryMessages.TranslatedMessage.Builder messageBuilder =
                startBuildingTranslated(builder, message);
        messageBuilder.setValue(buildDynamicField(message.getValue()));
        builder.setTranslatedMessage(messageBuilder);
    }

    private static void serializeNamedVehicleMessage(BinaryMessages.VehicleMessage.Builder builder,
            NamedVehicleMessage message) {
        BinaryMessages.TranslatedMessage.Builder messageBuilder =
                startBuildingTranslated(builder, message);
        builder.setTranslatedMessage(messageBuilder);
    }

    private static void serializeGenericVehicleMessage(BinaryMessages.VehicleMessage.Builder builder,
            VehicleMessage message) throws SerializationException {
        // The binary format doesn't support arbitrary extra fields right now -
        // could support with protobuf extensions but that is not something I
        // want to do right now
        throw new SerializationException(
                "Can't serialize generic VehicleMessage to binary: " + message);
    }
}
