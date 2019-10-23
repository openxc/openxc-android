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
        name = simpleMessage.getName();
        Object value = null;
        BinaryMessages.DynamicField field =
            simpleMessage.getValue();
        if(field.getTypeValue() == 2)  {
            value = field.getNumericValue();
        } else if(field.getTypeValue() == 3) {
            value = field.getBooleanValue();
        } else if(field.getTypeValue() == 1) {
            value = field.getStringValue();
        }

        Object event = null;
        if(simpleMessage.hasEvent()) {
            field = simpleMessage.getEvent();
            if(field.getTypeValue() == 2) {
                event = field.getNumericValue();
            } else if(field.getTypeValue() == 3) {
                event = field.getBooleanValue();
            } else if(field.getTypeValue() == 1) {
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
        String action;
        if(diagnosticCommand.getActionValue() == 0) {
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

        request.setPayload(serializedRequest.getPayload().toByteArray());
        request.setPid(serializedRequest.getPid());
        request.setMultipleResponses(serializedRequest.getMultipleResponses());
        request.setFrequency(serializedRequest.getFrequency());
        request.setName(serializedRequest.getName());

        return new Command(request, action);
    }

    private static Command deserializeCommand(
            BinaryMessages.VehicleMessage binaryMessage)
            throws UnrecognizedMessageTypeException {
        BinaryMessages.ControlCommand command =
                binaryMessage.getControlCommand();
        CommandType commandType;
        if(command.getTypeValue() != 0) {
            BinaryMessages.ControlCommand.Type serializedType = command.getType();
            if(serializedType.equals(BinaryMessages.ControlCommand.Type.VERSION)) {
                commandType = CommandType.VERSION;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.DEVICE_ID)) {
                commandType = CommandType.DEVICE_ID;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.PLATFORM)) {
                commandType = CommandType.PLATFORM;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.PASSTHROUGH)) {
                commandType = CommandType.PASSTHROUGH;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.ACCEPTANCE_FILTER_BYPASS)) {
                commandType = CommandType.AF_BYPASS;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.PAYLOAD_FORMAT)) {
                commandType = CommandType.PAYLOAD_FORMAT;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.SD_MOUNT_STATUS)) {
                commandType = CommandType.SD_MOUNT_STATUS;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.RTC_CONFIGURATION)) {
                commandType = CommandType.RTC_CONFIGURATION;
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

        Command deserializedCommand;
        if(commandType.equals(CommandType.DIAGNOSTIC_REQUEST)) {
            deserializedCommand = deserializeDiagnosticCommand(command);
        } else {
            deserializedCommand = new Command(commandType);
        }
        return deserializedCommand;
    }

    private static DiagnosticResponse deserializeDiagnosticResponse(
            BinaryMessages.VehicleMessage binaryMessage) {
        BinaryMessages.DiagnosticResponse serializedResponse =
                binaryMessage.getDiagnosticResponse();

        DiagnosticResponse response = new DiagnosticResponse(
                serializedResponse.getBus(),
                serializedResponse.getMessageId(),
                serializedResponse.getMode());

        response.setPid(serializedResponse.getPid());
        response.setPayload(serializedResponse.getPayload().toByteArray());
        response.setNegativeResponseCode(
                DiagnosticResponse.NegativeResponseCode.get(
                serializedResponse.getNegativeResponseCode()));

        if(serializedResponse.getValue().getTypeValue() == 2) {
            response.setValue(serializedResponse.getValue().getNumericValue());
        }
        return response;
    }

    private static CommandResponse deserializeCommandResponse(
            BinaryMessages.VehicleMessage binaryMessage)
            throws UnrecognizedMessageTypeException {
        BinaryMessages.CommandResponse response =
                binaryMessage.getCommandResponse();
        CommandType commandType;
        if(response.getTypeValue() != 0) {
            BinaryMessages.ControlCommand.Type serializedType = response.getType();
            if(serializedType.equals(BinaryMessages.ControlCommand.Type.VERSION)) {
                commandType = CommandType.VERSION;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.DEVICE_ID)) {
                commandType = CommandType.DEVICE_ID;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.PLATFORM)){
                commandType = CommandType.PLATFORM;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.PASSTHROUGH)){
                commandType = CommandType.PASSTHROUGH;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.ACCEPTANCE_FILTER_BYPASS)){
                commandType = CommandType.AF_BYPASS;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.PAYLOAD_FORMAT)){
                commandType = CommandType.PAYLOAD_FORMAT;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.SD_MOUNT_STATUS)){
                commandType = CommandType.SD_MOUNT_STATUS;
            } else if(serializedType.equals(BinaryMessages.ControlCommand.Type.RTC_CONFIGURATION)){
                commandType = CommandType.RTC_CONFIGURATION;
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

        String message = response.getMessage();
        return new CommandResponse(commandType, response.getStatus(), message);
    }

    private static VehicleMessage deserialize(
            BinaryMessages.VehicleMessage binaryMessage)
                throws UnrecognizedMessageTypeException {
        if(binaryMessage.getTypeValue() == 2) {
            return deserializeNamedMessage(binaryMessage);
        } else if(binaryMessage.getTypeValue() == 1) {
            return deserializeCanMessage(binaryMessage);
        } else if(binaryMessage.getTypeValue() == 5) {
            return deserializeCommandResponse(binaryMessage);
        } else if(binaryMessage.getTypeValue() == 4) {
            return deserializeCommand(binaryMessage);
        } else if(binaryMessage.getTypeValue() == 3) {
            return deserializeDiagnosticResponse(binaryMessage);
        } else {
            throw new UnrecognizedMessageTypeException(
                    "Binary message type not recognized");
        }
    }
}
