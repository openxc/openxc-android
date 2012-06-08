package com.openxc.sources;

import org.json.JSONException;
import org.json.JSONObject;

import com.openxc.sources.SourceCallback;

import android.content.Context;

import android.util.Log;

/**
 * A vehicle data source expecting JSON messages as raw input.
 *
 * Whether coming from a trace file or over a network, this class can parse a
 * JSON string and pass its measurement values to the handleMessage method of
 * the data source.
 *
 * The class is abstract as it does not describe the source of the data, only
 * the expected format.
 */
public abstract class JsonVehicleDataSource
        extends ContextualVehicleDataSource {
    private static final String TAG = "JsonVehicleDataSource";

    private static final String NAME_FIELD_NAME = "name";
    private static final String VALUE_FIELD_NAME = "value";
    private static final String EVENT_FIELD_NAME = "event";

    public JsonVehicleDataSource(Context context) {
        super(context);
    }

    public JsonVehicleDataSource(SourceCallback callback, Context context) {
        super(callback, context);
    }

    public String createMessage(String measurementId, Object value,
            Object event) {
        JSONObject message = new JSONObject();
        try {
            message.put(NAME_FIELD_NAME, measurementId);
            message.put(VALUE_FIELD_NAME, value);
            message.putOpt(EVENT_FIELD_NAME, event);
        } catch(JSONException e) {
            Log.w(TAG, "Unable to encode all data to JSON -- " +
                    "message may be incomplete", e);
        }
        return message.toString() + "\u0000";
    }

    /**
     * Parses a JSON string and calls handleMessage with the values.
     */
    protected void handleJson(String json) {
        final JSONObject message;

        try {
            message = new JSONObject(json);
        } catch(JSONException e) {
            Log.w(TAG, "Couldn't decode JSON from: " + json);
            return;
        }

        try {
            handleMessage(message.getString(NAME_FIELD_NAME),
                    message.get(VALUE_FIELD_NAME),
                    message.opt(EVENT_FIELD_NAME));
            return;
        } catch(JSONException e) {
            Log.w(TAG, "JSON message didn't have the expected format: "
                    + message, e);
        }
    }
}
