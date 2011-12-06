package com.openxc.remote.sources;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import android.util.Log;

public abstract class JsonVehicleDataSource
        extends AbstractVehicleDataSource {
    private static final String TAG = "JsonVehicleDataSource";

    public JsonVehicleDataSource() {
        super();
    }

    public JsonVehicleDataSource(Context context,
            VehicleDataSourceCallbackInterface callback) {
        super(context, callback);
    }

    public JsonVehicleDataSource(VehicleDataSourceCallbackInterface callback) {
        this(null, callback);
    }

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
