package com.openxc.sources;

import com.openxc.remote.RawMeasurement;

import com.openxc.sources.SourceCallback;

import android.content.Context;

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

    public JsonVehicleDataSource(Context context) {
        super(context);
    }

    public JsonVehicleDataSource(SourceCallback callback, Context context) {
        super(callback, context);
    }

    /**
     * Parses a JSON string and calls handleMessage with the values.
     */
    protected void handleJson(String json) {
        handleMessage(RawMeasurement.deserialize(json));
    }
}
