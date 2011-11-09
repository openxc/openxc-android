package com.openxc.remote.sources;

public class ManualVehicleDataSource extends AbstractVehicleDataSource {
    public ManualVehicleDataSource() {
        super();
    }

    public ManualVehicleDataSource(
            VehicleDataSourceCallbackInterface callback) {
        super(callback);
    }

    public void trigger(String name, double value) {
    }

    public void trigger(String name, String value) {
    }
}
