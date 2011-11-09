package com.openxc.remote.sources;

public abstract class JsonVehicleDataSource
        extends AbstractVehicleDataSource {
    protected void parseJson(String json) {
        handleMessage("foo", "bar");
    }
}
