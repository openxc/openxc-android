package com.openxc.interfaces;

import android.content.Context;

import com.openxc.messages.VehicleMessage;
import com.openxc.sinks.DataSinkException;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.SourceCallback;

public class TestVehicleInterface implements VehicleInterface {
    public TestVehicleInterface(Context context, String resource) {
    }

    public boolean setResource(String resource) throws DataSourceException {
        return true;
    }

    public void setCallback(SourceCallback callback) {
    }

    public boolean isConnected() {
        return true;
    }

    public void stop() {
    }

    public void receive(VehicleMessage measurement) throws DataSinkException {
    }

    @Override
    public void onPipelineActivated() { }

    @Override
    public void onPipelineDeactivated() { }
}
