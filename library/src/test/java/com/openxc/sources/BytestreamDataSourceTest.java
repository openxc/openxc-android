package com.openxc.sources;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import com.openxc.TestUtils;
import com.openxc.messages.SerializationException;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.streamers.BinaryStreamer;
import com.openxc.messages.streamers.JsonStreamer;

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
        TestUtils.pause(100);
        assertTrue(source.isRunning());
        assertFalse(source.isConnected());
    }

    @Test
    public void exceptionOnReadDisconnects() {
        source.start();
        source.connect();
        source.nextReadThrowsException = true;
        source.inject(new byte[] {1,2,3,4});
        TestUtils.pause(50);
        assertTrue(source.isRunning());
        assertFalse(source.isConnected());
    }

    @Test
    public void readNotCalledUntilConnected() {
        source.start();
        source.inject(new byte[] {1,2,3,4});
        assertFalse(source.packets.isEmpty());
        source.connect();
        TestUtils.pause(20);
        assertTrue(source.packets.isEmpty());
    }

    @Test
    public void receiveInvalidDataNoCallback() {
        source.start();
        source.connect();
        source.inject(new byte[] {1,2,3,4});
        TestUtils.pause(20);
        verify(callback, never()).receive(Matchers.any(VehicleMessage.class));
    }

    @Test
    public void receiveValidBinaryTriggersCallback() throws SerializationException {
        source.start();
        source.connect();
        SimpleVehicleMessage message = new SimpleVehicleMessage("foo", "bar");
        source.inject(new BinaryStreamer().serializeForStream(message));
        TestUtils.pause(100);
        ArgumentCaptor<VehicleMessage> argument = ArgumentCaptor.forClass(
                VehicleMessage.class);
        verify(callback).receive(argument.capture());
        VehicleMessage received = argument.getValue();
        received.untimestamp();
        assertEquals(received, message);
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

    @Test
    public void readMultipleMessageAtOnceReceivesAll() {
        source.start();
        source.connect();
        List<SimpleVehicleMessage> messages = new ArrayList<>();
        messages.add(new SimpleVehicleMessage("1", "foo"));
        messages.add(new SimpleVehicleMessage("2", "bar"));
        messages.add(new SimpleVehicleMessage("3", "baz"));
        for(SimpleVehicleMessage message : messages) {
            source.inject(new JsonStreamer().serializeForStream(message), false);
        }
        source.signal();

        TestUtils.pause(100);
        ArgumentCaptor<VehicleMessage> argument = ArgumentCaptor.forClass(
                VehicleMessage.class);
        verify(callback, times(3)).receive(argument.capture());
        List<VehicleMessage> capturedMessages = argument.getAllValues();
        for(int i = 0; i < messages.size(); i++) {
            VehicleMessage received = capturedMessages.get(i);
            received.untimestamp();
            assertEquals(received, messages.get(i));
        }
    }

    private class TestBytestreamSource extends BytestreamDataSource {
        public boolean connected = false;
        public ArrayList<byte[]> packets = new ArrayList<>();
        private Lock mPacketLock = new ReentrantLock();
        private Condition mPacketReceived = mPacketLock.newCondition();
        public boolean nextReadIsError = false;
        public boolean nextReadThrowsException = false;

        public TestBytestreamSource(SourceCallback callback) {
            super(callback, RuntimeEnvironment.application);
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        public void signal() {
            try {
                mPacketLock.lock();
                mPacketReceived.signal();
            } finally {
                mPacketLock.unlock();
            }
        }

        public void inject(byte[] bytes) {
            inject(bytes, true);
        }

        public void inject(byte[] bytes, boolean signal) {
            try {
                mPacketLock.lock();
                packets.add(bytes);
                if(signal) {
                    mPacketReceived.signal();
                }
            } finally {
                mPacketLock.unlock();
            }
        }

        @Override
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
                    if(data != null) {
                        System.arraycopy(data, 0, bytes, 0, data.length);
                        return data.length;
                    } else {
                        return -1;
                    }
                }
            } catch(InterruptedException e) {
                return -1;
            } finally {
                mPacketLock.unlock();
            }
        }

        protected boolean write(byte[] bytes) {
            return true;
        }

        @Override
        protected void disconnect() {
            mConnectionLock.writeLock().lock();
            connected = false;
            disconnected();
            mConnectionLock.writeLock().unlock();
        }

        @Override
        protected void connect() {
            mConnectionLock.writeLock().lock();
            connected = true;
            disconnected();
            mConnectionLock.writeLock().unlock();
        }
    }
}
