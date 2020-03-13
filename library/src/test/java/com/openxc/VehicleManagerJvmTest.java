package com.openxc;

import android.content.Intent;

import com.openxc.messages.VehicleMessage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;

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

    @Test
    public void onUnbindTest()
    {
        Assert.assertTrue(manager.onUnbind(new Intent()));
    }
}
