package com.openxc.sources;

import com.openxc.messages.SimpleVehicleMessage;

public class TestSource extends BaseVehicleDataSource {
    public SourceCallback callback;

    public void sendTestMessage() {
        if(callback != null) {
            callback.receive(new SimpleVehicleMessage("message", "value"));
        }
    }

    public void inject(String name, Object value) {
        if(callback != null) {
            callback.receive(new SimpleVehicleMessage(name, value));
            // If we don't pause here the background thread that processes
            // the injected measurement may not get to run before we make
            // our assertions
            // TODO make this less of an integration test, e.g. use the
            // datapipeline which doesn't have this background thread to
            // worry about.
            try {
                Thread.sleep(10);
            } catch(InterruptedException e) {}
            }
    }


    public void setCallback(SourceCallback theCallback) {
        callback = theCallback;
    }

    public void stop() {
        callback = null;
    }
}
