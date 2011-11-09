package com.openxc.remote.sources;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public abstract class JsonVehicleDataSource
        extends AbstractVehicleDataSource {
    private static final String TAG = "JsonVehicleDataSource";

    public JsonVehicleDataSource() {
        super();
    }

    public JsonVehicleDataSource(VehicleDataSourceCallbackInterface callback) {
        super(callback);
    }

    protected void parseJson(String json) {
        final JSONObject message;

        try {
            message = new JSONObject(json);
        } catch(JSONException e) {
            Log.i(TAG, "Couldn't decode JSON from: " + json);
            return;
        }

        Log.d(TAG, "Parsed JSON object " + message);
        try {
            handleMessage(message.getString("name"),
                    message.getDouble("value"));
            return;
        } catch(JSONException e) {
            Log.d(TAG, "Couldn't parse a double from JSON " + message +
                    " -- trying String next");
        }
        try {
            handleMessage(message.getString("name"),
                    message.getString("value"));
        } catch(JSONException e) {
            Log.w(TAG, "JSON value wasn't a double or string -- couldn't parse",
                    e);
        }
    }
}
