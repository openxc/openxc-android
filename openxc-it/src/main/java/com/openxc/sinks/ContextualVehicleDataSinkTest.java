package com.openxc.sinks;

import android.test.AndroidTestCase;

import android.test.suitebuilder.annotation.SmallTest;

public class ContextualVehicleDataSinkTest extends AndroidTestCase {
    ContextualVehicleDataSink sink;

    @SmallTest
    public void testConstructWithContext() {
        sink = new ContextualVehicleDataSink(getContext());
        // getContext is protected so we can't really test that it works
    }
}
