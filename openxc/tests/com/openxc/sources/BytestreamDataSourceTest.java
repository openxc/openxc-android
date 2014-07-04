package com.openxc.sources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;

import com.openxc.TestUtils;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.streamers.JsonStreamer;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class BytestreamDataSourceTest {
    TestBytestreamSource source;
    SourceCallback callback = mock(SourceCallback.class);

    @Before
    public void setup() {
        source = new TestBytestreamSource(callback);
    }

    @After
    public void teardown() {
        source.stop();
    }

    @Test
    public void startedIsRunning() {
        source.start();
        assertTrue(source.isRunning());
    }

    @Test
    public void initializedNotRunning() {
        assertFalse(source.isRunning());
    }

    @Test
    public void stoppedNotRunning() {
        source.start();
        source.stop();
        assertFalse(source.isRunning());
    }

    @Test
    public void errorOnReadDisconnects() {
        source.start();
        source.connect();
        source.nextReadIsError = true;
        source.inject(new byte[] {1,2,3,4});
        TestUtils.pause(10);
        assertTrue(source.isRunning());
        assertFalse(source.isConnected());
    }

    @Test
    public void exceptionOnReadDisconnects() {
        source.start();
        source.connect();
        source.nextReadThrowsException = true;
        source.inject(new byte[] {1,2,3,4});
        TestUtils.pause(10);
        assertTrue(source.isRunning());
        assertFalse(source.isConnected());
    }

    @Test
    public void readNotCalledUntilConnected() {
        source.start();
        source.inject(new byte[] {1,2,3,4});
        assertFalse(source.packets.isEmpty());
        source.connect();
        TestUtils.pause(10);
        assertTrue(source.packets.isEmpty());
    }

    @Test
    public void receiveInvalidDataNoCallback() {
        source.start();
        source.connect();
        source.inject(new byte[] {1,2,3,4});
        TestUtils.pause(10);
        verify(callback, never()).receive(Mockito.any(VehicleMessage.class));
    }

    @Test
    public void receiveValidJsonTriggersCallback() {
        source.start();
        source.connect();
        SimpleVehicleMessage message = new SimpleVehicleMessage("foo", "bar");
        source.inject(new JsonStreamer().serializeForStream(message));
        TestUtils.pause(100);
        ArgumentCaptor<VehicleMessage> argument = ArgumentCaptor.forClass(
                VehicleMessage.class);
        verify(callback).receive(argument.capture());
        VehicleMessage received = argument.getValue();
        received.untimestamp();
        assertEquals(received, message);
    }

    private class TestBytestreamSource extends BytestreamDataSource {
        public boolean connected = false;
        public ArrayList<byte[]> packets = new ArrayList<>();
        private Lock mPacketLock = new ReentrantLock();
        private Condition mPacketReceived = mPacketLock.newCondition();
        public boolean nextReadIsError = false;
        public boolean nextReadThrowsException = false;

        public TestBytestreamSource(SourceCallback callback) {
            super(callback, Robolectric.application);
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        public void inject(byte[] bytes) {
            try {
                mPacketLock.lock();
                packets.add(bytes);
                mPacketReceived.signal();
            } finally {
                mPacketLock.unlock();
            }
        }

        protected int read(byte[] bytes) throws IOException {
            try {
                mPacketLock.lock();
                while(packets.isEmpty()) {
                    mPacketReceived.await();
                }
                if(nextReadIsError) {
                    return -1;
                } else if(nextReadThrowsException) {
                    throw new IOException();
                } else {
                    byte[] data = packets.remove(0);
                    System.arraycopy(data, 0, bytes, 0, data.length);
                    return data.length;
                }
            } catch(InterruptedException e) {
                return -1;
            } finally {
                mPacketLock.unlock();
            }
        }

        protected void disconnect() {
            mConnectionLock.writeLock().lock();
            connected = false;
            disconnected();
            mConnectionLock.writeLock().unlock();
        }

        protected void connect() {
            mConnectionLock.writeLock().lock();
            connected = true;
            disconnected();
            mConnectionLock.writeLock().unlock();
        }
    }
}
