package com.openxc;

import com.openxc.messages.VehicleMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class VehicleManagerJvmTest {
    VehicleManager manager;

    @Before
    public void setup()  {
        manager = new VehicleManager();
    }

    @Test
    public void doesntDereferenceNullIfNotConectedToRemote() {
        manager.send(new VehicleMessage());
    }
}
