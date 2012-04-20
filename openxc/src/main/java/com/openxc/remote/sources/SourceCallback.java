package com.openxc.remote.sources;

public interface SourceCallback {
    public void receive(String measurementId, Object value, Object event);
}
