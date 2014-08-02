package com.openxc.sources;

import com.openxc.messages.SimpleVehicleMessage;

public class TestSource extends BaseVehicleDataSource {
    public SourceCallback callback;
    public boolean delayAfterInject = true;

    public void sendTestMessage() {
        if(callback != null) {
            callback.receive(new SimpleVehicleMessage("message", "value"));
        }
    }

    public void inject(String name, Object value) {
        if(callback != null) {
            callback.receive(new SimpleVehicleMessage(name, value));
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
