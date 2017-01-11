package com.openxc.sources;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.mockito.Matchers;
import org.mockito.ArgumentCaptor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.messages.VehicleMessage;

@RunWith(RobolectricTestRunner.class)
public class BaseVehicleDataSourceTest {
    SourceCallback mCallback;
    BaseSourceSpy mSource;

    @Before
    public void setup() {
        mCallback = mock(SourceCallback.class);
        mSource = new BaseSourceSpy(mCallback);
    }

    @Test
    public void disconnectedNoCallback() {
        mSource.setCallback(null);
        mSource.disconnected();
        verify(mCallback, never()).sourceDisconnected(mSource);
    }

    @Test
    public void connectedNoCallback() {
        mSource.setCallback(null);
        mSource.connected();
        verify(mCallback, never()).sourceDisconnected(mSource);
    }

    @Test
    public void callbackDisconnected() {
        mSource.disconnected();
        verify(mCallback).sourceDisconnected(mSource);
    }

    @Test
    public void callbackConnected() {
        mSource.connected();
        verify(mCallback).sourceConnected(mSource);
    }

    @Test
    public void disconnectsOnStop() {
        mSource.stop();
        mSource.disconnected();
        verify(mCallback, times(1)).sourceDisconnected(mSource);
    }

    @Test
    public void receivedMessagesAreTimestampped() {
        mSource.inject(new VehicleMessage());
        ArgumentCaptor<VehicleMessage> argument = ArgumentCaptor.forClass(
                VehicleMessage.class);
        verify(mCallback).receive(argument.capture());
        assertTrue(argument.getValue().isTimestamped());
    }

    @Test
    public void receiveMessageSentToCallback() {
        mSource.inject(new VehicleMessage());
        verify(mCallback).receive(Matchers.any(VehicleMessage.class));
    }

    @Test
    public void nullMessageNotSentToCallback() {
        mSource.inject(null);
        verify(mCallback, never()).receive(Matchers.any(VehicleMessage.class));
    }

    @Test
    public void handleWithNoCallback() {
        mSource.setCallback(null);
        mSource.inject(new VehicleMessage());
        verify(mCallback, never()).receive(Matchers.any(VehicleMessage.class));
    }

    @Test
    public void waitForCallbackAlreadySet() throws InterruptedException {
        mSource.setCallback(mCallback);
        Thread thread = new Thread() {
            @Override
            public void run() {
                mSource.waitForCallback();
            }
        };
        thread.start();
        thread.join(10);
    }

    @Test
    public void waitForCallback() throws InterruptedException {
        mSource.setCallback(null);
        Thread thread = new Thread() {
            @Override
            public void run() {
                mSource.waitForCallback();
            }
        };
        thread.start();
        mSource.setCallback(mCallback);
        thread.join(100);
        // TODO need to assert something
    }

    private class BaseSourceSpy extends BaseVehicleDataSource {
        public BaseSourceSpy(SourceCallback callback) {
            super(callback);
        }

        public void inject(VehicleMessage message) {
            handleMessage(message);
        }

        @Override
        public boolean isConnected() {
            return false;
        }
    }
}
