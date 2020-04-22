package com.openxc.messages.streamers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SerializationException;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;

@RunWith(RobolectricTestRunner.class)
public class BinaryStreamerTest {
    BinaryStreamer streamer;
    SimpleVehicleMessage message = new SimpleVehicleMessage("foo", "bar");

    @Before
    public void setup() {
        streamer = new BinaryStreamer();
    }

    @Test
    public void emptyHasNoMessages() {
        assertThat(streamer.parseNextMessage(), nullValue());
    }

    @Test
    public void deserializeBadLengthReturnsNull()
            throws SerializationException {
        byte[] data = new byte[]{0,1,2,3,4};
        streamer.receive(data, data.length);
        assertThat(streamer.parseNextMessage(), nullValue());
    }

    @Test(expected=SerializationException.class)
    public void serializeEmptyFails()
            throws SerializationException {
        streamer.serializeForStream(new VehicleMessage());
    }

    @Test
    public void receiveLessThanFullBufferDoesntGrabAll()
            throws SerializationException {
        byte[] serialized = streamer.serializeForStream(message);
        streamer.receive(serialized, serialized.length / 2);
        assertThat(streamer.parseNextMessage(), nullValue());
    }

    @Test
    public void readingGenericThenSpecific() throws SerializationException {
        NamedVehicleMessage namedMessage = new NamedVehicleMessage("baz");
        byte[] bytes = streamer.serializeForStream(namedMessage);
        streamer.receive(bytes, bytes.length);

        bytes = streamer.serializeForStream(message);
        streamer.receive(bytes, bytes.length);

        assertThat(streamer.parseNextMessage(), equalTo((VehicleMessage) namedMessage));
        assertThat(streamer.parseNextMessage(), equalTo((VehicleMessage) message));
    }

    @Test
    public void readLinesOne() throws SerializationException {
        byte[] serialized = streamer.serializeForStream(message);
        streamer.receive(serialized, serialized.length);

        VehicleMessage deserialized = streamer.parseNextMessage();
        assertThat(deserialized, notNullValue());
        assertThat(deserialized, instanceOf(NamedVehicleMessage.class));
        NamedVehicleMessage deserializedMessage =
                (NamedVehicleMessage) deserialized;
        assertThat(message, equalTo(deserializedMessage));

        assertThat(streamer.parseNextMessage(), nullValue());
    }

    @Test
    public void leavePartial() throws SerializationException {
        byte[] bytes = streamer.serializeForStream(message);
        streamer.receive(bytes, bytes.length);

        NamedVehicleMessage namedMessage = new NamedVehicleMessage("baz");
        bytes = streamer.serializeForStream(namedMessage);
        streamer.receive(bytes, bytes.length  / 2);

        assertThat(streamer.parseNextMessage(), notNullValue());
        assertThat(streamer.parseNextMessage(), nullValue());
    }

    @Test
    public void completePartial() throws SerializationException {
        byte[] bytes = streamer.serializeForStream(message);
        streamer.receive(bytes, bytes.length);

        NamedVehicleMessage namedMessage = new NamedVehicleMessage("baz");
        bytes = streamer.serializeForStream(namedMessage);
        streamer.receive(bytes, bytes.length  / 2);

        assertThat(streamer.parseNextMessage(), notNullValue());

        int remainingBytes = bytes.length / 2;
        byte[] remainder = new byte[remainingBytes];
        System.arraycopy(bytes, remainingBytes, remainder, 0, remainingBytes);
        streamer.receive(remainder, remainingBytes);

        assertThat(streamer.parseNextMessage(), notNullValue());
        assertThat(streamer.parseNextMessage(), nullValue());
    }

    @Test
    public void deserializeSerialized() throws SerializationException {
        byte[] data = streamer.serializeForStream(message);
        streamer.receive(data, data.length);
        VehicleMessage deserialized = streamer.parseNextMessage();
        assertEquals(message, deserialized);
    }

    @Test
    public void dontDeserializeIfStreamTooShort() throws SerializationException {
        byte[] data = streamer.serializeForStream(message);
        byte[] half = new byte[data.length];
        System.arraycopy(data, 0, half, 0, data.length / 2);
        streamer.receive(half, data.length / 2);
        assertThat(streamer.parseNextMessage(), nullValue());
        System.arraycopy(data, data.length / 2, half, 0,
                data.length - data.length / 2);
        streamer.receive(half, half.length);
        VehicleMessage deserialized = streamer.parseNextMessage();
        assertEquals(message, deserialized);
    }

    // @Test
    // TODO the binary deserialization can get in a really messed up state if it
    // gets too far off, but i can't seem to reliably trip it in a test. it's
    // fixed in code now and works well over Bluetooth, but I think we could
    // make it better.
    // public void ignoreJunkDataInFront() throws SerializationException {
        // byte[] data = streamer.serializeForStream(message);
        // byte[] junk = new byte[] { 0x12, 0x0b };
        // byte[] dataWithJunkPrefix = new byte[data.length + junk.length];
        // System.arraycopy(junk, 0, dataWithJunkPrefix, 0, junk.length);
        // System.arraycopy(data, 0, dataWithJunkPrefix, junk.length, data.length);
        // streamer.receive(dataWithJunkPrefix, dataWithJunkPrefix.length);
        // VehicleMessage deserialized = streamer.parseNextMessage();
        // assertEquals(message, deserialized);
    // }

    @Test
    public void logTransferStatsAfterMegabyte() throws SerializationException {
        byte[] data = streamer.serializeForStream(message);
        for(int i = 0; i < 10000; i++) {
            streamer.receive(data, data.length);
        }

        for(int i = 0; i < 10000; i++) {
            VehicleMessage deserialized = streamer.parseNextMessage();
            assertEquals(message, deserialized);
        }
    }
}
