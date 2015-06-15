package com.openxc.messages.formatters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.math.BigDecimal;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.openxc.messages.CanMessage;
import com.openxc.messages.Command;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;

/**
 * A formatter for serializing and deserializing JSON OpenXC messages.
 */
public class JsonFormatter {
    private static Gson sGson = new Gson();

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory());
        builder.registerTypeAdapter(byte[].class, new ByteAdapter());
        builder.registerTypeAdapter(Double.class,  new JsonSerializer<Double>() {
            @Override
            public JsonElement serialize(final Double src, final Type typeOfSrc, final JsonSerializationContext context) {
                BigDecimal value = BigDecimal.valueOf(src);
                return new JsonPrimitive(value);
            }
        });
        sGson = builder.create();
    }

    /**
     * Serialize a vehicle message to a string.
     *
     * @param message the message to serialize
     * @return the message serialized to a String.
     */
    public static String serialize(VehicleMessage message) {
        return sGson.toJson(message);
    }

    /**
     * Serialize a collection of messages to a string.
     *
     * Each message will be serialized to a JSON object, and they will all be
     * included in a JSON array in the result.
     *
     * @param messages the messages to serialize.
     * @return the messages serialized individually and in a JSON array.
     */
    public static String serialize(Collection<VehicleMessage> messages) {
        return sGson.toJson(messages);
    }

    /**
     * Deserialize a single vehicle messages from the string.
     *
     * @param data a String containing the JSON serialized vehicle message.
     * @throws UnrecognizedMessageTypeException if no message could be
     *  deserialized.
     * @return the deserialized VehicleMessage.
     */
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

        VehicleMessage message;
        if(CanMessage.containsRequiredFields(fields)) {
            message = sGson.fromJson(root, CanMessage.class);
        } else if(DiagnosticResponse.containsRequiredFields(fields)) {
            message = sGson.fromJson(root, DiagnosticResponse.class);
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
