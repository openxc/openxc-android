package com.openxc.messages.formatters;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.util.Log;

import com.google.common.base.CharMatcher;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.CanMessage;
import com.openxc.messages.Command;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;

public class JsonFormatter {
    private static final String TAG = "JsonFormatter";
    private static Gson sGson = new Gson();

    public static String serialize(VehicleMessage message) {
        return sGson.toJson(message);
    }

    public static VehicleMessage deserialize(String data)
            throws UnrecognizedMessageTypeException {
        JsonObject root;
        try {
            JsonParser parser = new JsonParser();
            root = parser.parse(data).getAsJsonObject();
        } catch(JsonSyntaxException | IllegalStateException e) {
            throw new UnrecognizedMessageTypeException(
                    "Unable to parse JSON from \"" + data + "\": " + e);
        }

        Set<String> fields = new HashSet<>();
        for(Map.Entry<String, JsonElement> entry : root.entrySet()) {
            fields.add(entry.getKey());
        }

        Gson gson = new Gson();
        VehicleMessage message = new VehicleMessage();
        if(CanMessage.containsRequiredFields(fields)) {
            message = sGson.fromJson(root, CanMessage.class);
        } else if(DiagnosticResponse.containsRequiredFields(fields)) {
            message = sGson.fromJson(root, DiagnosticResponse.class);
        } else if(DiagnosticRequest.containsRequiredFields(fields)) {
            message = sGson.fromJson(root, DiagnosticRequest.class);
        } else if(Command.containsRequiredFields(fields)) {
            message = sGson.fromJson(root, Command.class);
        } else if(CommandResponse.containsRequiredFields(fields)) {
            message = sGson.fromJson(root, CommandResponse.class);
        } else if(EventedSimpleVehicleMessage.containsRequiredFields(fields)) {
            message = sGson.fromJson(root, EventedSimpleVehicleMessage.class);
        } else if(SimpleVehicleMessage.containsRequiredFields(fields)) {
            message = sGson.fromJson(root, SimpleVehicleMessage.class);
        } else if(NamedVehicleMessage.containsRequiredFields(fields)) {
            message = sGson.fromJson(root, NamedVehicleMessage.class);
        } else if(fields.contains(VehicleMessage.EXTRAS_KEY)) {
            message = sGson.fromJson(root, VehicleMessage.class);
        } else {
            throw new UnrecognizedMessageTypeException(
                    "Unrecognized combination of fields: " + fields);
        }
        return message;
    }
}
