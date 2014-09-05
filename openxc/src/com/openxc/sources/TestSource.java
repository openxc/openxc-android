package com.openxc.sources;

import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;

public class TestSource extends BaseVehicleDataSource {
    public SourceCallback callback;
    public boolean delayAfterInject = true;

    public void sendTestMessage() {
        inject(new SimpleVehicleMessage("message", "value"));
    }

    public void inject(String name, Object value) {
        inject(new SimpleVehicleMessage(name, value));
    }

    public void inject(VehicleMessage message) {
        if(callback != null) {
            callback.receive(message);
            if(delayAfterInject) {
                // If we don't pause here the background thread that processes
                // the injected measurement may not get to run before we make
                // our assertions
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {}
            }
        }
    }

    @Override
    public void setCallback(SourceCallback theCallback) {
        callback = theCallback;
    }

    @Override
    public void stop() {
        callback = null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }
}
