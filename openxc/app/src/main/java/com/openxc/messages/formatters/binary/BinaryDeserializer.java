package com.openxc.messages.formatters.binary;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

import com.openxc.BinaryMessages;
import com.openxc.messages.CanMessage;
import com.openxc.messages.Command;
import com.openxc.messages.Command.CommandType;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;

public class BinaryDeserializer {
    private final static String TAG = "BinaryDeserializer";

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

    private static NamedVehicleMessage deserializeNamedMessage(
            BinaryMessages.VehicleMessage binaryMessage) throws UnrecognizedMessageTypeException {
        BinaryMessages.SimpleMessage simpleMessage =
            binaryMessage.getSimpleMessage();
        String name;
        if(simpleMessage.hasName()) {
            name = simpleMessage.getName();
        } else {
            throw new UnrecognizedMessageTypeException(
                    "Binary message is missing name");
        }

        Object value = null;
        BinaryMessages.DynamicField field =
            simpleMessage.getValue();
        if(field.hasNumericValue()) {
            value = field.getNumericValue();
        } else if(field.hasBooleanValue()) {
            value = field.getBooleanValue();
        } else if(field.hasStringValue()) {
            value = field.getStringValue();
        }

        Object event = null;
        if(simpleMessage.hasEvent()) {
            field = simpleMessage.getEvent();
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
        BinaryMessages.CanMessage canMessage = binaryMessage.getCanMessage();
        return new CanMessage(canMessage.getBus(),
                canMessage.getId(),
                canMessage.getData().toByteArray());
    }

    private static Command deserializeDiagnosticCommand(
            BinaryMessages.ControlCommand command)
            throws UnrecognizedMessageTypeException {
        if(!command.hasDiagnosticRequest()) {
            throw new UnrecognizedMessageTypeException(
                    "Diagnostic command missing request details");
        }

        BinaryMessages.DiagnosticControlCommand diagnosticCommand =
                command.getDiagnosticRequest();
        String action = null;
        if(!diagnosticCommand.hasAction()) {
            throw new UnrecognizedMessageTypeException(
                    "Diagnostic command missing action");
        } else if(diagnosticCommand.getAction() ==
                BinaryMessages.DiagnosticControlCommand.Action.ADD) {
            action = DiagnosticRequest.ADD_ACTION_KEY;
        } else if(diagnosticCommand.getAction() ==
                BinaryMessages.DiagnosticControlCommand.Action.CANCEL) {
            action = DiagnosticRequest.CANCEL_ACTION_KEY;
        } else {
            throw new UnrecognizedMessageTypeException(
                    "Unrecognized action: " + diagnosticCommand.getAction());
        }

        BinaryMessages.DiagnosticRequest serializedRequest =
                diagnosticCommand.getRequest();
        DiagnosticRequest request = new DiagnosticRequest(
                serializedRequest.getBus(),
                serializedRequest.getMessageId(),
                serializedRequest.getMode());

        if(serializedRequest.hasPayload()) {
            request.setPayload(
                    serializedRequest.getPayload().toByteArray());
        }

        if(serializedRequest.hasPid()) {
            request.setPid(serializedRequest.getPid());
        }

        if(serializedRequest.hasMultipleResponses()) {
            request.setMultipleResponses(
                    serializedRequest.getMultipleResponses());
        }

        if(serializedRequest.hasFrequency()) {
            request.setFrequency(serializedRequest.getFrequency());
        }

        if(serializedRequest.hasName()) {
            request.setName(serializedRequest.getName());
        }
        return new Command(request, action);
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

        Command deserializedCommand = null;
        if(commandType.equals(CommandType.DIAGNOSTIC_REQUEST)) {
            deserializedCommand = deserializeDiagnosticCommand(command);
        } else {
            deserializedCommand = new Command(commandType);
        }
        return deserializedCommand;
    }

    private static DiagnosticResponse deserializeDiagnosticResponse(
            BinaryMessages.VehicleMessage binaryMessage)
            throws UnrecognizedMessageTypeException {
        BinaryMessages.DiagnosticResponse serializedResponse =
                binaryMessage.getDiagnosticResponse();
        if(!serializedResponse.hasBus() || !serializedResponse.hasMessageId() ||
                !serializedResponse.hasMode()) {
            throw new UnrecognizedMessageTypeException(
                    "Diagnostic response missing one or more required fields");
        }

        DiagnosticResponse response = new DiagnosticResponse(
                serializedResponse.getBus(),
                serializedResponse.getMessageId(),
                serializedResponse.getMode());

        if(serializedResponse.hasPid()) {
            response.setPid(serializedResponse.getPid());
        }

        if(serializedResponse.hasPayload()) {
            response.setPayload(serializedResponse.getPayload().toByteArray());
        }

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

        if(!response.hasStatus()) {
            throw new UnrecognizedMessageTypeException(
                    "Command response missing status");
        }

        String message = null;
        if(response.hasMessage()) {
            message = response.getMessage();
        }
        return new CommandResponse(commandType, response.getStatus(), message);
    }

    private static VehicleMessage deserialize(
            BinaryMessages.VehicleMessage binaryMessage)
                throws UnrecognizedMessageTypeException {
        if(binaryMessage.hasSimpleMessage()) {
            return deserializeNamedMessage(binaryMessage);
        } else if(binaryMessage.hasCanMessage()) {
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
}
