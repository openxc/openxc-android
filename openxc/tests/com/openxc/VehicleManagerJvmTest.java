package com.openxc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.messages.VehicleMessage;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class VehicleManagerJvmTest {
    VehicleManager manager;

    @Before
    public void setup()  {
        manager = new VehicleManager();
        manager.onCreate();
    }

    @Test
    public void doesntDereferenceNullIfNotConectedToRemote() {
        manager.send(new VehicleMessage());
    }
}
