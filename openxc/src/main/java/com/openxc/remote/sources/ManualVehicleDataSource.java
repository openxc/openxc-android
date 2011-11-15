package com.openxc.remote.sources;

import java.net.URI;

import android.content.Context;

public class ManualVehicleDataSource extends AbstractVehicleDataSource {
    public ManualVehicleDataSource() {
        super();
    }

    public ManualVehicleDataSource(
            VehicleDataSourceCallbackInterface callback) {
        super(callback);
    }

    public ManualVehicleDataSource(
            VehicleDataSourceCallbackInterface callback, URI uri) {
        super(callback);
    }

    public ManualVehicleDataSource(Context context,
            VehicleDataSourceCallbackInterface callback, URI uri) {
        super(context, callback);
    }



    public void trigger(String name, double value) {
        handleMessage(name, value);
    }

    public void trigger(String name, String value) {
        handleMessage(name, value);
    }

    public void run() { }

    public void stop() { }
}
