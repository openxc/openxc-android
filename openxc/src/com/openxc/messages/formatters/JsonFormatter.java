package com.openxc.messages.formatters;

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
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;

public class JsonFormatter implements VehicleMessageFormatter {
    private static final String TAG = "JsonFormatter";
    public static final String NAME_FIELD = "name";
    public static final String VALUE_FIELD = "value";
    public static final String EVENT_FIELD = "event";
    public static final String TIMESTAMP_FIELD = "timestamp";
    private static final String TIMESTAMP_PATTERN = "##########.######";
    private static DecimalFormat sTimestampFormatter =
            (DecimalFormat) DecimalFormat.getInstance(Locale.US);

    static {
        sTimestampFormatter.applyPattern(TIMESTAMP_PATTERN);
    }

    // TODO need a version of this for each more specific type
    public byte[] serialize(VehicleMessage message) {
        StringWriter buffer = new StringWriter(64);
        // TODO migrate from Jackson to Gson
        JsonFactory jsonFactory = new JsonFactory();
        try {
            JsonGenerator gen = jsonFactory.createGenerator(buffer);

            gen.writeStartObject();
            // TODO
            // gen.writeStringField(NAME_FIELD, message.getName());
            // TODO for each of values in message.getvalues(), writeObjectField

            if(message.isTimestamped()) {
                gen.writeFieldName(TIMESTAMP_FIELD);
                // serialized measurements store the timestamp in UNIX time
                // (seconds with a fractional part since the UNIX epoch)
                gen.writeRawValue(sTimestampFormatter.format(
                            message.getTimestamp() / 1000.0));
            }

            gen.writeEndObject();
            gen.close();
        } catch(IOException e) {
            Log.w(TAG, "Unable to encode all data to JSON -- " +
                    "message may be incomplete", e);
        }
        return buffer.toString().getBytes();
    }

    public VehicleMessage deserialize(String data)
            throws UnrecognizedMessageTypeException {
        Gson gson = new Gson();
        LinkedTreeMap<String, Object> result =
                new LinkedTreeMap<String, Object>();
        try {
            result = (LinkedTreeMap<String, Object>) gson.fromJson(
                    data, result.getClass());
            // TODO this needs to happen but only for JSON?
            // measurement.mTimestamp =
                // (long) (parser.getNumberValue().doubleValue() * 1000);
            return VehicleMessage.buildSubtype(result);
        } catch(JsonSyntaxException e) {
            throw new UnrecognizedMessageTypeException(
                    "Unable to parse JSON from \"" + data + "\": " + e);
        }
    }

    public VehicleMessage deserialize(InputStream data)
            throws UnrecognizedMessageTypeException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(data));
        return deserialize(reader.toString());
    }

    /**
     * Return true if the buffer *most likely* contains JSON (as opposed to a
     * protobuf).
     */
    public boolean containsJson(String buffer) {
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
