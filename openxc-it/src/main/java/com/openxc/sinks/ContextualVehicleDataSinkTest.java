package com.openxc.sinks;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import android.test.AndroidTestCase;

import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.Test;

public class ContextualVehicleDataSinkTest extends AndroidTestCase {
    ContextualVehicleDataSink sink;

    @SmallTest
    public void testConstructWithContext() {
        sink = new ContextualVehicleDataSink(getContext());
        // getContext is protected so we can't really test that it works
    }
}
