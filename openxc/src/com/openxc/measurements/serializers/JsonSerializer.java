package com.openxc.measurements.serializers;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Locale;

import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class JsonSerializer implements MeasurementSerializer {
    private static final String TAG = "JsonSerializer";
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

    public static String serialize(String name, Object value, Object event,
            long timestamp) {
        StringWriter buffer = new StringWriter(64);
        JsonFactory jsonFactory = new JsonFactory();
        try {
            JsonGenerator gen = jsonFactory.createGenerator(buffer);

            gen.writeStartObject();
            gen.writeStringField(NAME_FIELD, name);

            if(value != null) {
                gen.writeObjectField(VALUE_FIELD, value);
            }

            if(event != null) {
                gen.writeObjectField(EVENT_FIELD, event);
            }

            if(timestamp != 0) {
                gen.writeFieldName(TIMESTAMP_FIELD);
                // serialized measurements store the timestamp in UNIX time
                // (seconds with a fractional part since the UNIX epoch)
                gen.writeRawValue(sTimestampFormatter.format(timestamp / 1000.0));
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
