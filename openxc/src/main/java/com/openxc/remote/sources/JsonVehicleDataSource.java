package com.openxc.remote.sources;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import android.util.Log;

import com.openxc.remote.sinks.VehicleDataSink;

/**
 * A vehicle data source expecting JSON messages as raw input.
 *
 * Whether coming from a trace file or over a network, this class can parse a
 * JSON string and pass its measurement values to the handleMessage method of
 * the data source.
 *
 * The class is abstract as it does not describe the source of the data, only
 * the expected format.
 *
 * TODO Is it weird that this is data source class, but isn't a data source? It
 * feels more like a mixin, but that doesn't fit well with Java.
 */
public abstract class JsonVehicleDataSource
        extends AbstractVehicleDataSource {
    private static final String TAG = "JsonVehicleDataSource";

    public JsonVehicleDataSource() {
        super();
    }

    public JsonVehicleDataSource(Context context,
            VehicleDataSink callback) {
        super(context, callback);
    }

    public JsonVehicleDataSource(VehicleDataSink callback) {
        this(null, callback);
    }

    /**
     * Parses a JSON string and calls handleMessage with the values.
     *
     * TODO It would be perhaps better if this didn't have the implicit
     * dependency on handleMessage, but we can't return multiple values from
     * this function without having yet another wrapper object.
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
            handleMessage(message.getString("name"),
                    message.get("value"),
                    message.opt("event"));
            return;
        } catch(JSONException e) {
            Log.w(TAG, "JSON message didn't have the expected format: "
                    + message, e);
        }
    }
}
