package com.openxc.sources;

public interface SourceCallback {
    public void receive(String measurementId, Object value, Object event);
}
