package com.openxc.remote.sources.manual;

import java.net.URI;

import com.openxc.remote.sources.AbstractVehicleDataSource;
import com.openxc.remote.sources.VehicleDataSourceCallbackInterface;

import android.content.Context;

/**
 * The vehicle data source that doesn't do anything unless you tell it to.
 *
 * This vehicle data source can be manually triggered with custom raw data -
 * great for testing. Unfortunately, you can't use it that way with the full
 * VehicleService stack, since there is no way to inject this dependency through
 * an Intent. The RemoteVehicleService has to initialize its own instance of
 * this object, and at that point we can no longer call the {@link trigger}
 * method.
 *
 * TODO This can probably be removed.
 */
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
