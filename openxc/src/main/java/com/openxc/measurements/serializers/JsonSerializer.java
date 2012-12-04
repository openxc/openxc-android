package com.openxc.measurements.serializers;

import java.io.IOException;
import java.io.StringWriter;

import java.text.DecimalFormat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import android.util.Log;

public class JsonSerializer implements MeasurementSerializer {
    private static final String TAG = "JsonSerializer";
    public static final String NAME_FIELD = "name";
    public static final String VALUE_FIELD = "value";
    public static final String EVENT_FIELD = "event";
    public static final String TIMESTAMP_FIELD = "timestamp";
    private static DecimalFormat sTimestampFormatter =
            new DecimalFormat("##########.000000");

    public static String serialize(String name, Object value, Object event,
            Double timestamp) {
        StringWriter buffer = new StringWriter(64);
        JsonFactory jsonFactory = new JsonFactory();
        try {
            JsonGenerator gen = jsonFactory.createJsonGenerator(buffer);

            gen.writeStartObject();
            gen.writeStringField(NAME_FIELD, name);

            if(value != null) {
                gen.writeObjectField(VALUE_FIELD, value);
            }

            if(event != null) {
                gen.writeObjectField(EVENT_FIELD, event);
            }

            if(timestamp != null) {
                gen.writeFieldName(TIMESTAMP_FIELD);
                gen.writeRawValue(sTimestampFormatter.format(timestamp));
            }

            gen.writeEndObject();
            gen.close();
        } catch(IOException e) {
            Log.w(TAG, "Unable to encode all data to JSON -- " +
                    "message may be incomplete", e);
        }
        return buffer.toString();
    }
}
