package com.openxc.measurements.serializers;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonSerializer implements MeasurementSerializer {
    private static final String TAG = "JsonSerializer";
    private static final String NAME_FIELD_NAME = "name";
    private static final String VALUE_FIELD_NAME = "value";
    private static final String EVENT_FIELD_NAME = "event";

    public static String serialize(String name, Object value, Object event) {
        JSONObject message = new JSONObject();
        try {
            message.put(NAME_FIELD_NAME, name);
            message.put(VALUE_FIELD_NAME, value);
            message.putOpt(EVENT_FIELD_NAME, event);
        } catch(JSONException e) {
            Log.w(TAG, "Unable to encode all data to JSON -- " +
                    "message may be incomplete", e);
        }
        return message.toString();
    }
}
