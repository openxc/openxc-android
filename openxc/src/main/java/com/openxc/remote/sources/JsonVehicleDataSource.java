package com.openxc.remote.sources;

public abstract class JsonVehicleDataSource
        extends AbstractVehicleDataSource {

    public JsonVehicleDataSource() {
        super();
    }

    public JsonVehicleDataSource(VehicleDataSourceCallbackInterface callback) {
        super(callback);
    }

    protected void parseJson(String json) {
        handleMessage("foo", "bar");
        handleMessage("foo", 42);
    }
}
