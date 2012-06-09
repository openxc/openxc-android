package com.openxc.measurements.serializers;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonSerializer implements MeasurementSerializer {
    private static final String TAG = "JsonSerializer";
    public static final String NAME_FIELD = "name";
    public static final String VALUE_FIELD = "value";
    public static final String EVENT_FIELD = "event";

    public static String serialize(String name, Object value, Object event) {
        return preSerialize(name, value, event).toString();
    }

    public static JSONObject preSerialize(String name, Object value, Object event) {
        JSONObject message = new JSONObject();
        try {
            message.put(NAME_FIELD, name);
            message.put(VALUE_FIELD, value);
            message.putOpt(EVENT_FIELD, event);
        } catch(JSONException e) {
            Log.w(TAG, "Unable to encode all data to JSON -- " +
                    "message may be incomplete", e);
        }
        return message;
    }
}
