package com.openxc.messages.formatters;

import java.util.Map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Locale;

import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.CharMatcher;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.LinkedTreeMap;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;

public class JsonFormatter {
    private static final String TAG = "JsonFormatter";
    private static final String TIMESTAMP_PATTERN = "##########.######";
    private static DecimalFormat sTimestampFormatter =
            (DecimalFormat) DecimalFormat.getInstance(Locale.US);

    static {
        sTimestampFormatter.applyPattern(TIMESTAMP_PATTERN);
    }

    public static String serialize(VehicleMessage message) {
        Gson gson = new Gson();
        JsonObject result = gson.toJsonTree(message).getAsJsonObject();
        if(message.isTimestamped()) {
            result.add(VehicleMessage.TIMESTAMP_KEY,
                    new JsonPrimitive(message.getTimestamp() / 1000.0));
        }

        for(Map.Entry<String, Object> entry : message.getValuesMap().entrySet()) {
            result.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
        }
        return gson.toJson(result);
    }

    public static VehicleMessage deserialize(String data)
            throws UnrecognizedMessageTypeException {
        Gson gson = new Gson();
        LinkedTreeMap<String, Object> result =
                new LinkedTreeMap<String, Object>();
        try {
            result = (LinkedTreeMap<String, Object>) gson.fromJson(
                    data, result.getClass());
            return VehicleMessage.buildSubtype(result);
        } catch(JsonSyntaxException e) {
            throw new UnrecognizedMessageTypeException(
                    "Unable to parse JSON from \"" + data + "\": " + e);
        }
    }

    public static VehicleMessage deserialize(InputStream data)
            throws UnrecognizedMessageTypeException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(data));
        return deserialize(reader.toString());
    }

    /**
     * Return true if the buffer *most likely* contains JSON (as opposed to a
     * protobuf).
     */
    public static boolean containsJson(String buffer) {
        return CharMatcher.ASCII
            // We need to allow the \u0000 delimiter for JSON messages, so we
            // can't use the JAVA_ISO_CONTROL character set and must build the
            // range manually (minus \u0000)
            .and(CharMatcher.inRange('\u0001', '\u001f').negate())
            .and(CharMatcher.inRange('\u007f', '\u009f').negate())
            .and(CharMatcher.ASCII)
            .matchesAllOf(buffer.toString());
    }

}
